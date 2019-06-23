/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 27, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.presenter.rcm;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
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
import com.github.g3force.instanceables.InstanceableClass;
import com.github.g3force.instanceables.InstanceableClass.NotCreateableException;
import com.github.g3force.instanceables.InstanceableParameter;
import com.google.protobuf.Message;

import edu.dhbw.mannheim.tigers.sumatra.proto.AiControlAvailableProtos.AiControlAvailable;
import edu.dhbw.mannheim.tigers.sumatra.proto.AiControlProtos.AiControl;
import edu.dhbw.mannheim.tigers.sumatra.proto.AiControlProtos.AiControl.Type;
import edu.dhbw.mannheim.tigers.sumatra.proto.AiControlStateProtos.AiControlState;
import edu.dhbw.mannheim.tigers.sumatra.proto.BotActionCommandProtos.BotActionCommand;
import edu.dhbw.mannheim.tigers.sumatra.proto.BotColorIdProtos.BotColorId;
import edu.dhbw.mannheim.tigers.sumatra.proto.BotColorIdProtos.BotColorId.Color;
import edu.dhbw.mannheim.tigers.sumatra.proto.BotStatusProtos.BotStati;
import edu.dhbw.mannheim.tigers.sumatra.proto.BotStatusProtos.BotStatus;
import edu.dhbw.mannheim.tigers.sumatra.proto.LifeSignProtos.LifeSign;
import edu.dhbw.mannheim.tigers.sumatra.proto.LogMessagesProtos.LogMessage;
import edu.dhbw.mannheim.tigers.sumatra.proto.LogMessagesProtos.LogMessage.Level;
import edu.dhbw.mannheim.tigers.sumatra.proto.ParamInstanceProtos.Param;
import edu.dhbw.mannheim.tigers.sumatra.proto.ParamInstanceProtos.ParamInstance;
import edu.dhbw.mannheim.tigers.sumatra.proto.RefereeMsgProtos.RefereeCommandSimple;
import edu.dhbw.mannheim.tigers.sumatra.view.rcm.IMessagingGUIObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.rcm.RCMPanel;
import edu.tigers.bluetoothprotobuf.BluetoothPbLocal;
import edu.tigers.bluetoothprotobuf.IMessageGateway;
import edu.tigers.bluetoothprotobuf.IMessageObserver;
import edu.tigers.bluetoothprotobuf.IMessageType;
import edu.tigers.bluetoothprotobuf.MessageContainer;
import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.moduli.listenerVariables.ModulesState;
import edu.tigers.sumatra.Referee.SSL_Referee.Command;
import edu.tigers.sumatra.ai.AAgent;
import edu.tigers.sumatra.ai.Agent;
import edu.tigers.sumatra.ai.data.EAIControlState;
import edu.tigers.sumatra.ai.data.frames.AIInfoFrame;
import edu.tigers.sumatra.ai.pandora.plays.APlay;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
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
import edu.tigers.sumatra.rcm.ActionSender;
import edu.tigers.sumatra.rcm.CommandInterpreter;
import edu.tigers.sumatra.rcm.CommandInterpreterStub;
import edu.tigers.sumatra.rcm.EControllerType;
import edu.tigers.sumatra.rcm.EMessage;
import edu.tigers.sumatra.rcm.ICommandInterpreter;
import edu.tigers.sumatra.rcm.IRCMObserver;
import edu.tigers.sumatra.rcm.RcmActionMap.ERcmControllerConfig;
import edu.tigers.sumatra.referee.AReferee;
import edu.tigers.sumatra.skillsystem.ASkillSystem;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.skillsystem.GenericSkillSystem;
import edu.tigers.sumatra.skillsystem.skills.ASkill;
import edu.tigers.sumatra.thread.NamedThreadFactory;
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
	private static final Logger						log							= Logger.getLogger(RCMPresenter.class
																										.getName());
																										
	private static final long							CLIENT_TIMEOUT				= 10;
																								
	private final BotManagerObserver					botManagerObserver		= new BotManagerObserver();
																								
	// --- modules ---
	private ABotManager									botManager					= null;
	private Agent											aiAgentYellow				= null;
	private Agent											aiAgentBlue					= null;
	private GenericSkillSystem							skillSystem					= null;
	private AReferee										referee						= null;
																								
	private final Map<BotID, CommandInterpreter>	botInterpreters			= new HashMap<BotID, CommandInterpreter>();
	private final List<ActionSender>					actionSenders				= new LinkedList<ActionSender>();
	private final Map<BotID, Long>					botLastMessage				= new HashMap<BotID, Long>();
																								
	private final Map<String, Long>					clients						= new HashMap<String, Long>();
	private final LogMessagesSender					logMessageSender			= new LogMessagesSender();
																								
	private ScheduledExecutorService					scheduledExecutor;
																
	private final BotStati.Builder					botConnBuilder				= BotStati.newBuilder();
																								
	private final DelayedBotStatiSender				delayedBotStatiSender	= new DelayedBotStatiSender();
																								
	private final List<IMessageGateway>				messageGateways			= new ArrayList<IMessageGateway>();
	private final BluetoothPbLocal					btPbLocal					= new BluetoothPbLocal(new MessageContainer(
																										EMessage.values()));
																										
	private final List<AControllerPresenter>		controllerPresenterS		= new ArrayList<AControllerPresenter>();
	private final RCMPanel								rcmPanel;
																
	private final Object									syncSwitchBot				= new Object();
																								
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	static
	{
		// set library path for jinput
		final String curDir = System.getProperty("user.dir");
		System.setProperty("net.java.games.input.librarypath",
				curDir + "/lib/native/" + NativesLoader.DEFAULT_FOLDER_MAP.get(OsDetector.detectOs()));
	}
	
	
	/**
	  * 
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
		final List<Controller> controllers = new ArrayList<Controller>(cs.length);
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
	
	
	private boolean addController(final Controller controller)
	{
		final Controller.Type type = controller.getType();
		AControllerPresenter presenter = null;
		if (type == Controller.Type.KEYBOARD)
		{
			presenter = new KeyboardPresenter(controller);
		} else if (type == Controller.Type.GAMEPAD)
		{
			presenter = new GamePadPresenter(controller);
		} else if (type == Controller.Type.STICK)
		{
			presenter = new GamePadPresenter(controller);
		} else
		{
			return false;
		}
		rcmPanel.addControllerPanel(controller.getName(), presenter.getPanel());
		controllerPresenterS.add(presenter);
		return true;
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
				cP.getPanel().setSelectedBot(BotID.get());
			}
			if (botManager != null)
			{
				for (ABot bot : botManager.getAllBots().values())
				{
					if (!bot.getControlledBy().isEmpty())
					{
						bot.setControlledBy("");
					}
				}
			}
			rcmPanel.stopRcm();
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
				cP.getPanel().setSelectedBot(BotID.get());
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
			int idMax = ((AObjectID.BOT_ID_MAX * 2) + 2);
			assert initId < idMax;
			assert initId >= 0;
			for (int i = 0; i < idMax; i++)
			// for (int id = initId + inc; id != initId; id = (id + inc))
			{
				int id = (initId + (inc * i) + idMax) % idMax;
				// id = (id + idMax) % idMax;
				BotID botId = BotID.createBotIdFromIdWithColorOffsetBS(id);
				ABot bot = botManager.getAllBots().get(botId);
				if (bot != null)
				{
					if ((bot != oldBot) && !bot.isBlocked()
							&& !bot.isHideFromRcm())
					{
						if ((oldBot != null) && !oldBot.getControlledBy().isEmpty())
						{
							oldBot.setControlledBy("");
						}
						changeBotAssignment(actionSender, bot);
						return;
					}
				}
			}
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
			{
				try
				{
					botManager = (ABotManager) SumatraModel.getInstance().getModule(ABotManager.MODULE_ID);
					for (IBot bot : botManager.getAllBots().values())
					{
						addBot(bot);
					}
					botManager.addObserver(botManagerObserver);
					sendAllBotStati();
				} catch (ModuleNotFoundException err)
				{
					log.error("Botmanager module not found. Strange...");
				}
				try
				{
					aiAgentBlue = (Agent) SumatraModel.getInstance().getModule(AAgent.MODULE_ID_BLUE);
					aiAgentYellow = (Agent) SumatraModel.getInstance().getModule(AAgent.MODULE_ID_YELLOW);
				} catch (ModuleNotFoundException err)
				{
					log.error("At least one ai agent module not found. Strange...");
				}
				try
				{
					skillSystem = (GenericSkillSystem) SumatraModel.getInstance().getModule(ASkillSystem.MODULE_ID);
				} catch (ModuleNotFoundException err)
				{
					log.error("SkillSystem module not found. Strange...");
				}
				try
				{
					referee = (AReferee) SumatraModel.getInstance().getModule(AReferee.MODULE_ID);
				} catch (ModuleNotFoundException err)
				{
					log.error("Referee module not found. Strange...");
				}
				
				rcmPanel.start();
				break;
			}
			
			case RESOLVED:
			{
				onStartStopButtonPressed(false);
				if (botManager != null)
				{
					botManager.removeObserver(botManagerObserver);
					removeAllBots(botManager.getAllBots().values());
				}
				sendAllBotStati();
				rcmPanel.stop();
				break;
			}
			case NOT_LOADED:
			default:
				break;
		}
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
	
	
	private void botActionCommandReceived(final BotActionCommand cmd)
	{
		final ABot bot = botManager.getAllBots().get(createBotIdFromBotColorId(cmd.getBotId()));
		if (bot == null)
		{
			log.error("bot " + cmd.getBotId() + " is not available");
			return;
		}
		
		ICommandInterpreter interpreter = getInterpreter(bot);
		interpreter.interpret(cmd);
		botLastMessage.put(bot.getBotId(), System.nanoTime());
	}
	
	
	private BotID createBotIdFromBotColorId(final BotColorId botColorId)
	{
		Color color = botColorId.getColor();
		ETeamColor tColor = ETeamColor.valueOf(color.name());
		return BotID.createBotId(botColorId.getBotId(), tColor);
	}
	
	
	private void lifeSignReceived(final LifeSign lifeSign)
	{
		ABot bot = null;
		if (!clients.containsKey(lifeSign.getName()))
		{
			log.info("New client: " + lifeSign.getName());
		}
		delayedBotStatiSender.send();
		sendAiControlAvailableInfo();
		clients.put(lifeSign.getName(), System.nanoTime());
		
		if (lifeSign.hasActiveBot())
		{
			bot = botManager.getAllBots().get(createBotIdFromBotColorId(lifeSign.getActiveBot()));
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
	
	
	private void aiControlStateReceived(final AiControlState msg)
	{
		EAIControlState state;
		switch (msg.getState())
		{
			case EMERGENCY:
				state = EAIControlState.EMERGENCY_MODE;
				break;
			case MATCH:
				state = EAIControlState.MATCH_MODE;
				break;
			case TEST:
				state = EAIControlState.TEST_MODE;
				break;
			default:
				throw new IllegalStateException();
		}
		if ((aiAgentBlue != null))
		{
			aiAgentBlue.getAthena().changeMode(state);
		}
		if ((aiAgentYellow != null))
		{
			aiAgentYellow.getAthena().changeMode(state);
		}
		if (state == EAIControlState.EMERGENCY_MODE)
		{
			skillSystem.emergencyStop();
		}
	}
	
	
	private void aiControlReceived(final AiControl msg)
	{
		BotID botId = createBotIdFromBotColorId(msg.getBotId());
		String name = msg.getInstance().getName();
		List<Param> params = msg.getInstance().getParamsList();
		final IInstanceableEnum instEnum;
		
		final Agent agent;
		if (botId.getTeamColor() == ETeamColor.BLUE)
		{
			agent = aiAgentBlue;
		} else
		{
			agent = aiAgentYellow;
		}
		
		switch (msg.getType())
		{
			case PLAY:
				instEnum = EPlay.valueOf(name);
				break;
			case ROLE:
				instEnum = ERole.valueOf(name);
				break;
			case SKILL:
				instEnum = ESkill.valueOf(name);
				break;
			default:
				throw new IllegalArgumentException("Unknown type: " + msg.getType());
		}
		
		if (msg.getType() == Type.PLAY)
		{
			AIInfoFrame frame = agent.getLatestAiFrame();
			if (frame != null)
			{
				for (APlay play : frame.getPlayStrategy().getActivePlays())
				{
					if (play.getType() == instEnum)
					{
						agent.getAthena().getAthenaAdapter().getAiControl().addRoles2Play(play, 1);
						return;
					}
				}
			}
		}
		
		InstanceableClass instClass = instEnum.getInstanceableClass();
		
		if (instClass.getParams().size() != params.size())
		{
			throw new IllegalStateException("Provided number of parameters does not match required ones.");
		}
		Object[] objParams = new Object[params.size()];
		int i = 0;
		for (InstanceableParameter instParam : instClass.getParams())
		{
			Object obj = instParam.parseString(params.get(i).getValue());
			objParams[i] = obj;
			i++;
		}
		Object instance;
		try
		{
			instance = instClass.newInstance(objParams);
		} catch (NotCreateableException err1)
		{
			log.error("Could not create instance: " + instEnum, err1);
			return;
		}
		if ((agent != null))
		{
			if ((msg.getType() == Type.PLAY) || (msg.getType() == Type.ROLE))
			{
				if (!agent.isActive())
				{
					agent.setActive(true);
				}
				if (agent.getAthena().getControlState() != EAIControlState.TEST_MODE)
				{
					agent.getAthena().changeMode(EAIControlState.TEST_MODE);
				}
			}
			switch (msg.getType())
			{
				case PLAY:
					agent.getAthena().getAthenaAdapter().getAiControl().addPlay((APlay) instance);
					agent.getAthena().getAthenaAdapter().getAiControl().addRoles2Play((APlay) instance, 1);
					break;
				case ROLE:
					agent.getAthena().getAthenaAdapter().getAiControl().addRole((ARole) instance, botId);
					break;
				case SKILL:
					break;
				default:
					break;
			}
		}
		if ((msg.getType() == Type.SKILL) && (skillSystem != null))
		{
			if (!botId.isBot())
			{
				try
				{
					botId = botManager.getAllBots().keySet().iterator().next();
				} catch (NoSuchElementException err)
				{
					log.error("No available bot!");
				}
			}
			skillSystem.execute(botId, (ASkill) instance);
		}
	}
	
	
	private void refereeCommandReceived(final RefereeCommandSimple msg, final long timestamp)
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
		
		referee.sendOwnRefereeMsg(cmd, 0, 0, (short) 899, timestamp, null);
	}
	
	
	/**
	 * @param botId
	 */
	private void addActiveBot(final BotID botId)
	{
		BotStatus.Builder botStatus = getBotStatusBuilder(botId);
		if (botStatus != null)
		{
			botStatus.setBlocked(true);
			sendAllBotStati();
		}
	}
	
	
	private IBot removeActiveBot(final String clientName)
	{
		for (ABot bot : botManager.getAllBots().values())
		{
			if (bot.getControlledBy().equals(clientName))
			{
				bot.setControlledBy("");
				BotStatus.Builder botStatus = getBotStatusBuilder(bot.getBotId());
				if (botStatus != null)
				{
					botStatus.setBlocked(false);
					sendAllBotStati();
				}
				return bot;
			}
		}
		return null;
	}
	
	
	private void sendAllBotStati()
	{
		for (IMessageGateway gateway : messageGateways)
		{
			gateway.sendMessage(EMessage.BOT_STATI, botConnBuilder.build().toByteArray());
		}
	}
	
	
	private void sendAiControlAvailableInfo()
	{
		AiControlAvailable.Builder builder = AiControlAvailable.newBuilder();
		builder.addAllPlays(createParamInstances(EPlay.values()));
		builder.addAllRoles(createParamInstances(ERole.values()));
		builder.addAllSkills(createParamInstances(ESkill.values()));
		
		for (IMessageGateway gateway : messageGateways)
		{
			gateway.sendMessage(EMessage.AI_CONTROL_AVAILABLE, builder.build().toByteArray());
		}
	}
	
	
	private List<ParamInstance> createParamInstances(final IInstanceableEnum[] instances)
	{
		List<ParamInstance> paramInstances = new ArrayList<ParamInstance>(instances.length);
		for (IInstanceableEnum eRole : instances)
		{
			ParamInstance.Builder instBuilder = ParamInstance.newBuilder();
			instBuilder.setName(eRole.name());
			
			for (InstanceableParameter param : eRole.getInstanceableClass().getParams())
			{
				Param.Builder pBuilder = Param.newBuilder();
				pBuilder.setName(param.getDescription());
				pBuilder.setValue(param.getDefaultValue());
				instBuilder.addParams(pBuilder.build());
			}
			paramInstances.add(instBuilder.build());
		}
		return paramInstances;
	}
	
	
	private void addBot(final IBot bot)
	{
		BotStatus.Builder builder = BotStatus.newBuilder();
		BotColorId.Builder idBuilder = BotColorId.newBuilder();
		idBuilder.setBotId(bot.getBotId().getNumber());
		idBuilder.setColor(Color.valueOf(bot.getColor().getId()));
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
	
	
	private BotID getBotID(final BotColorId bci)
	{
		return BotID.createBotId(bci.getBotId(), bci.getColor() == Color.YELLOW ? ETeamColor.YELLOW : ETeamColor.BLUE);
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
	
	
	private void removeAllBots(final Collection<ABot> bots)
	{
		synchronized (botConnBuilder)
		{
			botConnBuilder.clearStati();
		}
	}
	
	
	private BotStatus.Builder getBotStatusBuilder(final BotID id)
	{
		for (BotStatus.Builder botStatus : botConnBuilder.getStatiBuilderList())
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
		private final ScheduledExecutorService	executor	= Executors
																				.newSingleThreadScheduledExecutor(new NamedThreadFactory(
																						"DelayedBotStatiSender"));
		private Future<?>								future	= null;
																		
																		
		/**
		 */
		public void send()
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
				for (IBot bot : botManager.getAllBots().values())
				{
					if (bot.isHideFromRcm())
					{
						continue;
					}
					BotStatus.Builder builder = getBotStatusBuilder(bot.getBotId());
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
				BotActionCommand cmd = (BotActionCommand) message;
				botActionCommandReceived(cmd);
			} else if (name.equals(EMessage.LIFE_SIGN.name()))
			{
				LifeSign lifeSign = (LifeSign) message;
				lifeSignReceived(lifeSign);
			} else if (name.equals(EMessage.AI_CONTROL_STATE.name()))
			{
				AiControlState msg = (AiControlState) message;
				aiControlStateReceived(msg);
			} else if (name.equals(EMessage.AI_CONTROL.name()))
			{
				AiControl msg = (AiControl) message;
				aiControlReceived(msg);
			} else if (name.equals(EMessage.REFEREE_COMMAND.name()))
			{
				RefereeCommandSimple msg = (RefereeCommandSimple) message;
				refereeCommandReceived(msg, System.nanoTime());
			} else
			{
				log.error("Message could not be handeled: " + name);
			}
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
	
	private class LogMessagesSender extends WriterAppender
	{
		private LogMessagesSender()
		{
			setLayout(new PatternLayout("%d{ABSOLUTE} %-5p [%t|%c{1}] %m%n"));
		}
		
		
		@Override
		public void append(final LoggingEvent logEvent)
		{
			LogMessage.Builder builder = LogMessage.newBuilder();
			Level level;
			switch (logEvent.getLevel().toInt())
			{
				case org.apache.log4j.Level.TRACE_INT:
					level = Level.TRACE;
					break;
				case Priority.DEBUG_INT:
					level = Level.DEBUG;
					break;
				case Priority.INFO_INT:
					level = Level.INFO;
					break;
				case Priority.WARN_INT:
					level = Level.WARN;
					break;
				case Priority.ERROR_INT:
					level = Level.ERROR;
					break;
				case Priority.FATAL_INT:
					level = Level.FATAL;
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
					ABot bot = botManager.getAllBots().get(entry.getKey());
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
	}
}
