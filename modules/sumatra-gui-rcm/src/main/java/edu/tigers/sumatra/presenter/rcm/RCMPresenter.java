/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.presenter.rcm;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.Priority;
import org.apache.log4j.WriterAppender;
import org.apache.log4j.spi.LoggingEvent;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.instanceables.IInstanceableEnum;
import com.github.g3force.instanceables.InstanceableParameter;
import com.google.protobuf.Message;

import edu.tigers.bluetoothprotobuf.BluetoothPbLocal;
import edu.tigers.bluetoothprotobuf.IMessageGateway;
import edu.tigers.bluetoothprotobuf.IMessageObserver;
import edu.tigers.bluetoothprotobuf.IMessageType;
import edu.tigers.bluetoothprotobuf.MessageContainer;
import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.moduli.listenerVariables.ModulesState;
import edu.tigers.sumatra.Referee.SSL_Referee.Command;
import edu.tigers.sumatra.bot.IBot;
import edu.tigers.sumatra.botmanager.ABotManager;
import edu.tigers.sumatra.botmanager.IBotManagerObserver;
import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.ids.AObjectID;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.natives.NativesLoader;
import edu.tigers.sumatra.natives.OsDetector;
import edu.tigers.sumatra.proto.AiControlAvailableProtos;
import edu.tigers.sumatra.proto.BotActionCommandProtos;
import edu.tigers.sumatra.proto.BotColorIdProtos;
import edu.tigers.sumatra.proto.BotStatusProtos;
import edu.tigers.sumatra.proto.LifeSignProtos;
import edu.tigers.sumatra.proto.LogMessagesProtos;
import edu.tigers.sumatra.proto.ParamInstanceProtos;
import edu.tigers.sumatra.proto.RefereeMsgProtos.RefereeCommandSimple;
import edu.tigers.sumatra.rcm.ActionSender;
import edu.tigers.sumatra.rcm.CommandInterpreter;
import edu.tigers.sumatra.rcm.CommandInterpreterStub;
import edu.tigers.sumatra.rcm.EControllerType;
import edu.tigers.sumatra.rcm.EMessage;
import edu.tigers.sumatra.rcm.ICommandInterpreter;
import edu.tigers.sumatra.rcm.IRCMObserver;
import edu.tigers.sumatra.rcm.RcmActionMap.ERcmControllerConfig;
import edu.tigers.sumatra.referee.AReferee;
import edu.tigers.sumatra.referee.data.RefBoxRemoteControlFactory;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.thread.NamedThreadFactory;
import edu.tigers.sumatra.view.rcm.IMessagingGUIObserver;
import edu.tigers.sumatra.view.rcm.RCMPanel;
import edu.tigers.sumatra.views.ASumatraViewPresenter;
import edu.tigers.sumatra.views.ISumatraView;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;


/**
 * This class enables clients to send commands directly to the bots
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class RCMPresenter extends ASumatraViewPresenter implements IMessagingGUIObserver, IRCMObserver
{
	private static final Logger log = Logger.getLogger(RCMPresenter.class
			.getName());
	
	private static final long CLIENT_TIMEOUT = 10;
	
	private final BotManagerObserver botManagerObserver = new BotManagerObserver();
	
	// --- modules ---
	private ABotManager botManager = null;
	private AReferee refBox;
	
	private final Map<BotID, CommandInterpreter> botInterpreters = new HashMap<>();
	private final List<ActionSender> actionSenders = new LinkedList<>();
	private final Map<BotID, Long> botLastMessage = new HashMap<>();
	
	private final Map<String, Long> clients = new HashMap<>();
	private final LogMessagesSender logMessageSender = new LogMessagesSender();
	
	private ScheduledExecutorService scheduledExecutor;
	
	private final BotStatusProtos.BotStati.Builder botConnBuilder = BotStatusProtos.BotStati.newBuilder();
	
	private final DelayedBotStatiSender delayedBotStatiSender = new DelayedBotStatiSender();
	
	private final List<IMessageGateway> messageGateways = new ArrayList<>();
	private final BluetoothPbLocal btPbLocal = new BluetoothPbLocal(new MessageContainer(
			EMessage.values()));
	
	private final List<AControllerPresenter> controllerPresenterS = new ArrayList<>();
	private final RCMPanel rcmPanel;
	
	private final SwitchBallSync syncSwitchBot = new SwitchBallSync()
	{
	};
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	static
	{
		// set library path for jinput
		final String curDir = System.getProperty("user.dir");
		System.setProperty("net.java.games.input.librarypath",
				curDir + "/lib/native/" + NativesLoader.getDefaultFolderMap().get(OsDetector.detectOs()));
	}
	
	
	/**
	 * Initialize class
	 */
	public RCMPresenter()
	{
		MessageReceiver receiver = new MessageReceiver();
		btPbLocal.addObserver(receiver);
		
		messageGateways.add(btPbLocal);
		
		rcmPanel = new RCMPanel();
		rcmPanel.addObserver(this);
		rcmPanel.addMessgingGUIObserver(this);
		setUpController(false);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 */
	@Override
	public void setUpController(final boolean keepConnections)
	{
		final ControllerEnvironment cEnv = ControllerEnvironment.getDefaultEnvironment();
		final Controller[] cs = cEnv.getControllers();
		final List<Controller> controllers = new ArrayList<>(cs.length);
		for (final Controller controller : cs)
		{
			controllers.add(controller);
			log.info("Controller found: " + controller.getName());
		}
		
		List<ABot> bots = new ArrayList<>(controllerPresenterS.size());
		if (keepConnections)
		{
			for (AControllerPresenter cp : controllerPresenterS)
			{
				bots.add(cp.getActionSender().getCmdInterpreter().getBot());
			}
		}
		
		onStartStopButtonPressed(false);
		controllerPresenterS.clear();
		rcmPanel.clearControllerPanels();
		for (Controller controller : controllers)
		{
			addController(controller);
		}
		if (controllers.isEmpty())
		{
			log.info(
					"No controller found. For Linux: ls -l /dev/input/by-id/ for finding controller and sudo chmod o+r /dev/input/eventX to enable");
		}
		
		
		if (keepConnections)
		{
			onStartStopButtonPressed(true);
			for (int i = 0; (i < controllerPresenterS.size()) && (i < bots.size()); i++)
			{
				if (bots.get(i).getBotId().isBot())
				{
					changeBotAssignment(controllerPresenterS.get(i).getActionSender(), bots.get(i));
				}
			}
		}
	}
	
	
	private void addController(final Controller controller)
	{
		final Controller.Type type = controller.getType();
		AControllerPresenter presenter;
		if (type == Controller.Type.KEYBOARD)
		{
			presenter = new KeyboardPresenter(controller);
		} else if ((type == Controller.Type.GAMEPAD) || (type == Controller.Type.STICK))
		{
			presenter = new GamePadPresenter(controller);
		} else
		{
			return;
		}
		rcmPanel.addControllerPanel(controller.getName(), presenter.getPanel());
		controllerPresenterS.add(presenter);
	}
	
	
	/**
	 * Start or stop sending and polling.
	 * 
	 * @param activeState if start, false if stop
	 */
	@Override
	public void onStartStopButtonPressed(final boolean activeState)
	{
		// --- Start polling when start-button pressed ---
		if (activeState)
		{
			for (AControllerPresenter cP : controllerPresenterS)
			{
				ActionSender actionSender = new ActionSender(cP.getController().getName());
				actionSenders.add(actionSender);
				actionSender.addObserver(this);
				cP.startPolling(actionSender);
			}
			rcmPanel.startRcm();
		}
		// --- Stop polling when stop-button pressed ---
		else
		{
			for (final AControllerPresenter cP : controllerPresenterS)
			{
				cP.getActionSender().removeObserver(this);
				cP.getActionSender().setInterpreter(new CommandInterpreterStub());
				actionSenders.remove(cP.getActionSender());
				cP.stopPolling();
				cP.getPanel().setSelectedBot(BotID.noBot());
			}
			if (botManager != null)
			{
				refreshBotControllers(botManager.getBots().values());
			}
			rcmPanel.stopRcm();
		}
	}
	
	
	private void refreshBotControllers(Collection<ABot> bots)
	{
		for (ABot bot : bots)
		{
			if (!bot.getControlledBy().isEmpty())
			{
				bot.setControlledBy("");
			}
		}
	}
	
	
	@Override
	public void onReconnect(final boolean keepConnections)
	{
		List<ABot> bots = new ArrayList<>(controllerPresenterS.size());
		if (keepConnections)
		{
			for (AControllerPresenter cp : controllerPresenterS)
			{
				bots.add(cp.getActionSender().getCmdInterpreter().getBot());
			}
		}
		onStartStopButtonPressed(false);
		onStartStopButtonPressed(true);
		if (keepConnections)
		{
			for (int i = 0; (i < controllerPresenterS.size()) && (i < bots.size()); i++)
			{
				if (bots.get(i).getBotId().isBot())
				{
					changeBotAssignment(controllerPresenterS.get(i).getActionSender(), bots.get(i));
				}
			}
		}
	}
	
	
	@Override
	public void onBotUnassigned(final ActionSender actionSender)
	{
		for (AControllerPresenter cP : controllerPresenterS)
		{
			if (cP.getActionSender() == actionSender)
			{
				cP.getPanel().setSelectedBot(BotID.noBot());
				break;
			}
		}
	}
	
	
	@Override
	public void onNextBot(final ActionSender actionSender)
	{
		switchBot(actionSender, 1);
	}
	
	
	@Override
	public void onPrevBot(final ActionSender actionSender)
	{
		switchBot(actionSender, -1);
	}
	
	
	/**
	 * Try to switch given robot. If there is a free robot available, it will be returned.
	 * Else, the given robot will be
	 * returned
	 * 
	 * @param actionSender
	 * @param inc -1 or 1 (endless loop else...)
	 */
	public void switchBot(final ActionSender actionSender, final int inc)
	{
		synchronized (syncSwitchBot)
		{
			ICommandInterpreter interpreter = actionSender.getCmdInterpreter();
			ABot oldBot = null;
			if (interpreter != null)
			{
				oldBot = interpreter.getBot();
			}
			int initId = 0;
			if ((oldBot != null) && oldBot.getBotId().isBot())
			{
				initId = oldBot.getBotId().getNumberWithColorOffsetBS();
			}
			int idMax = (AObjectID.BOT_ID_MAX * 2) + 2;
			assert initId < idMax;
			assert initId >= 0;
			for (int i = 0; i < idMax; i++)
			{
				int id = (initId + (inc * i) + idMax) % idMax;
				BotID botId = BotID.createBotIdFromIdWithColorOffsetBS(id);
				ABot bot = botManager.getBots().get(botId);
				if (bot != null && ((bot != oldBot) && !bot.isBlocked()
						&& !bot.isHideFromRcm()))
				{
					updateBotOwnership(oldBot);
					changeBotAssignment(actionSender, bot);
					return;
				}
			}
		}
	}
	
	
	private void updateBotOwnership(ABot oldBot)
	{
		if ((oldBot != null) && !oldBot.getControlledBy().isEmpty())
		{
			oldBot.setControlledBy("");
		}
	}
	
	
	private void changeBotAssignment(final ActionSender actionSender,
			final ABot bot)
	{
		ICommandInterpreter interpreter = actionSender.getCmdInterpreter();
		CommandInterpreter newInterpreter = getInterpreter(bot);
		if (interpreter != null)
		{
			ConfigRegistration.applySpezis(interpreter, "rcm", "");
		}
		ConfigRegistration.applySpezis(newInterpreter, "rcm", "");
		for (ActionSender sender : actionSenders)
		{
			if ((sender.getCmdInterpreter() == newInterpreter) && (sender.getCmdInterpreter() != interpreter))
			{
				log.warn("The interpreter is still assigned to another ActionSender, which should not happen");
				return;
			}
		}
		bot.setControlledBy(actionSender.getIdentifier());
		
		for (AControllerPresenter cP : controllerPresenterS)
		{
			if (cP.getActionSender() == actionSender)
			{
				cP.getPanel().setSelectedBot(bot.getBotId());
				newInterpreter.setCompassThreshold(cP.getConfig().getConfigValues()
						.get(ERcmControllerConfig.DEADZONE));
				break;
			}
		}
		
		actionSender.setInterpreter(newInterpreter);
	}
	
	
	@Override
	public void onModuliStateChanged(final ModulesState state)
	{
		super.onModuliStateChanged(state);
		switch (state)
		{
			case ACTIVE:
				handleModuliActivation();
				break;
			case RESOLVED:
				handleModuliResolution();
				break;
			case NOT_LOADED:
			default:
				break;
		}
	}
	
	
	private void handleModuliActivation()
	{
		try
		{
			botManager = SumatraModel.getInstance().getModule(ABotManager.class);
			for (IBot bot : botManager.getBots().values())
			{
				addBot(bot);
			}
			botManager.addObserver(botManagerObserver);
			sendAllBotStati();
		} catch (ModuleNotFoundException err)
		{
			log.error("Botmanager module not found. Strange... " + err);
		}
		try
		{
			refBox = SumatraModel.getInstance().getModule(AReferee.class);
		} catch (ModuleNotFoundException err)
		{
			log.error("Referee module not found. Strange... " + err);
		}
		
		rcmPanel.start();
	}
	
	
	private void handleModuliResolution()
	{
		onStartStopButtonPressed(false);
		if (botManager != null)
		{
			botManager.removeObserver(botManagerObserver);
			removeAllBots();
		}
		sendAllBotStati();
		rcmPanel.stop();
	}
	
	
	private CommandInterpreter getInterpreter(final ABot bot)
	{
		CommandInterpreter interpreter = botInterpreters.get(bot.getBotId());
		if ((interpreter == null) || (interpreter.getBot() != bot))
		{
			interpreter = new CommandInterpreter(bot);
			botInterpreters.put(bot.getBotId(), interpreter);
		}
		return interpreter;
	}
	
	
	private void removeActiveBot(final String clientName)
	{
		for (ABot bot : botManager.getBots().values())
		{
			if (bot.getControlledBy().equals(clientName))
			{
				bot.setControlledBy("");
				BotStatusProtos.BotStatus.Builder botStatus = getBotStatusBuilder(bot.getBotId());
				if (botStatus != null)
				{
					botStatus.setBlocked(false);
					sendAllBotStati();
				}
				return;
			}
		}
	}
	
	
	private void sendAllBotStati()
	{
		for (IMessageGateway gateway : messageGateways)
		{
			gateway.sendMessage(EMessage.BOT_STATI, botConnBuilder.build().toByteArray());
		}
	}
	
	
	private void addBot(final IBot bot)
	{
		BotStatusProtos.BotStatus.Builder builder = BotStatusProtos.BotStatus.newBuilder();
		BotColorIdProtos.BotColorId.Builder idBuilder = BotColorIdProtos.BotColorId.newBuilder();
		idBuilder.setBotId(bot.getBotId().getNumber());
		idBuilder.setColor(BotColorIdProtos.BotColorId.Color.forNumber(bot.getColor().getId()));
		builder.setBotId(idBuilder.build());
		builder.setAvailable(true);
		builder.setConnected(true);
		builder.setControlledBy(bot.getControlledBy());
		builder.setBattery((int) (bot.getBatteryRelative() * 100));
		builder.setKicker((int) ((bot.getKickerLevel() / bot.getKickerLevelMax()) * 100));
		synchronized (botConnBuilder)
		{
			botConnBuilder.addStati(builder);
		}
	}
	
	
	private void removeAllBots()
	{
		synchronized (botConnBuilder)
		{
			botConnBuilder.clearStati();
		}
	}
	
	
	private BotStatusProtos.BotStatus.Builder getBotStatusBuilder(final BotID id)
	{
		for (BotStatusProtos.BotStatus.Builder botStatus : botConnBuilder.getStatiBuilderList())
		{
			if ((botStatus.getBotId().getBotId() == id.getNumber())
					&& botStatus.getBotId().getColor().name().equals(id.getTeamColor().name()))
			{
				return botStatus;
			}
		}
		return null;
	}
	
	
	@Override
	public void onConnect()
	{
		Logger.getRootLogger().addAppender(logMessageSender);
		btPbLocal.start();
		scheduledExecutor = Executors
				.newSingleThreadScheduledExecutor(new NamedThreadFactory("RCM_Cleanup"));
		scheduledExecutor.scheduleAtFixedRate(new DeleteOldClients(), 0, 4, TimeUnit.SECONDS);
		scheduledExecutor.scheduleAtFixedRate(new StopBotOnNoMessage(), 0, 800, TimeUnit.MILLISECONDS);
	}
	
	
	@Override
	public void onDisconnect()
	{
		if (scheduledExecutor != null)
		{
			scheduledExecutor.shutdown();
		}
		btPbLocal.stop();
		Logger.getRootLogger().removeAppender(logMessageSender);
		log.info("Messaging stopped");
	}
	
	
	@Override
	public Component getComponent()
	{
		return rcmPanel;
	}
	
	
	@Override
	public ISumatraView getSumatraView()
	{
		return rcmPanel;
	}
	
	
	private class DeleteOldClients implements Runnable
	{
		
		@Override
		public void run()
		{
			for (Map.Entry<String, Long> entry : clients.entrySet())
			{
				if ((System.nanoTime() - entry.getValue()) > TimeUnit.SECONDS.toNanos(CLIENT_TIMEOUT))
				{
					clients.remove(entry.getKey());
					removeActiveBot(entry.getKey());
					log.info("Client " + entry.getKey() + " removed");
				}
			}
		}
		
	}
	
	private class DelayedBotStatiSender implements Runnable
	{
		private final ScheduledExecutorService executor = Executors
				.newSingleThreadScheduledExecutor(new NamedThreadFactory(
						"DelayedBotStatiSender"));
		private Future<?> future = null;
		
		
		void send()
		{
			if (future != null)
			{
				future.cancel(false);
			}
			future = executor.schedule(this, 100, TimeUnit.MILLISECONDS);
		}
		
		
		@Override
		public void run()
		{
			try
			{
				for (IBot bot : botManager.getBots().values())
				{
					BotStatusProtos.BotStatus.Builder builder = getBotStatusBuilder(bot.getBotId());
					if (bot.isHideFromRcm() || (builder == null))
					{
						continue;
					}
					builder.setConnected(true);
					builder.setBattery((int) (bot.getBatteryRelative() * 100));
					builder.setKicker((int) ((bot.getKickerLevel() / bot.getKickerLevelMax()) * 100));
					if (bot.isBlocked())
					{
						builder.setControlledBy(bot.getControlledBy());
					}
				}
				sendAllBotStati();
			} catch (Exception e)
			{
				log.error("Could not send bot stati", e);
			}
		}
	}
	
	private interface SwitchBallSync
	{
	}
	
	private class LogMessagesSender extends WriterAppender
	{
		private LogMessagesSender()
		{
			setLayout(new PatternLayout("%d{ABSOLUTE} %-5p [%t|%c{1}] %m%n"));
		}
		
		
		@Override
		public void append(final LoggingEvent logEvent)
		{
			LogMessagesProtos.LogMessage.Builder builder = LogMessagesProtos.LogMessage.newBuilder();
			LogMessagesProtos.LogMessage.Level level;
			switch (logEvent.getLevel().toInt())
			{
				case org.apache.log4j.Level.TRACE_INT:
					level = LogMessagesProtos.LogMessage.Level.TRACE;
					break;
				case Priority.DEBUG_INT:
					level = LogMessagesProtos.LogMessage.Level.DEBUG;
					break;
				case Priority.INFO_INT:
					level = LogMessagesProtos.LogMessage.Level.INFO;
					break;
				case Priority.WARN_INT:
					level = LogMessagesProtos.LogMessage.Level.WARN;
					break;
				case Priority.ERROR_INT:
					level = LogMessagesProtos.LogMessage.Level.ERROR;
					break;
				case Priority.FATAL_INT:
					level = LogMessagesProtos.LogMessage.Level.FATAL;
					break;
				default:
					log.warn("Unknown log level prio: " + logEvent.getLevel());
					return;
			}
			builder.setLevel(level);
			builder.setMessage(layout.format(logEvent));
			
			Logger.getRootLogger().removeAppender(this);
			for (IMessageGateway gateway : messageGateways)
			{
				gateway.sendMessage(EMessage.LOG_MESSAGES, builder.build().toByteArray());
			}
			Logger.getRootLogger().addAppender(this);
		}
	}
	
	private class StopBotOnNoMessage implements Runnable
	{
		
		@Override
		public void run()
		{
			for (Map.Entry<BotID, Long> entry : botLastMessage.entrySet())
			{
				long diff = System.nanoTime() - entry.getValue();
				if (TimeUnit.NANOSECONDS.toMillis(diff) > 500)
				{
					ABot bot = botManager.getBots().get(entry.getKey());
					if (bot != null)
					{
						getInterpreter(bot).stopAll();
					}
					botLastMessage.remove(entry.getKey());
					break;
				}
			}
		}
	}
	
	private class MessageReceiver implements IMessageObserver
	{
		
		
		@Override
		public void onNewMessageArrived(final IMessageType msgType, final Message message)
		{
			messageArrived(msgType.name(), message);
		}
		
		
		private void messageArrived(final String name, final Message message)
		{
			if (name.equals(EMessage.BOT_ACTION_COMMAND.name()))
			{
				BotActionCommandProtos.BotActionCommand cmd = (BotActionCommandProtos.BotActionCommand) message;
				botActionCommandReceived(cmd);
			} else if (name.equals(EMessage.LIFE_SIGN.name()))
			{
				LifeSignProtos.LifeSign lifeSign = (LifeSignProtos.LifeSign) message;
				lifeSignReceived(lifeSign);
			} else if (name.equals(EMessage.REFEREE_COMMAND.name()))
			{
				RefereeCommandSimple msg = (RefereeCommandSimple) message;
				refereeCommandReceived(msg);
			} else
			{
				log.error("Message could not be handeled: " + name);
			}
		}
		
		
		// Switch case required to map message commands to Command objects
		@SuppressWarnings("squid:MethodCyclomaticComplexity")
		private void refereeCommandReceived(final RefereeCommandSimple msg)
		{
			Command cmd;
			switch (msg.getCommand())
			{
				case DIRECT_B:
					cmd = Command.DIRECT_FREE_BLUE;
					break;
				case DIRECT_Y:
					cmd = Command.DIRECT_FREE_YELLOW;
					break;
				case FORCE_START:
					cmd = Command.FORCE_START;
					break;
				case HALT:
					cmd = Command.HALT;
					break;
				case INDIRECT_B:
					cmd = Command.INDIRECT_FREE_BLUE;
					break;
				case INDIRECT_Y:
					cmd = Command.INDIRECT_FREE_YELLOW;
					break;
				case KICKOFF_B:
					cmd = Command.PREPARE_KICKOFF_BLUE;
					break;
				case KICKOFF_Y:
					cmd = Command.PREPARE_KICKOFF_YELLOW;
					break;
				case NORMAL_START:
					cmd = Command.NORMAL_START;
					break;
				case PENALTY_B:
					cmd = Command.PREPARE_PENALTY_BLUE;
					break;
				case PENALTY_Y:
					cmd = Command.PREPARE_PENALTY_YELLOW;
					break;
				case STOP:
					cmd = Command.STOP;
					break;
				default:
					throw new IllegalStateException();
			}
			
			refBox.handleControlRequest(RefBoxRemoteControlFactory.fromCommand(cmd));
		}
		
		
		private void botActionCommandReceived(final BotActionCommandProtos.BotActionCommand cmd)
		{
			final ABot bot = botManager.getBots().get(createBotIdFromBotColorId(cmd.getBotId()));
			if (bot == null)
			{
				log.error("bot " + cmd.getBotId() + " is not available");
				return;
			}
			
			ICommandInterpreter interpreter = getInterpreter(bot);
			interpreter.interpret(cmd);
			botLastMessage.put(bot.getBotId(), System.nanoTime());
		}
		
		
		private void lifeSignReceived(final LifeSignProtos.LifeSign lifeSign)
		{
			ABot bot;
			if (!clients.containsKey(lifeSign.getName()))
			{
				log.info("New client: " + lifeSign.getName());
			}
			delayedBotStatiSender.send();
			sendAiControlAvailableInfo();
			clients.put(lifeSign.getName(), System.nanoTime());
			
			if (lifeSign.hasActiveBot())
			{
				bot = botManager.getBots().get(createBotIdFromBotColorId(lifeSign.getActiveBot()));
				if ((bot == null) || !bot.getControlledBy().equals(lifeSign.getName()))
				{
					removeActiveBot(lifeSign.getName());
					addActiveBot(createBotIdFromBotColorId(lifeSign.getActiveBot()));
					if (bot != null)
					{
						CommandInterpreter interpreter = getInterpreter(bot);
						ConfigRegistration.applySpezis(interpreter, "rcm", EControllerType.ANDROID.name());
						interpreter.setCompassThreshold(0);
						bot.setControlledBy(lifeSign.getName());
					}
				}
			}
		}
		
		
		private BotID createBotIdFromBotColorId(final BotColorIdProtos.BotColorId botColorId)
		{
			BotColorIdProtos.BotColorId.Color color = botColorId.getColor();
			ETeamColor tColor = ETeamColor.valueOf(color.name());
			return BotID.createBotId(botColorId.getBotId(), tColor);
		}
		
		
		/**
		 * @param botId
		 */
		private void addActiveBot(final BotID botId)
		{
			BotStatusProtos.BotStatus.Builder botStatus = getBotStatusBuilder(botId);
			if (botStatus != null)
			{
				botStatus.setBlocked(true);
				sendAllBotStati();
			}
		}
		
		
		private void sendAiControlAvailableInfo()
		{
			AiControlAvailableProtos.AiControlAvailable.Builder builder = AiControlAvailableProtos.AiControlAvailable
					.newBuilder();
			builder.addAllSkills(createParamInstances(ESkill.values()));
			
			for (IMessageGateway gateway : messageGateways)
			{
				gateway.sendMessage(EMessage.AI_CONTROL_AVAILABLE, builder.build().toByteArray());
			}
		}
		
		
		private List<ParamInstanceProtos.ParamInstance> createParamInstances(final IInstanceableEnum[] instances)
		{
			List<ParamInstanceProtos.ParamInstance> paramInstances = new ArrayList<>(instances.length);
			for (IInstanceableEnum eRole : instances)
			{
				ParamInstanceProtos.ParamInstance.Builder instBuilder = ParamInstanceProtos.ParamInstance.newBuilder();
				instBuilder.setName(eRole.name());
				
				for (InstanceableParameter param : eRole.getInstanceableClass().getParams())
				{
					ParamInstanceProtos.Param.Builder pBuilder = ParamInstanceProtos.Param.newBuilder();
					pBuilder.setName(param.getDescription());
					pBuilder.setValue(param.getDefaultValue());
					instBuilder.addParams(pBuilder.build());
				}
				paramInstances.add(instBuilder.build());
			}
			return paramInstances;
		}
		
		
		@Override
		public void onConnectionEstablished()
		{
			log.info("Connection established.");
		}
		
		
		@Override
		public void onConnectionLost()
		{
			log.info("Connection lost.");
		}
		
	}
	
	private class BotManagerObserver implements IBotManagerObserver
	{
		
		@Override
		public void onBotAdded(final ABot bot)
		{
			addBot(bot);
		}
		
		
		@Override
		public void onBotRemoved(final ABot bot)
		{
			removeBot(bot);
		}
		
		
		private void removeBot(final IBot bot)
		{
			for (int i = 0; i < botConnBuilder.getStatiCount(); i++)
			{
				if (getBotID(botConnBuilder.getStati(i).getBotId()).equals(bot.getBotId()))
				{
					botConnBuilder.removeStati(i);
					break;
				}
			}
		}
		
		
		private BotID getBotID(final BotColorIdProtos.BotColorId bci)
		{
			return BotID.createBotId(bci.getBotId(),
					bci.getColor() == BotColorIdProtos.BotColorId.Color.YELLOW ? ETeamColor.YELLOW : ETeamColor.BLUE);
		}
	}
}
