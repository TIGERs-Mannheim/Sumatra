/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.sim.net;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ball.BallState;
import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.pose.Pose;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector3f;
import edu.tigers.sumatra.referee.proto.SslGcRefereeMessage;
import edu.tigers.sumatra.sim.ISimulatorActionCallback;
import edu.tigers.sumatra.sim.SimKickEvent;
import edu.tigers.sumatra.sim.dynamics.bot.SimBotAction;
import edu.tigers.sumatra.sim.dynamics.bot.SimBotState;
import edu.tigers.sumatra.sim.net.SimRequestOuterClass.SimRequest;
import edu.tigers.sumatra.vision.data.FilteredVisionBall;
import edu.tigers.sumatra.vision.data.FilteredVisionBot;
import edu.tigers.sumatra.vision.data.FilteredVisionFrame;
import edu.tigers.sumatra.vision.data.FilteredVisionKick;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


/**
 * The SimNetClient is the network interface to the simulation server on the client side
 */
public class SimNetClient
{
	private static final Logger log = LogManager.getLogger(SimNetClient.class);

	@Configurable(defValue = "14242", comment = "The port of the simulation server to connect to")
	private static int port = 14242;

	@Configurable(defValue = "localhost", comment = "The host of the simulation server to connect to")
	private static String host = "localhost";

	@Configurable(defValue = "YELLOW;BLUE", comment = "The team color that will be controlled by this instance")
	private static ETeamColor[] defaultTeamColors = { ETeamColor.YELLOW, ETeamColor.BLUE };

	private static List<ETeamColor> startupTeamColors = null;
	private static String startupHost = null;

	static
	{
		ConfigRegistration.registerClass("user", SimNetClient.class);
	}

	private Socket socket;
	private boolean connected = false;
	private boolean active = false;
	private List<ETeamColor> teamColors = new ArrayList<>();


	public SimNetClient()
	{
		teamColors.addAll(Objects.requireNonNullElseGet(startupTeamColors, () -> Arrays.asList(defaultTeamColors)));
	}


	private String getHost()
	{
		if (startupHost != null)
		{
			return startupHost;
		}
		return host;
	}


	public void start()
	{
		int tries = 0;
		while (active)
		{
			try
			{
				tries++;
				connect();
				return;
			} catch (IOException e)
			{
				if (tries == 10)
				{
					log.warn("Could not connect to sim server", e);
				}
				try
				{
					Thread.sleep(100);
				} catch (InterruptedException e1)
				{
					Thread.currentThread().interrupt();
				}
			}
		}
	}


	private void connect() throws IOException
	{
		socket = new Socket(getHost(), port);
		socket.setTcpNoDelay(true);
		connected = true;

		log.info("Connected to sim server");

		final SimRegisterOuterClass.SimRegister.Builder simRegister = SimRegisterOuterClass.SimRegister.newBuilder();
		for (ETeamColor teamColor : teamColors)
		{
			simRegister.addTeamColor(LocalToProtoMapper.mapTeamColor(teamColor))
					.build();
		}
		simRegister.build().writeDelimitedTo(socket.getOutputStream());
	}


	public void stop()
	{
		connected = false;
		if (socket != null)
		{
			try
			{
				socket.close();
			} catch (IOException e)
			{
				log.error("Could not close simulation server socket", e);
			}
			socket = null;
		}
	}


	public SimServerResponse accept(Collection<ISimulatorActionCallback> simulatorActionCallbacks)
	{
		if (!connected)
		{
			return null;
		}
		try
		{
			final SimRequest simRequest = SimRequest.parseDelimitedFrom(socket.getInputStream());

			if (simRequest == null)
			{
				return null;
			}
			Map<BotID, SimBotState> botStates = new HashMap<>();
			for (SimState.SimBotState simState : simRequest.getBotStateList())
			{
				final BotID botID = ProtoToLocalMapper.mapBotId(simState.getBotId());
				if (teamColors.contains(botID.getTeamColor()))
				{
					var vel = ProtoToLocalMapper.mapVector3(simState.getVel());
					botStates.put(botID,
							new SimBotState(
									ProtoToLocalMapper.mapPose(simState.getPose()),
									Vector3f.from2d(vel.getXYVector().multiplyNew(1000), vel.z()),
									simState.getBarrierInterrupted(),
									0));
				}
			}

			final List<Map<BotID, SimBotAction>> actions = collectActions(simulatorActionCallbacks, botStates,
					simRequest.getTimestamp());
			Map<BotID, SimBotAction> actionMap = new HashMap<>();
			actions.forEach(actionMap::putAll);

			final List<SimBotActionOuterClass.SimBotAction> simBotActions = actionMap.entrySet().stream()
					.map(this::mapBotSimAction).toList();

			final SimResponseOuterClass.SimResponse simResponse = SimResponseOuterClass.SimResponse.newBuilder()
					.addAllAction(simBotActions)
					.build();
			simResponse.writeDelimitedTo(socket.getOutputStream());

			SslGcRefereeMessage.Referee refereeMessage = null;
			if (simRequest.hasRefereeMessage())
			{
				refereeMessage = SslGcRefereeMessage.Referee.parseFrom(simRequest.getRefereeMessage().toByteArray());
			}

			return new SimServerResponse(createFilteredFrame(simRequest), refereeMessage);
		} catch (IOException e)
		{
			if ("Broken pipe".equals(e.getMessage()))
			{
				log.debug("Disconnected from server");
			} else
			{
				log.warn("Could not read from simulation server", e);
			}
			stop();
		}
		return null;
	}


	private SimBotActionOuterClass.SimBotAction mapBotSimAction(final Map.Entry<BotID, SimBotAction> entry)
	{
		final BotID botID = entry.getKey();
		final SimBotAction action = entry.getValue();
		final SimBotActionOuterClass.SimBotAction.Builder builder = SimBotActionOuterClass.SimBotAction.newBuilder()
				.setBotId(LocalToProtoMapper.mapBotId(botID))
				.setModeXyValue(action.getModeXY().ordinal())
				.setModeWValue(action.getModeW().ordinal())
				.setDriveLimits(LocalToProtoMapper.mapDriveLimits(action.getDriveLimits()))
				.setStrictVelocityLimit(action.isStrictVelocityLimit())
				.setKickSpeed(action.getKickSpeed())
				.setChip(action.isChip())
				.setDisarm(action.isDisarm())
				.setDribbleRpm(action.getDribbleRpm());

		if (action.getTargetPos() != null)
		{
			builder.setTargetPos(LocalToProtoMapper.mapVector3(action.getTargetPos()));
		}
		if (action.getTargetVelLocal() != null)
		{
			builder.setTargetVelLocal(LocalToProtoMapper.mapVector3(action.getTargetVelLocal()));
		}
		if (action.getTargetWheelVel() != null)
		{
			builder.setTargetWheelVel(LocalToProtoMapper.mapVectorN(action.getTargetWheelVel()));
		}
		if (action.getPrimaryDirection() != null)
		{
			builder.setPrimaryDirection(LocalToProtoMapper.mapVector2(action.getPrimaryDirection()));
		}
		return builder.build();
	}


	private List<Map<BotID, SimBotAction>> collectActions(
			Collection<ISimulatorActionCallback> simulatorActionCallbacks,
			final Map<BotID, SimBotState> botStates,
			final long timestamp)
	{
		simulatorActionCallbacks.forEach(cb -> cb.updateConnectedBotList(botStates.keySet()));
		return simulatorActionCallbacks.stream()
				.map(cb -> cb.nextSimBotActions(botStates, timestamp))
				.toList();
	}


	public boolean isConnected()
	{
		return active && connected;
	}


	private FilteredVisionFrame createFilteredFrame(final SimRequest simRequest)
	{
		List<FilteredVisionBot> filteredBots = new ArrayList<>();

		for (SimState.SimBotState botState : simRequest.getBotStateList())
		{
			BotID botID = ProtoToLocalMapper.mapBotId(botState.getBotId());
			Pose pose = ProtoToLocalMapper.mapPose(botState.getPose());
			IVector3 vel = ProtoToLocalMapper.mapVector3(botState.getVel());
			FilteredVisionBot fBot = FilteredVisionBot.builder()
					.withPos(pose.getPos())
					.withVel(vel.getXYVector())
					.withOrientation(pose.getOrientation())
					.withAngularVel(vel.z())
					.withBotID(botID)
					.withTimestamp(simRequest.getTimestamp())
					.withQuality(1.0)
					.build();
			filteredBots.add(fBot);
		}

		final SimState.SimBallState ballState = simRequest.getBallState();
		BallState bs = ProtoToLocalMapper.mapBallState(ballState);

		FilteredVisionBall filteredBall = FilteredVisionBall.builder()
				.withTimestamp(simRequest.getTimestamp())
				.withBallState(bs)
				.withLastVisibleTimestamp(simRequest.getTimestamp())
				.build();

		FilteredVisionKick filteredKick = null;

		if (simRequest.hasLastKickEvent())
		{
			SimKickEvent kick = ProtoToLocalMapper.mapKickEvent(simRequest.getLastKickEvent());

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
				.withId(simRequest.getFrameId())
				.withTimestamp(simRequest.getTimestamp())
				.withKick(filteredKick)
				.withShapeMap(new ShapeMap())
				.build();
	}


	record SimServerResponse(
			FilteredVisionFrame frame,
			SslGcRefereeMessage.Referee refereeMessage)
	{
	}


	/**
	 * Set team colors that should be used and override those that are configured in the user config
	 *
	 * @param startupTeamColors
	 */
	public static void setStartupTeamColors(final List<ETeamColor> startupTeamColors)
	{
		SimNetClient.startupTeamColors = startupTeamColors;
	}


	/**
	 * Set the host that should be used and override the host from the configurable
	 *
	 * @param startupHost
	 */
	public static void setStartupHost(final String startupHost)
	{
		SimNetClient.startupHost = startupHost;
	}


	public void setActive(final boolean active)
	{
		this.active = active;
	}


	public boolean isActive()
	{
		return active;
	}
}
