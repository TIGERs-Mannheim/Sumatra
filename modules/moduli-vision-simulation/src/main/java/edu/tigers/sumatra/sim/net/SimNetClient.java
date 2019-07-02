package edu.tigers.sumatra.sim.net;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.Referee;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.pose.Pose;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.sim.ISimulatorActionCallback;
import edu.tigers.sumatra.sim.dynamics.bot.SimBotAction;
import edu.tigers.sumatra.sim.dynamics.bot.SimBotState;
import edu.tigers.sumatra.sim.net.SimRequestOuterClass.SimRequest;
import edu.tigers.sumatra.vision.data.FilteredVisionBall;
import edu.tigers.sumatra.vision.data.FilteredVisionBot;
import edu.tigers.sumatra.vision.data.FilteredVisionFrame;


/**
 * The SimNetClient is the network interface to the simulation server on the client side
 */
public class SimNetClient
{
	private static final Logger log = Logger.getLogger(SimNetClient.class);
	
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
		if (startupTeamColors != null)
		{
			teamColors.addAll(startupTeamColors);
		} else
		{
			teamColors.addAll(Arrays.asList(defaultTeamColors));
		}
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
					botStates.put(botID,
							new SimBotState(
									ProtoToLocalMapper.mapPose(simState.getPose()),
									ProtoToLocalMapper.mapVector3(simState.getVel()),
									simState.getBarrierInterrupted()));
				}
			}
			
			final List<Map<BotID, SimBotAction>> actions = collectActions(simulatorActionCallbacks, botStates,
					simRequest.getTimestamp());
			Map<BotID, SimBotAction> actionMap = new HashMap<>();
			actions.forEach(actionMap::putAll);
			
			final List<SimBotActionOuterClass.SimBotAction> simBotActions = actionMap.entrySet().stream()
					.map(this::mapBotSimAction).collect(Collectors.toList());
			
			final SimResponseOuterClass.SimResponse simResponse = SimResponseOuterClass.SimResponse.newBuilder()
					.addAllAction(simBotActions)
					.build();
			simResponse.writeDelimitedTo(socket.getOutputStream());
			
			Referee.SSL_Referee refereeMessage = null;
			if (simRequest.getRefereeMessage() != null)
			{
				refereeMessage = Referee.SSL_Referee.parseFrom(simRequest.getRefereeMessage().toByteArray());
			}
			
			return new SimServerResponse(createFilteredFrame(simRequest), refereeMessage);
		} catch (IOException e)
		{
			log.warn("Could not read from simulation server", e);
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
		simulatorActionCallbacks.forEach(cb -> cb.updateConnectedBotList(botStates));
		return simulatorActionCallbacks.stream()
				.map(cb -> cb.nextSimBotActions(botStates, timestamp))
				.collect(Collectors.toList());
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
			FilteredVisionBot fBot = FilteredVisionBot.Builder.create()
					.withPos(pose.getPos())
					.withVel(vel.getXYVector())
					.withOrientation(pose.getOrientation())
					.withAVel(vel.z())
					.withId(botID)
					.withQuality(1.0)
					.build();
			filteredBots.add(fBot);
		}
		
		final SimState.SimBallState ballState = simRequest.getBallState();
		FilteredVisionBall filteredBall = FilteredVisionBall.Builder.create()
				.withPos(ProtoToLocalMapper.mapVector3(ballState.getPose()))
				.withVel(ProtoToLocalMapper.mapVector3(ballState.getVel()))
				.withAcc(ProtoToLocalMapper.mapVector3(ballState.getAcc()))
				.withLastVisibleTimestamp(simRequest.getTimestamp())
				.withIsChipped(ballState.getChipped())
				.withvSwitch(ballState.getVSwitchToRoll())
				.build();
		
		return FilteredVisionFrame.Builder.create()
				.withBots(filteredBots)
				.withBall(filteredBall)
				.withId(simRequest.getFrameId())
				.withTimestamp(simRequest.getTimestamp())
				.withKickEvent(ProtoToLocalMapper.mapKickEvent(simRequest.getLastKickEvent()))
				.withKickFitState(filteredBall)
				.build();
	}
	
	static class SimServerResponse
	{
		private final FilteredVisionFrame frame;
		private final Referee.SSL_Referee refereeMessage;
		
		
		public SimServerResponse(final FilteredVisionFrame frame, final Referee.SSL_Referee refereeMessage)
		{
			this.frame = frame;
			this.refereeMessage = refereeMessage;
		}
		
		
		public FilteredVisionFrame getFrame()
		{
			return frame;
		}
		
		
		public Referee.SSL_Referee getRefereeMessage()
		{
			return refereeMessage;
		}
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
