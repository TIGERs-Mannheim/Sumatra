/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.sim;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.Referee;
import edu.tigers.sumatra.clock.ThreadUtil;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.pose.Pose;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.math.vector.Vector3f;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.referee.AReferee;
import edu.tigers.sumatra.referee.IRefereeObserver;
import edu.tigers.sumatra.sim.collision.bot.BotCollisionHandler;
import edu.tigers.sumatra.sim.dynamics.bot.SimBotAction;
import edu.tigers.sumatra.sim.dynamics.bot.SimBotState;
import edu.tigers.sumatra.sim.net.SimNetServer;
import edu.tigers.sumatra.vision.data.FilteredVisionBall;
import edu.tigers.sumatra.vision.data.FilteredVisionBot;
import edu.tigers.sumatra.vision.data.FilteredVisionFrame;
import edu.tigers.sumatra.wp.ball.trajectory.ABallTrajectory;
import edu.tigers.sumatra.wp.ball.trajectory.BallFactory;
import edu.tigers.sumatra.wp.data.BallTrajectoryState;


/**
 * Simulate vision in Sumatra
 */
public class SumatraSimulator extends ASumatraSimulator implements IRefereeObserver, SimNetServer.ISimNetObserver
{
	private static final Logger log = Logger.getLogger(SumatraSimulator.class.getName());

	private static final long CAM_DT = 10_000_000;
	private static final long SIM_DT = 1_000_000;
	private static final double SIMULATION_BUFFER_TIME = 5;

	private SimState simState = new SimState();

	private final BotCollisionHandler botCollisionHandler = new BotCollisionHandler();

	private CountDownLatch steppingLatch;

	private boolean running;
	private double simSpeed;
	private boolean manageBotCount;

	private final Deque<FilteredVisionFrame> stateBuffer = new ArrayDeque<>();

	private final SimNetServer simNetServer = new SimNetServer();

	private static boolean waitForRemoteAis = false;

	private final Object simSync = new Object();

	private long lastRefereeCommandId = -1;
	private IVector3 ballTargetPos = null;


	@Override
	public void initModule()
	{
		simState = new SimState();
		running = false;
		steppingLatch = new CountDownLatch(1);
		simSpeed = 1;
		manageBotCount = true;
		stateBuffer.clear();
	}


	@Override
	public void startModule()
	{
		super.startModule();

		AReferee referee = SumatraModel.getInstance().getModule(AReferee.class);
		referee.addObserver(this);

		if (getSubnodeConfiguration().getBoolean("start-sim-net-server", true))
		{
			simNetServer.start();
		}

		final boolean startPaused = getSubnodeConfiguration().getBoolean("start-paused", false);

		if (!waitForRemoteAis)
		{
			if (!startPaused)
			{
				play();
			}
		} else
		{
			simNetServer.addObserver(this);
		}
	}


	@Override
	public void stopModule()
	{
		super.stopModule();

		AReferee referee = SumatraModel.getInstance().getModule(AReferee.class);
		referee.removeObserver(this);

		simState.getSimulatedBots().keySet().forEach(this::unregisterBot);

		simNetServer.removeObserver(this);
		simNetServer.stop();
	}


	/**
	 * @param botId
	 * @param initialPose [mm, mm, rad]
	 * @param vel [mm/s, mm/s, rad/s]
	 */
	public void registerBot(final BotID botId, final Pose initialPose, final IVector3 vel)
	{
		synchronized (simSync)
		{
			if (isBotRegistered(botId))
			{
				log.warn("Can not register a bot id twice: " + botId);
			} else
			{
				SimBotState state = new SimBotState(initialPose, vel);
				simState.getSimulatedBots().put(botId, new SimulatedBot(botId, state, simState.getSimulatedBall()));
				simulatorObservers.forEach(o -> o.onBotAdded(botId));
			}
		}
	}


	public void unregisterBot(final BotID botId)
	{
		synchronized (simSync)
		{
			final SimulatedBot simulatedBot = simState.getSimulatedBots().remove(botId);
			if (simulatedBot == null)
			{
				log.warn("No simulated bot registered with id " + botId);
			} else
			{
				simulatorObservers.forEach(o -> o.onBotRemove(botId));
			}
		}
	}


	public boolean isBotRegistered(final BotID botID)
	{
		return simState.getSimulatedBots().containsKey(botID);
	}


	private void updateSimBotActions()
	{
		final List<Map<BotID, SimBotAction>> simBotActionList = getSimBotActionList();

		final Map<BotID, SimBotAction> receivedActions = simNetServer.receive();

		Map<BotID, SimBotAction> actionMap = new HashMap<>();
		simBotActionList.forEach(actionMap::putAll);
		actionMap.putAll(receivedActions);

		for (Map.Entry<BotID, SimulatedBot> bot : simState.getSimulatedBots().entrySet())
		{
			final SimBotAction simBotAction = actionMap.get(bot.getKey());
			if (simBotAction != null)
			{
				bot.getValue().setAction(simBotAction);
			} else
			{
				bot.getValue().setAction(SimBotAction.idle());
			}
		}
	}


	private List<Map<BotID, SimBotAction>> getSimBotActionList()
	{
		Map<BotID, SimBotState> botStates = new HashMap<>();
		for (Map.Entry<BotID, SimulatedBot> entry : simState.getSimulatedBots().entrySet())
		{
			final SimBotState simBotState = entry.getValue().getState();
			botStates.put(entry.getKey(), simBotState);
		}

		simulatorActionCallbacks.forEach(cb -> cb.updateConnectedBotList(botStates));
		return simulatorActionCallbacks.stream()
				.map(cb -> cb.nextSimBotActions(botStates, simState.getSimTime()))
				.collect(Collectors.toList());
	}


	private void simulate(final long dt)
	{
		processBotCollisions();
		updateSimBotActions();

		for (int i = 0; (i < (dt / SIM_DT)) && (i < 100); i++)
		{
			double stepDt = SIM_DT / 1e9;
			simState.incSimTime(SIM_DT);
			simState.getSimulatedBall().collision(stepDt);
			simState.getSimulatedBall().dynamics(stepDt);
			simState.getSimulatedBots().values().forEach(b -> b.dynamics(stepDt));
		}

		simNetServer.publish(simState);
	}


	private void moveBallToTargetPos()
	{
		if (ballTargetPos == null)
		{
			return;
		}

		moveBotsAwayFromBallTargetPos();

		ABallTrajectory traj = BallFactory
				.createTrajectoryFromKick(ballTargetPos.getXYVector(), Vector2.zero(), false);
		simState.setLastKickEvent(null);
		simState.getSimulatedBall().setState(traj.getMilliStateAtTime(0));
		ballTargetPos = null;
	}


	private void moveBotsAwayFromBallTargetPos()
	{
		final Optional<SimulatedBot> botInWay = simState.getSimulatedBots().values().stream()
				.filter(b -> b.getState().getPose().getPos()
						.distanceTo(ballTargetPos.getXYVector()) < Geometry.getBotRadius() * 2)
				.findFirst();
		if (botInWay.isPresent())
		{
			IVector2 newPos = Vector2.fromXY(0, Geometry.getFieldWidth() / 2 + Geometry.getBotRadius() * 2);
			botInWay.get().setState(new SimBotState(Pose.from(newPos, 0), Vector3.zero()));
		}
	}


	private void storeFrameInStateBuffer(final FilteredVisionFrame filteredFrame)
	{
		FilteredVisionFrame latestState = stateBuffer.peekLast();
		if ((latestState == null) || (latestState.getTimestamp() < simState.getSimTime()))
		{
			stateBuffer.addLast(filteredFrame);

			while ((stateBuffer.peekFirst() != null)
					&& (((stateBuffer.peekLast().getTimestamp() - stateBuffer.peekFirst().getTimestamp())
							/ 1e9) >= SIMULATION_BUFFER_TIME))
			{
				stateBuffer.removeFirst();
			}
		}
	}


	private void processBotCollisions()
	{
		botCollisionHandler.process(new ArrayList<>(simState.getSimulatedBots().values()));
	}


	private FilteredVisionFrame createFilteredFrame()
	{
		List<FilteredVisionBot> filteredBots = new ArrayList<>();
		for (Map.Entry<BotID, SimulatedBot> e : simState.getSimulatedBots().entrySet())
		{
			BotID botID = e.getKey();
			SimulatedBot bot = e.getValue();
			SimBotState botState = bot.getState();
			Pose pose = botState.getPose();
			IVector3 vel = botState.getVel();
			FilteredVisionBot fBot = FilteredVisionBot.Builder.create()
					.withPos(pose.getPos())
					.withVel(vel.getXYVector().multiplyNew(1e-3))
					.withOrientation(pose.getOrientation())
					.withAVel(vel.z())
					.withId(botID)
					.withQuality(1.0)
					.build();
			filteredBots.add(fBot);
		}

		BallTrajectoryState ballState = simState.getSimulatedBall().getState();
		FilteredVisionBall filteredBall = FilteredVisionBall.Builder.create()
				.withPos(ballState.getPos().getXYZVector())
				.withVel(ballState.getVel().getXYZVector())
				.withAcc(ballState.getAcc().getXYZVector())
				.withLastVisibleTimestamp(simState.getSimTime())
				.withIsChipped(ballState.isChipped())
				.withvSwitch(ballState.getvSwitchToRoll())
				.build();

		return FilteredVisionFrame.Builder.create()
				.withBots(filteredBots)
				.withBall(filteredBall)
				.withId(simState.getFrameId())
				.withTimestamp(simState.getSimTime())
				.withKickEvent(simState.getLastKickEvent())
				.withKickFitState(filteredBall)
				.build();
	}


	private void updateKickEvent()
	{
		if (simState.getSimulatedBall().getState().getVel().getLength() < 0.01)
		{
			simState.setLastKickEvent(null);
		}

		if (simState.getSimulatedBall().getLastCollision().isPresent())
		{
			IVector2 pos = simState.getSimulatedBall().getLastCollision().get().getPos();
			BotID botID = simState.getSimulatedBall().getLastCollision().get().getObject().getBotID();
			if (botID.isBot()
					&& (simState.getLastKickEvent() == null || !pos.equals(simState.getLastKickEvent().getPosition())))
			{
				simState.setLastKickEvent(new SimKickEvent(pos, botID, simState.getSimTime()));
			}
		}
	}


	@Override
	public void run()
	{
		simState.incSimTime(SIM_DT);
		publishFilteredVisionFrame(createFilteredFrame());
		while (!Thread.interrupted())
		{
			try
			{
				steppingLatch.await();
			} catch (InterruptedException e1)
			{
				Thread.currentThread().interrupt();
				return;
			}

			long t0 = System.nanoTime();

			try
			{
				process();
			} catch (Exception e)
			{
				log.error("Uncaught exception in simulator", e);
			} catch (Throwable err)
			{
				log.error("Uncaught error in simulator", err);
			}

			long t1 = System.nanoTime();

			if (!running)
			{
				steppingLatch = new CountDownLatch(1);
			} else
			{
				long mDt = (long) (CAM_DT / simSpeed);
				long sleep = mDt - (t1 - t0);
				if (sleep > 0)
				{
					assert sleep < (long) 1e9;
					ThreadUtil.parkNanosSafe(sleep);
				}
			}
		}
	}


	private void process()
	{
		synchronized (simSync)
		{
			simulate(CAM_DT);
			updateKickEvent();
			simState.incFrameId();
			final FilteredVisionFrame filteredFrame = createFilteredFrame();
			storeFrameInStateBuffer(filteredFrame);
			publishFilteredVisionFrame(filteredFrame);
		}
		if (running)
		{
			moveBallToTargetPos();
			handleBotCount();
		}
	}


	/**
	 * @param time
	 */
	public void reset(final long time)
	{
		log.debug("Resetting simulation");
		boolean run = running;
		if (run)
		{
			pause();
		}
		simState.setSimTime(time);
		stateBuffer.clear();
		simState.getSimulatedBall().resetState();
		simState.setLastKickEvent(null);
		simState.getSimulatedBots().keySet().forEach(this::unregisterBot);
		if (run)
		{
			play();
		}
		log.debug("Reset simulation");
	}


	/**
	 * Pause cam
	 */
	public void pause()
	{
		running = false;
		log.debug("Paused simulation");
	}


	/**
	 * Start cam
	 */
	public void play()
	{
		running = true;
		steppingLatch.countDown();
		log.debug("Started simulation");
	}


	/**
	 * Do one step
	 */
	public void step()
	{
		steppingLatch.countDown();
	}


	public boolean isRunning()
	{
		return running;
	}


	/**
	 * Step backwards
	 */
	public void stepBack()
	{
		if (running)
		{
			// not useful, if not paused
			return;
		}
		// remove latest state, then remove one more. Afterwards, one step will be simulated again
		// this results in one step overall.
		// the extra simulation step is required to update the state on remote simulator clients and to keep in sync
		// with other threads
		stateBuffer.pollLast();
		FilteredVisionFrame frame = stateBuffer.pollLast();
		if (frame != null)
		{
			for (Map.Entry<BotID, SimulatedBot> e : simState.getSimulatedBots().entrySet())
			{
				BotID botID = e.getKey();
				SimulatedBot bot = e.getValue();
				Optional<FilteredVisionBot> filtBot = frame.getBots().stream()
						.filter(b -> b.getBotID().equals(botID))
						.findFirst();

				if (!filtBot.isPresent())
				{
					continue;
				}

				Pose pose = Pose.from(filtBot.get().getPos(), filtBot.get().getOrientation());
				IVector3 vel = Vector3.from2d(filtBot.get().getVel().multiplyNew(1000), filtBot.get().getAngularVel());
				bot.setState(new SimBotState(pose, vel));
			}

			FilteredVisionBall filteredState = frame.getBall();
			BallTrajectoryState ballState = BallTrajectoryState.aBallState()
					.withPos(filteredState.getPos())
					.withVel(filteredState.getVel())
					.withAcc(filteredState.getAcc())
					.withChipped(filteredState.isChipped())
					.withVSwitchToRoll(filteredState.getVSwitch())
					.withSpin(filteredState.getSpin())
					.build();

			simState.getSimulatedBall().setState(ballState);

			simState.setSimTime(frame.getTimestamp());
			steppingLatch.countDown();
		}
	}


	@Override
	public void placeBall(final IVector3 pos, final IVector3 vel)
	{
		ABallTrajectory traj = BallFactory
				.createTrajectoryFromKick(pos.getXYVector(), vel, vel.z() > 0);

		simState.setLastKickEvent(new SimKickEvent(pos.getXYVector(), BotID.noBot(), simState.getSimTime()));

		simState.getSimulatedBall().setState(traj.getMilliStateAtTime(0));
		ballTargetPos = null;
	}


	/**
	 * @param simSpeed the simSpeed to set
	 */
	public void setSimSpeed(final double simSpeed)
	{
		this.simSpeed = simSpeed;
	}


	@Override
	public void onNewRefereeMsg(final Referee.SSL_Referee refMsg)
	{
		simState.setLatestRefereeMessage(refMsg);

		if (refMsg.getCommand() == Referee.SSL_Referee.Command.HALT
				&& refMsg.getCommandCounter() != lastRefereeCommandId
				&& refMsg.getDesignatedPosition() != null)
		{
			ballTargetPos = Vector3.fromXYZ(
					refMsg.getDesignatedPosition().getX(),
					refMsg.getDesignatedPosition().getY(),
					0.0);
		}

		lastRefereeCommandId = refMsg.getCommandCounter();
	}


	private void handleBotCount()
	{
		Referee.SSL_Referee refMsg = simState.getLatestRefereeMessage();
		if (refMsg != null && manageBotCount)
		{
			handleTeam(refMsg.getBlue(), ETeamColor.BLUE);
			handleTeam(refMsg.getYellow(), ETeamColor.YELLOW);
		}
	}


	private void handleTeam(Referee.SSL_Referee.TeamInfo teamInfo, ETeamColor team)
	{
		long numOnField = simState.getSimulatedBots().keySet().stream().filter(b -> b.getTeamColor() == team).count();
		long numToRemove = numOnField - teamInfo.getMaxAllowedBots();
		if (numToRemove > 0)
		{
			nearestBotsToInterchangePoints(team, numToRemove).forEach(this::unregisterBot);
		} else if (numToRemove < 0)
		{
			IVector2 interchangePos = findFreeInterchangePos(team);
			final BotID botID = nextUnassignedBotId(team);
			if (interchangePos != null && botID.isBot())
			{
				registerBot(botID, Pose.from(interchangePos, 0), Vector3f.zero());
			}
		}
	}


	private IVector2 findFreeInterchangePos(ETeamColor team)
	{
		double sign = team == ETeamColor.BLUE ? -1 : 1;
		IVector2 interchangePos = Vector2f.fromXY(0, sign * Geometry.getFieldWidth() / 2);

		boolean posFree = simState.getSimulatedBots().values().stream().noneMatch(
				s -> s.getState().getPose().getPos().distanceTo(interchangePos) < Geometry.getBotRadius() * 2);
		if (posFree)
		{
			return interchangePos;
		}
		return null;
	}


	private BotID nextUnassignedBotId(final ETeamColor team)
	{
		for (int i = 0; i < BotID.BOT_ID_MAX; i++)
		{
			final BotID botId = BotID.createBotId(i, team);
			if (!simState.getSimulatedBots().keySet().contains(botId))
			{
				return botId;
			}
		}
		return BotID.noBot();
	}


	private Set<BotID> nearestBotsToInterchangePoints(ETeamColor team, long numBots)
	{
		return simState.getSimulatedBots().entrySet().stream()
				.filter(e -> e.getKey().getTeamColor() == team)
				.sorted(Comparator.comparingDouble(this::distanceToClosestInterchangePoint))
				.limit(numBots)
				.map(Map.Entry::getKey)
				.collect(Collectors.toSet());
	}


	private double distanceToClosestInterchangePoint(Map.Entry<BotID, SimulatedBot> entry)
	{
		double y = Geometry.getFieldWidth() / 2;
		double d1 = Vector2f.fromXY(0, y).distanceToSqr(entry.getValue().getState().getPose().getPos());
		double d2 = Vector2f.fromXY(0, -y).distanceToSqr(entry.getValue().getState().getPose().getPos());
		return Math.min(d1, d2);
	}


	@Override
	public void onNewClient()
	{
		if (!running && simNetServer.getConnectedClientTeams().size() == 2)
		{
			play();
		}
	}


	/**
	 * Should the simulator wait for all remote AIs to connect, before the simulation is started?
	 *
	 * @param waitForRemoteAis
	 */
	public static void setWaitForRemoteAis(final boolean waitForRemoteAis)
	{
		SumatraSimulator.waitForRemoteAis = waitForRemoteAis;
	}


	public void setManageBotCount(final boolean manageBotCount)
	{
		this.manageBotCount = manageBotCount;
	}
	
	
	public boolean getManageBotCount()
	{
		return this.manageBotCount;
	}
}
