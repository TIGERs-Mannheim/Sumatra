package edu.tigers.sumatra.sim.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import com.google.protobuf.InvalidProtocolBufferException;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.sim.SimulatedBall;
import edu.tigers.sumatra.sim.SimulatedBot;
import edu.tigers.sumatra.sim.dynamics.bot.EDriveMode;
import edu.tigers.sumatra.sim.dynamics.bot.SimBotAction;
import edu.tigers.sumatra.sim.net.SimRefereeOuterClass.SimReferee;
import edu.tigers.sumatra.sim.net.SimRegisterOuterClass.SimRegister;
import edu.tigers.sumatra.sim.net.SimRequestOuterClass.SimRequest;
import edu.tigers.sumatra.sim.net.SimResponseOuterClass.SimResponse;
import edu.tigers.sumatra.thread.NamedThreadFactory;


/**
 * The SimNetServer is the network interface of the simulator on the server side
 */
public class SimNetServer
{
	private static final Logger log = Logger.getLogger(SimNetServer.class);
	
	@Configurable(defValue = "14242", comment = "The listen port of the simulation server")
	private static int port = 14242;
	
	static
	{
		ConfigRegistration.registerClass("user", SimNetServer.class);
	}
	
	private ExecutorService service;
	private final Listener listener = new Listener();
	private boolean running = false;
	
	private final List<Socket> clientSockets = new CopyOnWriteArrayList<>();
	private final Set<Socket> teamsStateSendTo = new HashSet<>();
	private final Map<ETeamColor, Socket> registeredTeams = new EnumMap<>(ETeamColor.class);
	
	
	private final List<ISimNetObserver> observers = new CopyOnWriteArrayList<>();
	
	
	public void start()
	{
		running = true;
		listener.start();
		service = Executors.newFixedThreadPool(2, new NamedThreadFactory("SimNetServer"));
		service.submit(listener);
	}
	
	
	public void stop()
	{
		if (!running)
		{
			return;
		}
		running = false;
		closeClientConnections();
		listener.stop();
		service.shutdownNow();
		try
		{
			boolean terminated = service.awaitTermination(1, TimeUnit.SECONDS);
			if (!terminated)
			{
				log.error("Could not terminate simulation listener");
			}
		} catch (InterruptedException e)
		{
			log.error("Interrupted while stopping sim listener");
			Thread.currentThread().interrupt();
		}
		service = null;
		clientSockets.clear();
	}
	
	
	private void closeClientConnections()
	{
		clientSockets.forEach(this::closeClientSocket);
		if (!clientSockets.isEmpty())
		{
			log.error("All client sockets should have been gone.");
		}
	}
	
	
	public void publish(edu.tigers.sumatra.sim.SimState simState)
	{
		if (!running)
		{
			return;
		}
		SimRequest.Builder stateBuilder = SimRequest.newBuilder()
				.setTimestamp(simState.getSimTime())
				.setFrameId(simState.getFrameId());
		
		if (simState.getLastKickEvent() != null)
		{
			stateBuilder.setLastKickEvent(LocalToProtoMapper.mapKickEvent(simState.getLastKickEvent()));
		}
		
		for (SimulatedBot bot : simState.getSimulatedBots().values())
		{
			stateBuilder.addBotState(SimState.SimBotState.newBuilder()
					.setBotId(LocalToProtoMapper.mapBotId(bot.getBotId()))
					.setPose(LocalToProtoMapper.mapPose(bot.getState().getPose()))
					.setVel(LocalToProtoMapper.mapVector3(bot.getState().getVel()))
					.setBarrierInterrupted(bot.getState().isBarrierInterrupted()));
		}
		
		SimulatedBall simulatedBall = simState.getSimulatedBall();
		stateBuilder.setBallState(SimState.SimBallState.newBuilder()
				.setPose(LocalToProtoMapper.mapVector3(simulatedBall.getState().getPos().getXYZVector()))
				.setVel(LocalToProtoMapper.mapVector3(simulatedBall.getState().getVel().getXYZVector()))
				.setAcc(LocalToProtoMapper.mapVector3(simulatedBall.getState().getAcc().getXYZVector()))
				.setChipped(simulatedBall.getState().isChipped())
				.setVSwitchToRoll(simulatedBall.getState().getvSwitchToRoll()));
		
		try
		{
			if (simState.getLatestRefereeMessage() != null)
			{
				SimReferee simReferee = SimReferee.parseFrom(simState.getLatestRefereeMessage().toByteArray());
				stateBuilder.setRefereeMessage(simReferee);
			}
		} catch (InvalidProtocolBufferException e)
		{
			log.warn("Could not convert referee message", e);
		}
		
		SimRequest state = stateBuilder.build();
		
		// send and receive should always be performed after each other per client
		// especially, receive must not be called for new clients, before send has been called
		teamsStateSendTo.clear();
		teamsStateSendTo.addAll(clientSockets);
		service.submit(() -> sendToClients(state));
	}
	
	
	private void sendToClients(final SimRequest state)
	{
		for (Socket socket : teamsStateSendTo)
		{
			try
			{
				state.writeDelimitedTo(socket.getOutputStream());
			} catch (IOException e)
			{
				log.warn("Could not write state to client", e);
				closeClientSocket(socket);
			}
		}
	}
	
	
	public Map<BotID, SimBotAction> receive()
	{
		Map<BotID, SimBotAction> actions = new HashMap<>();
		for (Socket socket : teamsStateSendTo)
		{
			if (!clientSockets.contains(socket))
			{
				// client disconnected in the meantime
				continue;
			}
			try
			{
				final SimResponse simResponse = SimResponse.parseDelimitedFrom(socket.getInputStream());
				if (simResponse != null)
				{
					actions.putAll(mapActions(simResponse.getActionList()));
				}
			} catch (IOException e)
			{
				log.warn("Could not read from client", e);
				closeClientSocket(socket);
			}
		}
		return actions;
	}
	
	
	private Map<BotID, SimBotAction> mapActions(List<SimBotActionOuterClass.SimBotAction> actions)
	{
		Map<BotID, SimBotAction> actionMap = new HashMap<>();
		for (SimBotActionOuterClass.SimBotAction action : actions)
		{
			SimBotAction.Builder builder = SimBotAction.Builder.create();
			if (action.hasTargetPos())
			{
				builder.targetPos(ProtoToLocalMapper.mapVector3(action.getTargetPos()));
			}
			if (action.hasTargetVelLocal())
			{
				builder.targetVelLocal(ProtoToLocalMapper.mapVector3(action.getTargetVelLocal()));
			}
			if (action.hasTargetWheelVel())
			{
				builder.targetWheelVel(ProtoToLocalMapper.mapVectorN(action.getTargetWheelVel()));
			}
			if (action.hasPrimaryDirection())
			{
				builder.primaryDirection(ProtoToLocalMapper.mapVector2(action.getPrimaryDirection()));
			}
			builder.modeXY(EDriveMode.values()[action.getModeXy().ordinal()]);
			builder.modeW(EDriveMode.values()[action.getModeW().ordinal()]);
			builder.driveLimits(ProtoToLocalMapper.mapDriveLimits(action.getDriveLimits()));
			builder.strictVelocityLimit(action.getStrictVelocityLimit());
			builder.kickSpeed(action.getKickSpeed());
			builder.chip(action.getChip());
			builder.disarm(action.getDisarm());
			builder.dribbleRpm(action.getDribbleRpm());
			actionMap.put(ProtoToLocalMapper.mapBotId(action.getBotId()), builder.build());
		}
		return actionMap;
	}
	
	
	private void closeClientSocket(final Socket socket)
	{
		try
		{
			socket.close();
		} catch (IOException e)
		{
			log.warn("Could not close client connection", e);
		}
		registeredTeams.values().removeIf(s -> s == socket);
		clientSockets.removeIf(Socket::isClosed);
	}
	
	private class Listener implements Runnable
	{
		private ServerSocket serverSocket;
		
		
		@Override
		public void run()
		{
			listen(serverSocket);
		}
		
		
		private void listen(final ServerSocket serverSocket)
		{
			log.info("Listening for remote simulation clients");
			while (running)
			{
				try
				{
					final Socket socket = serverSocket.accept();
					socket.setTcpNoDelay(true);
					onNewClient(socket);
				} catch (IOException e)
				{
					if (running)
					{
						log.warn("Could not accept connection for simulation client", e);
					}
				}
			}
			log.info("Stop listening for remote simulation clients");
		}
		
		
		void start()
		{
			try
			{
				serverSocket = new ServerSocket(port);
			} catch (IOException e)
			{
				log.error("Could not listen for simulation clients on port " + port, e);
			}
		}
		
		
		void stop()
		{
			if (serverSocket != null)
			{
				try
				{
					serverSocket.close();
				} catch (IOException e)
				{
					log.error("Could not close server socket", e);
				}
			}
		}
		
		
		private void onNewClient(Socket socket) throws IOException
		{
			SimRegister simRegister = SimRegister.parseDelimitedFrom(socket.getInputStream());
			for (ETeamColor teamColor : registeredTeams.keySet())
			{
				if (simRegister.getTeamColorList().contains(LocalToProtoMapper.mapTeamColor(teamColor)))
				{
					log.warn("Someone tried to register with team color " + teamColor + ", but that is already registered");
					socket.close();
					return;
				}
			}
			log.info("Someone registered for team colors " + simRegister.getTeamColorList());
			simRegister.getTeamColorList().stream().map(ProtoToLocalMapper::mapTeamColor)
					.forEach(t -> registeredTeams.put(t, socket));
			
			clientSockets.add(socket);
			observers.forEach(ISimNetObserver::onNewClient);
		}
	}
	
	
	public Set<ETeamColor> getConnectedClientTeams()
	{
		return registeredTeams.keySet();
	}
	
	
	public void addObserver(ISimNetObserver o)
	{
		observers.add(o);
	}
	
	
	public void removeObserver(ISimNetObserver o)
	{
		observers.remove(o);
	}
	
	public interface ISimNetObserver
	{
		/**
		 * A new client connected
		 */
		void onNewClient();
	}
}
