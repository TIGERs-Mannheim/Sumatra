/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.sim;

import edu.tigers.sumatra.ball.BallState;
import edu.tigers.sumatra.ball.trajectory.IBallTrajectory;
import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.AObjectID;
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
import edu.tigers.sumatra.referee.proto.SslGcRefereeMessage;
import edu.tigers.sumatra.sim.collision.ball.ICollision;
import edu.tigers.sumatra.sim.collision.bot.BotCollisionHandler;
import edu.tigers.sumatra.sim.dynamics.bot.SimBotAction;
import edu.tigers.sumatra.sim.dynamics.bot.SimBotState;
import edu.tigers.sumatra.sim.net.SimNetServer;
import edu.tigers.sumatra.thread.NamedThreadFactory;
import edu.tigers.sumatra.vision.IBallPlacer;
import edu.tigers.sumatra.vision.data.FilteredVisionBall;
import edu.tigers.sumatra.vision.data.FilteredVisionBot;
import edu.tigers.sumatra.vision.data.FilteredVisionFrame;
import edu.tigers.sumatra.vision.data.FilteredVisionKick;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.ThreadContext;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.stream.Collectors;


/**
 * Simulate vision in Sumatra
 */
@Log4j2
public class SumatraSimulator extends ASumatraSimulator implements IRefereeObserver, SimNetServer.ISimNetObserver
{
	private static final long CAM_DT = 10_000_000;
	private static final long SIM_DT = 1_000_000;
	private static final double SIMULATION_BUFFER_TIME = 5;

	private SimState simState = new SimState();
	private ExecutorService service = null;
	private Future<?> future = null;

	private final BotCollisionHandler botCollisionHandler = new BotCollisionHandler();

	private boolean running;
	private double simSpeed;
	private boolean manageBotCount;

	private final Deque<FilteredVisionFrame> stateBuffer = new ArrayDeque<>();

	private final SimNetServer simNetServer = new SimNetServer();

	private static boolean waitForRemoteAis = false;

	private final Object simSync = new Object();
	private final Object ctrlSync = new Object();

	private IVector3 ballTargetPos = null;
	private long tHaltStart;


	@Override
	public void initModule()
	{
		simState = new SimState();
		running = false;
		simSpeed = 1;
		stateBuffer.clear();
		setBallPlacer(new BallPlacer());

		AReferee referee = SumatraModel.getInstance().getModule(AReferee.class);
		referee.addObserver(this);

		String key = SumatraSimulator.class.getCanonicalName() + ".manageBotCount";
		manageBotCount = Boolean.parseBoolean(SumatraModel.getInstance().getUserProperty(key, "true"));
	}


	@Override
	public void startModule()
	{
		reset();
		service = Executors.newSingleThreadExecutor(new NamedThreadFactory("SumatraSimulator"));

		if (getSubnodeConfiguration().getBoolean("start-sim-net-server", true))
		{
			simNetServer.start();
		}

		final boolean startPaused = getSubnodeConfiguration().getBoolean("start-paused", false);

		if (waitForRemoteAis)
		{
			log.debug("Waiting for remote AIs");
			simNetServer.addObserver(this);
		} else if (!startPaused)
		{
			resume();
		}
	}


	@Override
	public void deinitModule()
	{
		super.deinitModule();

		AReferee referee = SumatraModel.getInstance().getModule(AReferee.class);
		referee.removeObserver(this);
	}


	@Override
	public void stopModule()
	{
		running = false;
		service.shutdown();
		try
		{
			boolean terminated = service.awaitTermination(1, TimeUnit.SECONDS);
			if (!terminated)
			{
				log.error("Could not terminate simulator");
			}
		} catch (InterruptedException e)
		{
			log.error("Interrupted while awaiting termination", e);
			Thread.currentThread().interrupt();
		}

		simState.getSimulatedBots().keySet().forEach(this::unregisterBot);

		simNetServer.removeObserver(this);
		simNetServer.stop();
	}


	/**
	 * @param botId
	 * @param initialPose [mm, mm, rad]
	 * @param vel         [mm/s, mm/s, rad/s]
	 */
	public void registerBot(final BotID botId, final Pose initialPose, final IVector3 vel)
	{
		synchronized (simSync)
		{
			log.debug("Registering bot {}", botId);
			if (isBotRegistered(botId))
			{
				log.warn("Can not register a bot id twice: {}", botId);
			} else
			{
				SimBotState state = new SimBotState(initialPose, vel);
				simState.getSimulatedBots().put(botId, new SimulatedBot(botId, state));
				simulatorObservers.forEach(o -> o.onBotAdded(botId));
			}
		}
	}


	public void unregisterBot(final BotID botId)
	{
		synchronized (simSync)
		{
			log.debug("Unregistering bot {}", botId);
			final SimulatedBot simulatedBot = simState.getSimulatedBots().remove(botId);
			if (simulatedBot == null)
			{
				log.warn("No simulated bot registered with id {}", botId);
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
			bot.getValue().setAction(Objects.requireNonNullElseGet(simBotAction, SimBotAction::idle));
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

		simulatorActionCallbacks.forEach(cb -> cb.updateConnectedBotList(botStates.keySet()));
		return simulatorActionCallbacks.stream()
				.map(cb -> cb.nextSimBotActions(botStates, simState.getSimTime()))
				.toList();
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
			simState.getSimulatedBots().values()
					.forEach(b -> b.setBallPos(simState.getSimulatedBall().getState().getPos().getXYVector()));
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

		IBallTrajectory traj = Geometry.getBallFactory().createTrajectoryFromBallAtRest(ballTargetPos.getXYVector());
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

			while (hasNext())
			{
				stateBuffer.removeFirst();
			}
		}
	}


	private boolean hasNext()
	{
		var first = stateBuffer.peekFirst();
		if (first == null)
		{
			return false;
		}
		var diff = (stateBuffer.peekLast().getTimestamp() - first.getTimestamp()) / 1e9;
		return diff >= SIMULATION_BUFFER_TIME;
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
			FilteredVisionBot fBot = FilteredVisionBot.builder()
					.withPos(pose.getPos())
					.withVel(vel.getXYVector().multiplyNew(1e-3))
					.withOrientation(pose.getOrientation())
					.withAngularVel(vel.z())
					.withBotID(botID)
					.withTimestamp(simState.getSimTime())
					.withQuality(1.0)
					.build();
			filteredBots.add(fBot);
		}

		FilteredVisionBall filteredBall = FilteredVisionBall.builder()
				.withTimestamp(simState.getSimTime())
				.withBallState(simState.getSimulatedBall().getState())
				.withLastVisibleTimestamp(simState.getSimTime())
				.build();

		FilteredVisionKick filteredKick = null;

		if (simState.getLastKickEvent() != null)
		{
			SimKickEvent kick = simState.getLastKickEvent();

			filteredKick = FilteredVisionKick.builder()
					.withKickTimestamp(kick.getTimestamp())
					.withTrajectoryStartTime(kick.getTimestamp())
					.withKickingBot(kick.getKickingBot())
					.withKickingBotPosition(kick.getKickingBotPosition())
					.withKickingBotOrientation(kick.getBotDirection())
					.withNumBallDetectionsSinceKick(100)
					.withBallTrajectory(Geometry.getBallFactory().createTrajectoryFromState(kick.getKickBallState()))
					.build();
		}

		return FilteredVisionFrame.builder()
				.withBots(filteredBots)
				.withBall(filteredBall)
				.withId(simState.getFrameId())
				.withTimestamp(simState.getSimTime())
				.withKick(filteredKick)
				.withShapeMap(new ShapeMap())
				.build();
	}


	private void updateKickEvent()
	{
		if (simState.getSimulatedBall().getState().getVel().getLength() < 10)
		{
			simState.setLastKickEvent(null);
			return;
		}

		Optional<ICollision> lastCollision = simState.getSimulatedBall().getLastCollision();
		if (lastCollision.isPresent())
		{
			IVector2 pos = lastCollision.get().getPos();
			BotID botID = lastCollision.get().getObject().getBotID();
			SimulatedBot simulatedBot = simState.getSimulatedBots().get(botID);
			if (botID.isBot() && simulatedBot != null
					&& (simState.getLastKickEvent() == null || !pos.equals(simState.getLastKickEvent().getPosition())))
			{
				Pose botPose = simulatedBot.getState().getPose();
				simState.setLastKickEvent(
						new SimKickEvent(pos, botID, simState.getSimTime(), botPose.getPos(), botPose.getOrientation(),
								simState.getSimulatedBall().getState()));
			}
		}
	}


	@SuppressWarnings("java:S1181") // Catching Throwable intentionally
	@Override
	public void run()
	{
		long tLast = 0;
		do
		{
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

			long tNow = System.nanoTime();
			long mDt = (long) (CAM_DT / simSpeed);
			long sleep = mDt - (tNow - tLast);
			if (sleep > 0)
			{
				assert sleep < (long) 1e9;
				LockSupport.parkNanos(sleep);
			}
			tLast = System.nanoTime();
		} while (running);

		ThreadContext.remove("wfTs");
		ThreadContext.remove("wfId");
	}


	private void process()
	{
		synchronized (simSync)
		{
			simulate(CAM_DT);
			updateKickEvent();
			simState.incFrameId();
			ThreadContext.put("wfTs", String.valueOf(simState.getSimTime()));
			ThreadContext.put("wfId", String.valueOf(simState.getFrameId()));
			final FilteredVisionFrame filteredFrame = createFilteredFrame();
			storeFrameInStateBuffer(filteredFrame);
			publishFilteredVisionFrame(filteredFrame);
		}
		moveBallToTargetPos();
		handleBotCount();
	}


	/**
	 * Reset the simulation to zero.
	 */
	public void reset()
	{
		log.debug("Resetting simulation");
		synchronized (ctrlSync)
		{
			if (running)
			{
				throw new IllegalStateException("Simulation must not be running");
			}
			stateBuffer.clear();
			simState.getSimulatedBots().keySet().forEach(this::unregisterBot);
			simState = new SimState();
			// Do not start with time=0, as this is sometimes a special case
			simState.setSimTime(SIM_DT);
			simulatorActionCallbacks.forEach(cb -> cb.updateConnectedBotList(Set.of()));
			onClearCamFrame();
			publishFilteredVisionFrame();
		}
		log.debug("Reset simulation");
	}


	public void publishFilteredVisionFrame()
	{
		publishFilteredVisionFrame(createFilteredFrame());
	}


	/**
	 * Pause simulation. Blocks until simulation is paused.
	 */
	public void pause()
	{
		log.debug("Pause simulation");
		synchronized (ctrlSync)
		{
			if (!running)
			{
				log.debug("Simulation is already paused");
				return;
			}
			running = false;
			try
			{
				future.get();
			} catch (InterruptedException e)
			{
				log.warn("Interrupted while waiting for the simulation to pause");
				Thread.currentThread().interrupt();
			} catch (ExecutionException e)
			{
				log.warn("Sim future execution exception", e);
			}
		}
		log.debug("Paused simulation at {}", simState.getSimTime());
	}


	/**
	 * Resume simulation. Does not block.
	 */
	public void resume()
	{
		log.debug("Resume simulation at {}", simState.getSimTime());
		synchronized (ctrlSync)
		{
			if (running)
			{
				log.debug("Simulation is already running");
				return;
			}
			running = true;
			future = service.submit(this);
		}
		log.debug("Resumed simulation");
	}


	/**
	 * Do one step
	 */
	public Optional<Future<Void>> step()
	{
		synchronized (ctrlSync)
		{
			if (running)
			{
				return Optional.empty();
			}
			return Optional.of(service.submit(this, null));
		}
	}


	public void stepBlocking()
	{
		log.debug("Start step");
		try
		{
			var stepFuture = step();
			if (stepFuture.isEmpty())
			{
				return;
			}
			stepFuture.get().get();
		} catch (InterruptedException e)
		{
			Thread.currentThread().interrupt();
		} catch (ExecutionException e)
		{
			log.warn("Step failed", e);
		}
		log.debug("Stop step");
	}


	public boolean isRunning()
	{
		synchronized (ctrlSync)
		{
			return running;
		}
	}


	/**
	 * Step backwards
	 */
	public void stepBack()
	{
		synchronized (ctrlSync)
		{
			log.debug("Step back");
			if (running)
			{
				log.debug("Can not step back while simulation is running");
				return;
			}
			// remove latest state, then remove one more. Afterwards, one step will be simulated again
			// this results in one step overall.
			// the extra simulation step is required to update the state on remote simulator clients and to keep in sync
			// with other threads
			stateBuffer.pollLast();
			FilteredVisionFrame frame = stateBuffer.pollLast();
			if (frame == null)
			{
				return;
			}
			for (Map.Entry<BotID, SimulatedBot> e : simState.getSimulatedBots().entrySet())
			{
				BotID botID = e.getKey();
				SimulatedBot bot = e.getValue();
				Optional<FilteredVisionBot> filtBot = frame.getBots().stream()
						.filter(b -> b.getBotID().equals(botID))
						.findFirst();

				if (filtBot.isEmpty())
				{
					continue;
				}

				Pose pose = Pose.from(filtBot.get().getPos(), filtBot.get().getOrientation());
				IVector3 vel = Vector3.from2d(filtBot.get().getVel().multiplyNew(1000), filtBot.get().getAngularVel());
				bot.setState(new SimBotState(pose, vel));
			}

			FilteredVisionBall filteredState = frame.getBall();
			BallState ballState = BallState.builder()
					.withPos(filteredState.getPos())
					.withVel(filteredState.getVel())
					.withAcc(filteredState.getAcc())
					.withSpin(filteredState.getSpin())
					.build();

			simState.getSimulatedBall().setState(ballState);

			simState.setSimTime(frame.getTimestamp());
		}
		step();
	}


	/**
	 * @param simSpeed the simSpeed to set
	 */
	public void setSimSpeed(final double simSpeed)
	{
		this.simSpeed = simSpeed;
	}


	@Override
	public void onNewRefereeMsg(final SslGcRefereeMessage.Referee refMsg)
	{
		simState.setLatestRefereeMessage(refMsg);

		if (refMsg.getCommand() == SslGcRefereeMessage.Referee.Command.HALT
				&& refMsg.hasDesignatedPosition())
		{
			if (tHaltStart == 0)
			{
				tHaltStart = simState.getSimTime();
			}
			if ((simState.getSimTime() - tHaltStart) / 1e9 > 0.5)
			{
				IVector2 designatedPos = Vector2.fromXY(refMsg.getDesignatedPosition().getX(),
						refMsg.getDesignatedPosition().getY());
				if (simState.getSimulatedBall().getState().getPos().getXYVector().distanceTo(designatedPos) > 1)
				{
					ballTargetPos = Vector3.from2d(designatedPos, 0.0);
				}
			}
		} else
		{
			tHaltStart = 0;
		}
	}


	private void handleBotCount()
	{
		SslGcRefereeMessage.Referee refMsg = simState.getLatestRefereeMessage();
		if (refMsg != null && manageBotCount)
		{
			handleTeam(refMsg, refMsg.getBlue(), ETeamColor.BLUE);
			handleTeam(refMsg, refMsg.getYellow(), ETeamColor.YELLOW);
		}
	}


	private void handleTeam(
			SslGcRefereeMessage.Referee refMsg,
			SslGcRefereeMessage.Referee.TeamInfo teamInfo,
			ETeamColor team
	)
	{
		long numOnField = simState.getSimulatedBots().keySet().stream().filter(b -> b.getTeamColor() == team).count();
		long numToRemove = numOnField - teamInfo.getMaxAllowedBots();
		if (numToRemove > 0)
		{
			Set<BotID> botsToRemove = nearestBotsToInterchangePoints(refMsg, team, numToRemove);
			if (!botsToRemove.isEmpty())
			{
				log.debug("Team {} has {} too many robots. They will be unregistered.", team, numToRemove);
				botsToRemove.forEach(this::unregisterBot);
			}
		} else if (numToRemove < 0)
		{
			IVector2 interchangePos = findFreeInterchangePos(team);
			final BotID botID = nextUnassignedBotId(team);
			if (interchangePos != null && botID.isBot())
			{
				log.debug("Team {} has {} too few robots. They will be registered.", team, -numToRemove);
				registerBot(botID, Pose.from(interchangePos, 0), Vector3f.zero());
			}
		}
	}


	private boolean canRobotBeRemove(SslGcRefereeMessage.Referee refMsg, SimulatedBot bot)
	{
		if (refMsg.getCommand() == SslGcRefereeMessage.Referee.Command.HALT)
		{
			return true;
		}
		if (Math.abs(simState.getSimulatedBall().getState().getPos().x()) < 2000)
		{
			return false;
		}

		IVector2 pos = bot.getState().getPose().getPos();
		return Geometry.getField().withMargin(Geometry.getBotRadius()).isPointInShape(pos)
				&& Math.abs(pos.x()) <= 1000;
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
		for (int i = 0; i < AObjectID.BOT_ID_MAX; i++)
		{
			final BotID botId = BotID.createBotId(i, team);
			if (!simState.getSimulatedBots().containsKey(botId))
			{
				return botId;
			}
		}
		return BotID.noBot();
	}


	private Set<BotID> nearestBotsToInterchangePoints(SslGcRefereeMessage.Referee refMsg, ETeamColor team, long numBots)
	{
		return simState.getSimulatedBots().entrySet().stream()
				.filter(e -> e.getKey().getTeamColor() == team)
				.filter(e -> canRobotBeRemove(refMsg, e.getValue()))
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
		log.debug("New client");
		if (!running && simNetServer.getConnectedClientTeams().size() == 2)
		{
			log.debug("Two clients connected. Resuming simulation.");
			resume();
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


	private class BallPlacer implements IBallPlacer
	{
		@Override
		public void placeBall(final IVector3 pos, final IVector3 vel)
		{
			IBallTrajectory traj = Geometry.getBallFactory()
					.createTrajectoryFromKickedBallWithoutSpin(pos.getXYVector(), vel);

			var botId = simState.getSimulatedBots().values().stream()
					.min(Comparator.comparing(bot -> bot.getState().getPose().getPos().distanceToSqr(pos.getXYVector())))
					.map(SimulatedBot::getBotId)
					.orElse(BotID.noBot());
			simState.setLastKickEvent(
					new SimKickEvent(pos.getXYVector(), botId, simState.getSimTime(), pos.getXYVector(), 0,
							traj.getMilliStateAtTime(0)));

			simState.getSimulatedBall().setState(traj.getMilliStateAtTime(0));
			ballTargetPos = null;
		}
	}
}
