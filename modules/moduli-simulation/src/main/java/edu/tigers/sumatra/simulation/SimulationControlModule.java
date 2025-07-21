/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.simulation;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import com.github.g3force.configurable.IConfigClient;
import com.github.g3force.configurable.IConfigObserver;
import edu.tigers.sumatra.bot.State;
import edu.tigers.sumatra.cam.SSLVisionCam;
import edu.tigers.sumatra.filter.DataSync;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.BotMath;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.pose.Pose;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.moduli.AModule;
import edu.tigers.sumatra.sim.ISimulatorActionCallback;
import edu.tigers.sumatra.sim.SimulatedBot;
import edu.tigers.sumatra.sim.dynamics.bot.EDriveMode;
import edu.tigers.sumatra.sim.dynamics.bot.SimBotAction;
import edu.tigers.sumatra.sim.dynamics.bot.SimBotState;
import edu.tigers.sumatra.simulation.SslSimulationRobotControl.MoveLocalVelocity;
import edu.tigers.sumatra.simulation.SslSimulationRobotControl.RobotCommand;
import edu.tigers.sumatra.simulation.SslSimulationRobotControl.RobotControl;
import edu.tigers.sumatra.simulation.SslSimulationRobotControl.RobotMoveCommand;
import edu.tigers.sumatra.simulation.SslSimulationRobotFeedback.RobotControlResponse;
import edu.tigers.sumatra.simulation.SslSimulationRobotFeedback.RobotFeedback;
import edu.tigers.sumatra.vision.AVisionFilter;
import edu.tigers.sumatra.vision.IBallPlacer;
import edu.tigers.sumatra.wp.IWorldFrameObserver;
import edu.tigers.sumatra.wp.WorldInfoCollector;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.util.Strings;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.stream.Collectors;


/**
 * This modules connects to the ssl-simulation-protocol and sends robot and simulation control commands.
 */
@Log4j2
public class SimulationControlModule extends AModule
{
	private static final double BUFFER_HORIZON = 0.1;

	@Configurable(defValue = "10300", comment = "The control port")
	private static int controlPort = 10300;
	@Configurable(defValue = "10301", comment = "The blue team port")
	private static int blueTeamPort = 10301;
	@Configurable(defValue = "10302", comment = "The yellow team port")
	private static int yellowTeamPort = 10302;

	@Configurable(defValue = "true", comment = "Enable control of the blue team")
	private static boolean controlBlueTeam = true;
	@Configurable(defValue = "true", comment = "Enable control of the yellow team")
	private static boolean controlYellowTeam = true;

	@Configurable(defValue = "0.005")
	private static double pXyGain = 0.005;
	@Configurable(defValue = "0.5")
	private static double pXyMax = 0.5;
	@Configurable(defValue = "5.0")
	private static double pRotGain = 5;
	@Configurable(defValue = "1.0")
	private static double pRotMax = 1;

	@Configurable(defValue = "0.05")
	private static double delay = 0.05;
	@Configurable(comment = "Max allowed bot speed in stop phases", defValue = "1.3")
	private static double stopSpeedLimit = 1.3;

	@Configurable(defValue = "150.0", comment = "Max allowed divergence from actual position, before resetting the state")
	private static double maxDivergence = 150;

	@Configurable(defValue = "")
	private static String staticHostname = "";

	static
	{
		ConfigRegistration.registerClass("user", SimulationControlModule.class);
	}

	private final ConfigObserver configObserver = new ConfigObserver();
	private final VisionObserver visionObserver = new VisionObserver();
	private final Collection<ISimulatorActionCallback> simulatorActionCallbacks = new ArrayList<>();

	private final Map<BotID, SimulatedBot> simulatedBots = new ConcurrentSkipListMap<>();
	private final Map<BotID, RobotFeedback> robotFeedbackMap = new ConcurrentSkipListMap<>();
	private final Map<BotID, Long> lastRobotFeedbackMap = new ConcurrentSkipListMap<>();
	private final Map<BotID, DataSync<State>> stateSyncBuffers = new ConcurrentSkipListMap<>();
	private final Map<BotID, Long> collidingStartMap = new ConcurrentSkipListMap<>();

	private SimControlClient controlClient;
	private final Map<ETeamColor, SimTeamClient> teamClients = new EnumMap<>(ETeamColor.class);

	private String hostname;
	private long lastTimeReportedNoFeedback;


	@Override
	public void initModule()
	{
		controlClient = new SimControlClient();
		Arrays.stream(ETeamColor.yellowBlueValues()).forEach(c -> teamClients.put(c, new SimTeamClient()));
	}


	@Override
	public void deinitModule()
	{
		controlClient = null;
		teamClients.clear();
	}


	@Override
	public void startModule()
	{
		ConfigRegistration.registerConfigurableCallback("user", configObserver);
		SumatraModel.getInstance().getModule(WorldInfoCollector.class).addObserver(visionObserver);
		SumatraModel.getInstance().getModule(AVisionFilter.class).setBallPlacer(new BallPlacer());
	}


	@Override
	public void stopModule()
	{
		ConfigRegistration.unregisterConfigurableCallback("user", configObserver);
		SumatraModel.getInstance().getModule(WorldInfoCollector.class).removeObserver(visionObserver);

		controlClient.stop();
		teamClients.values().forEach(SimTeamClient::stop);
		simulatedBots.clear();
		robotFeedbackMap.clear();
		lastRobotFeedbackMap.clear();
		stateSyncBuffers.clear();
	}


	private void reconnect()
	{
		lastTimeReportedNoFeedback = System.nanoTime();
		simulatedBots.clear();
		robotFeedbackMap.clear();
		lastRobotFeedbackMap.clear();
		stateSyncBuffers.clear();

		if (!staticHostname.isEmpty())
		{
			hostname = staticHostname;
		}
		if (Strings.isBlank(hostname))
		{
			log.debug("No hostname set. Will not connect to simulator.");
			return;
		}

		log.info("Connecting to simulator at '{}'", hostname);
		controlClient.stop();
		teamClients.values().forEach(SimTeamClient::stop);
		controlClient.start(hostname, controlPort);
		teamClients.forEach((t, c) -> c.setResponseConsumer(r -> onRobotFeedback(t, r)));
		if (controlBlueTeam)
		{
			teamClients.get(ETeamColor.BLUE).start(hostname, blueTeamPort);
		}
		if (controlYellowTeam)
		{
			teamClients.get(ETeamColor.YELLOW).start(hostname, yellowTeamPort);
		}
	}


	private void onRobotFeedback(ETeamColor teamColor, RobotControlResponse robotControlResponse)
	{
		for (var feedback : robotControlResponse.getFeedbackList())
		{
			var botId = BotID.createBotId(feedback.getId(), teamColor);
			robotFeedbackMap.put(botId, feedback);
			lastRobotFeedbackMap.put(botId, System.nanoTime());
		}
	}


	private void sendBotCommands(Map<BotID, SimBotState> botFilterStates, Map<BotID, SimBotAction> actions,
			long timestamp)
	{
		Map<ETeamColor, RobotControl.Builder> robotCommandMap = new EnumMap<>(ETeamColor.class);
		robotCommandMap.put(ETeamColor.BLUE, RobotControl.newBuilder());
		robotCommandMap.put(ETeamColor.YELLOW, RobotControl.newBuilder());
		for (var e : actions.entrySet())
		{
			var robotCommand = RobotCommand.newBuilder();
			var botId = e.getKey();
			var simulatedBot = simulatedBots.get(botId);
			var botFilterState = botFilterStates.get(botId);
			var action = e.getValue();

			if (botCollidingWithOtherBot(botFilterStates, botId))
			{
				var collidingSince = collidingStartMap.computeIfAbsent(botId, b -> timestamp);
				if ((timestamp - collidingSince) / 1e9 > 0.2)
				{
					simulatedBot.setState(botFilterState);
					collidingStartMap.remove(botId);
				}
			} else
			{
				collidingStartMap.remove(botId);
			}

			IVector3 botVel = robotControl(simulatedBot, botFilterState, action, timestamp);
			IVector2 botVelXy = botVel.getXYVector().turnNew(-AngleMath.DEG_090_IN_RAD);
			robotCommand.setId(botId.getNumber());
			robotCommand.setMoveCommand(
					RobotMoveCommand.newBuilder().setLocalVelocity(
							MoveLocalVelocity.newBuilder()
									.setForward(((float) botVelXy.x()))
									.setLeft((float) botVelXy.y())
									.setAngular((float) botVel.z())
									.build()));
			robotCommand.setDribblerSpeed((float) action.getDribbleRpm());
			if (action.isDisarm())
			{
				robotCommand.setKickSpeed(0);
			} else if (action.isChip())
			{
				double chipAngle = 45;
				robotCommand.setKickSpeed((float) action.getKickSpeed() / 1000);
				robotCommand.setKickAngle((float) chipAngle);
			} else
			{
				robotCommand.setKickSpeed((float) action.getKickSpeed() / 1000);
				robotCommand.setKickAngle(0);
			}

			robotCommandMap.get(botId.getTeamColor()).addRobotCommands(robotCommand);
		}

		robotCommandMap
				.forEach((teamColor, robotControl) -> teamClients.get(teamColor).sendRobotControl(robotControl.build()));
	}


	private boolean botCollidingWithOtherBot(Map<BotID, SimBotState> botFilterStates, BotID botID)
	{
		var otherBotStates = botFilterStates.entrySet().stream()
				.filter(b -> !b.getKey().equals(botID))
				.map(Map.Entry::getValue)
				.toList();
		var botState = botFilterStates.get(botID);
		double margin = Geometry.getBotRadius() * 2 + 10;
		return otherBotStates.stream()
				.anyMatch(s -> s.getPose().getPos().distanceTo(botState.getPose().getPos()) < margin);
	}


	private IVector3 robotControl(SimulatedBot simulatedBot, SimBotState botFilterState, SimBotAction action,
			long timestamp)
	{
		if (action.getModeXY() == EDriveMode.OFF)
		{
			simulatedBot.setState(botFilterState);
			return Vector3.zero();
		}
		IVector3 ctrl = robotControl(simulatedBot, botFilterState, timestamp);
		if (action.isStrictVelocityLimit() && ctrl.getLength2() > stopSpeedLimit)
		{
			return Vector3.from2d(ctrl.getXYVector().scaleToNew(stopSpeedLimit), ctrl.z());
		}
		return ctrl;
	}


	private Vector3 robotControl(SimulatedBot simulatedBot, SimBotState botFilterState, long timestamp)
	{
		var refVel = simulatedBot.getVelLocal().getXYVector().multiplyNew(1e-3);
		var refRot = simulatedBot.getVelLocal().z();
		var sync = stateSyncBuffers.get(simulatedBot.getBotId());
		var pastTimestamp = timestamp - (long) (1e9 * delay);
		var refState = sync.get(pastTimestamp)
				.map(p -> p.interpolate(pastTimestamp))
				.or(() -> sync.getOldest().map(DataSync.DataStore::getData));
		var refPos = refState.map(State::getPos).orElse(simulatedBot.getState().getPose().getPos());
		var refOri = (double) refState.map(State::getOrientation)
				.orElse(simulatedBot.getState().getPose().getOrientation());

		var actPos = botFilterState.getPose().getPos();
		var actOri = botFilterState.getPose().getOrientation();
		var errPos = refPos.subtractNew(actPos);

		if (errPos.getLength2() > maxDivergence)
		{
			simulatedBot.setState(botFilterState);
			return Vector3.from2d(refVel, refOri);
		}

		var errVel = BotMath.convertGlobalBotVector2Local(errPos,
				simulatedBot.getState().getPose().getOrientation());
		var errRot = AngleMath.difference(refOri, actOri);

		var ctlVel = cap(errVel.multiplyNew(pXyGain), pXyMax);
		var ctlRot = SumatraMath.cap(errRot * pRotGain, -pRotMax, pRotMax);

		return Vector3.from2d(
				refVel.addNew(ctlVel),
				refRot + ctlRot
		);
	}


	private IVector2 cap(IVector2 v, double max)
	{
		if (v.getLength() > max)
		{
			return v.scaleToNew(max);
		}
		return v;
	}


	public void addSimulatorActionCallback(ISimulatorActionCallback cb)
	{
		simulatorActionCallbacks.add(cb);
	}


	public void removeSimulatorActionCallback(ISimulatorActionCallback cb)
	{
		simulatorActionCallbacks.remove(cb);
	}


	private class VisionObserver implements IWorldFrameObserver
	{
		long lastTimestamp;


		private double getDt(long newTimestamp)
		{
			double dt = (newTimestamp - lastTimestamp) / 1e9;
			if (dt < 0 || dt > 1)
			{
				return 0;
			}
			return dt;
		}


		@Override
		public void onNewWorldFrame(WorldFrameWrapper wfw)
		{
			updateHostname();
			cleanupRobotFeedback();

			Map<BotID, SimBotState> botFilterStates = new HashMap<>();
			for (var bot : wfw.getSimpleWorldFrame().getBots().values())
			{
				State filteredState = bot.getFilteredState().orElse(null);
				if (filteredState == null || botOffline(bot.getBotId()))
				{
					continue;
				}
				var feedback = robotFeedbackMap.get(bot.getBotId());
				boolean barrierInterrupted = Optional.ofNullable(feedback)
						.map(RobotFeedback::getDribblerBallContact)
						.orElse(false);

				Long lastUpdate = lastRobotFeedbackMap.getOrDefault(bot.getBotId(), 0L);
				SimBotState simBotState = new SimBotState(
						Pose.from(filteredState.getPos(), filteredState.getOrientation()),
						Vector3.from2d(filteredState.getVel2().multiplyNew(1000), filteredState.getAngularVel()),
						barrierInterrupted,
						lastUpdate
				);
				botFilterStates.put(bot.getBotId(), simBotState);
				var simBot = simulatedBots.computeIfAbsent(bot.getBotId(), botId -> new SimulatedBot(botId, simBotState));
				simBot.setBarrierInterrupted(barrierInterrupted);
				simBot.setLastUpdate(lastUpdate);
			}

			simulatedBots.keySet().removeIf(id -> !botFilterStates.containsKey(id));

			Map<BotID, SimBotState> botStates = new HashMap<>();
			simulatedBots.forEach((botId, bot) -> botStates.put(botId, bot.getState()));

			simulatorActionCallbacks.forEach(cb -> cb.updateConnectedBotList(simulatedBots.keySet()));
			var actions = simulatorActionCallbacks.stream()
					.map(cb -> cb.nextSimBotActions(botStates, wfw.getTimestamp()))
					.flatMap(map -> map.entrySet().stream())
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

			// Update ball position for all bots
			simulatedBots.values().forEach(b -> b.setBallPos(wfw.getSimpleWorldFrame().getBall().getPos()));
			// Update bot action for all bots
			actions.forEach((botId, action) -> simulatedBots.get(botId).setAction(action));

			// Simulate all robots
			double dt = getDt(wfw.getTimestamp());
			actions.forEach((botId, action) -> simulateBot(simulatedBots.get(botId), dt));
			actions.keySet().forEach(botId -> updateStateSyncBuffer(botId, wfw));

			sendBotCommands(botFilterStates, actions, wfw.getTimestamp());

			lastTimestamp = wfw.getTimestamp();
		}


		private void cleanupRobotFeedback()
		{
			double timeout = 0.5;

			for (var entry : lastRobotFeedbackMap.entrySet())
			{
				double diff = (System.nanoTime() - entry.getValue()) / 1e9;
				if (diff > timeout)
				{
					log.warn("Lost robot feedback from {} after {}", entry.getKey(), diff);
					lastRobotFeedbackMap.remove(entry.getKey());
				} else if (diff < 0)
				{
					log.warn("last robot feedback is in the future: {} < 0", diff);
				}
			}

			long now = System.nanoTime();
			if (lastRobotFeedbackMap.isEmpty() && (now - lastTimeReportedNoFeedback) / 1e9 > 5)
			{
				log.warn("No robot feedback for {}s", timeout);
				lastTimeReportedNoFeedback = now;
			}
		}


		private void updateStateSyncBuffer(BotID botId, WorldFrameWrapper wfw)
		{
			var dataSync = stateSyncBuffers.computeIfAbsent(botId, b -> new DataSync<>(BUFFER_HORIZON));
			var simState = simulatedBots.get(botId).getState();
			var state = State.of(simState.getPose(), simState.getVel());
			dataSync.add(wfw.getTimestamp(), state);
		}


		private boolean botOffline(BotID botID)
		{
			if (botID.getTeamColor() == ETeamColor.BLUE)
			{
				return !controlBlueTeam;
			} else if (botID.getTeamColor() == ETeamColor.YELLOW)
			{
				return !controlYellowTeam;
			}
			return true;
		}


		private void simulateBot(SimulatedBot bot, double dt)
		{
			double stepDt = 0.001;
			int n = (int) Math.min(100, dt / stepDt);
			for (int i = 0; i < n; i++)
			{
				bot.dynamics(stepDt);
			}
			double remainingDt = dt - stepDt * n;
			if (remainingDt > 0)
			{
				bot.dynamics(remainingDt);
			}
		}


		private void updateHostname()
		{
			SumatraModel.getInstance().getModuleOpt(SSLVisionCam.class)
					.flatMap(SSLVisionCam::getVisionAddress)
					.map(InetAddress::getHostAddress)
					.ifPresent(visionAddress -> {
						if (staticHostname.isEmpty() && !visionAddress.equals(hostname))
						{
							hostname = visionAddress;
							reconnect();
						}
					});
		}
	}


	private class ConfigObserver implements IConfigObserver
	{
		@Override
		public void afterApply(IConfigClient configClient)
		{
			reconnect();
		}
	}

	private class BallPlacer implements IBallPlacer
	{
		@Override
		public void placeBall(IVector3 pos, IVector3 vel)
		{
			var teleBall = SslSimulationControl.TeleportBall.newBuilder();
			teleBall.setX((float) pos.x() / 1000);
			teleBall.setY((float) pos.y() / 1000);
			teleBall.setZ((float) pos.z() / 1000);
			teleBall.setVx((float) vel.x() / 1000);
			teleBall.setVy((float) vel.y() / 1000);
			teleBall.setVz((float) vel.z() / 1000);
			var control = SslSimulationControl.SimulatorControl.newBuilder();
			control.setTeleportBall(teleBall);
			var command = SslSimulationControl.SimulatorCommand.newBuilder();
			command.setControl(control);
			controlClient.sendControlCommand(command.build());
		}
	}
}
