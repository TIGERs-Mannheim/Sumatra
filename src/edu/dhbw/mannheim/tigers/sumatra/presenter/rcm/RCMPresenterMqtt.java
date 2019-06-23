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
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.Priority;
import org.apache.log4j.WriterAppender;
import org.apache.log4j.spi.LoggingEvent;

import com.google.protobuf.Message;

import edu.dhbw.mannheim.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.dhbw.mannheim.tigers.moduli.listenerVariables.ModulesState;
import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.EMessage;
import edu.dhbw.mannheim.tigers.sumatra.model.data.ETopics;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Referee.SSL_Referee.Command;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory.SplinePair3D;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.Agent;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.IBotObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.ENetworkState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm.ActionCommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm.inputDevice.sendAction.ARCCommandInterpreter;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm.inputDevice.sendAction.TigerV2Interpreter;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.GenericSkillSystem;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ASkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AAgent;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ABotManager;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AReferee;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ASkillSystem;
import edu.dhbw.mannheim.tigers.sumatra.presenter.aicenter.EAIControlState;
import edu.dhbw.mannheim.tigers.sumatra.presenter.moduli.IModuliStateObserver;
import edu.dhbw.mannheim.tigers.sumatra.presenter.moduli.ModuliStateAdapter;
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
import edu.dhbw.mannheim.tigers.sumatra.util.IInstanceableEnum;
import edu.dhbw.mannheim.tigers.sumatra.util.InstanceableClass;
import edu.dhbw.mannheim.tigers.sumatra.util.InstanceableClass.NotCreateableException;
import edu.dhbw.mannheim.tigers.sumatra.util.InstanceableParameter;
import edu.dhbw.mannheim.tigers.sumatra.util.NamedThreadFactory;
import edu.dhbw.mannheim.tigers.sumatra.util.messaging.MessageType;
import edu.dhbw.mannheim.tigers.sumatra.util.messaging.Messaging;
import edu.dhbw.mannheim.tigers.sumatra.util.messaging.receiving.IMessageReceivable;
import edu.dhbw.mannheim.tigers.sumatra.util.messaging.topics.ITopics;
import edu.dhbw.mannheim.tigers.sumatra.view.rcm.IMessagingGUIObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.rcm.ShowRCMMainPanel;
import edu.dhbw.mannheim.tigers.sumatra.views.ISumatraView;
import edu.dhbw.mannheim.tigers.sumatra.views.ISumatraViewPresenter;
import edu.tigers.bluetoothprotobuf.BluetoothPbLocal;
import edu.tigers.bluetoothprotobuf.IMessageGateway;
import edu.tigers.bluetoothprotobuf.IMessageObserver;
import edu.tigers.bluetoothprotobuf.IMessageType;
import edu.tigers.bluetoothprotobuf.MessageContainer;


/**
 * This class enables clients to send commands directly to the bots
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class RCMPresenterMqtt implements IModuliStateObserver, IMessagingGUIObserver, ISumatraViewPresenter
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final Logger						log							= Logger.getLogger(RCMPresenterMqtt.class
																										.getName());
	
	private static final long							CLIENT_TIMEOUT				= 10;
	
	// --- modules ---
	private ABotManager									botManager					= null;
	private Agent											aiAgentYellow				= null;
	private Agent											aiAgentBlue					= null;
	private GenericSkillSystem							skillSystem					= null;
	private AReferee										referee						= null;
	
	private ScheduledExecutorService					executor						= Executors.newSingleThreadScheduledExecutor();
	private ScheduledFuture<?>							thread;
	
	private Map<BotID, ARCCommandInterpreter>		botInterpreters			= new HashMap<BotID, ARCCommandInterpreter>();
	
	private final Map<ABot, IBotObserver>			botObservers				= new HashMap<ABot, IBotObserver>();
	
	private final Map<String, Long>					clients						= new HashMap<String, Long>();
	private final Map<String, LogMessagesSender>	clientsLogSenders			= new HashMap<String, LogMessagesSender>();
	
	private ScheduledExecutorService					deleteClientsExecutor;
	
	private final BotStati.Builder					botConnBuilder				= BotStati.newBuilder();
	
	private final DelayedBotStatiSender				delayedBotStatiSender	= new DelayedBotStatiSender();
	
	private final List<IMessageGateway>				messageGateways			= new ArrayList<IMessageGateway>();
	private final BluetoothPbLocal					btPbLocal					= new BluetoothPbLocal(new MessageContainer(
																										EMessage.values()));
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	  * 
	  */
	public RCMPresenterMqtt()
	{
		Messaging.getDefaultCon().setConnectionInfo(Messaging.DEFAULT_HOST, Messaging.DEFAULT_PORT);
		
		MessageReceiver receiver = new MessageReceiver();
		Messaging.getDefaultCon().addMessageReceiver(ETopics.BOT_ACTION_COMMAND, receiver);
		Messaging.getDefaultCon().addMessageReceiver(ETopics.LIFE_SIGN, receiver);
		Messaging.getDefaultCon().addMessageReceiver(ETopics.AI_CONTROL, receiver);
		Messaging.getDefaultCon().addMessageReceiver(ETopics.AI_CONTROL_STATE, receiver);
		Messaging.getDefaultCon().addMessageReceiver(ETopics.REFEREE_COMMAND, receiver);
		btPbLocal.addObserver(receiver);
		
		ModuliStateAdapter.getInstance().addObserver(this);
		ShowRCMMainPanel.getInstance().addMessgingGUIObserver(this);
		
		messageGateways.add(new MqttMessageGateway());
		messageGateways.add(btPbLocal);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	public void onModuliStateChanged(final ModulesState state)
	{
		switch (state)
		{
			case ACTIVE:
			{
				try
				{
					botManager = (ABotManager) SumatraModel.getInstance().getModule(ABotManager.MODULE_ID);
					for (ABot bot : botManager.getAllBots().values())
					{
						addBot(bot);
					}
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
				deleteClientsExecutor = Executors.newSingleThreadScheduledExecutor();
				deleteClientsExecutor.scheduleAtFixedRate(new DeleteOldClients(), 0, 10, TimeUnit.SECONDS);
				
				break;
			}
			
			case RESOLVED:
			{
				if (deleteClientsExecutor != null)
				{
					deleteClientsExecutor.shutdown();
				}
				if (botManager != null)
				{
					removeAllBots(botManager.getAllBots().values());
				}
				sendAllBotStati();
				break;
			}
			case NOT_LOADED:
			default:
				break;
		}
		RCMPresenter.getInstance().onModuliStateChanged(state);
	}
	
	
	private ARCCommandInterpreter getInterpreter(final ABot bot)
	{
		ARCCommandInterpreter interpreter = botInterpreters.get(bot.getBotID());
		if (interpreter == null)
		{
			switch (bot.getType())
			{
				case GRSIM:
				case TIGER:
				case TIGER_V2:
					interpreter = new TigerV2Interpreter(bot);
					break;
				case UNKNOWN:
					throw new IllegalArgumentException();
			}
			botInterpreters.put(bot.getBotID(), interpreter);
		}
		return interpreter;
	}
	
	
	private void botActionCommandReceived(final BotActionCommand cmd)
	{
		ABot bot = null;
		ActionCommand aCmd = interpretActionCmdMessage(cmd);
		bot = botManager.getAllBots().get(createBotIdFromBotColorId(cmd.getBotId()));
		
		if (bot == null)
		{
			log.error("bot " + cmd.getBotId() + " is not available");
			return;
		}
		
		ARCCommandInterpreter interpreter = getInterpreter(bot);
		interpreter.interpret(aCmd);
		afterMessageArrived();
	}
	
	
	private void afterMessageArrived()
	{
		if (thread != null)
		{
			thread.cancel(true);
		}
		thread = executor.schedule(new StopBotOnNoMessageThread(), 500, TimeUnit.MILLISECONDS);
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
			LogMessagesSender lms = new LogMessagesSender();
			clientsLogSenders.put(lifeSign.getName(), lms);
			Logger.getRootLogger().addAppender(lms);
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
		switch (msg.getType())
		{
			case PLAY:
				instEnum = EPlay.valueOf(name);
				break;
			case ROLE:
				instEnum = ERole.valueOf(name);
				break;
			case SKILL:
				instEnum = ESkillName.valueOf(name);
				break;
			default:
				throw new IllegalArgumentException("Unknown type: " + msg.getType());
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
		
		final Agent agent;
		if (botId.getTeamColor() == ETeamColor.BLUE)
		{
			agent = aiAgentBlue;
		} else
		{
			agent = aiAgentYellow;
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
		
		referee.sendOwnRefereeMsg(cmd, 0, 0, (short) 899);
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
	
	
	private ABot removeActiveBot(final String clientName)
	{
		for (ABot bot : botManager.getAllBots().values())
		{
			if (bot.getControlledBy().equals(clientName))
			{
				bot.setControlledBy("");
				BotStatus.Builder botStatus = getBotStatusBuilder(bot.getBotID());
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
	
	
	private ActionCommand interpretActionCmdMessage(final BotActionCommand cmd)
	{
		ActionCommand actionCommand = new ActionCommand();
		actionCommand.setTranslateX(cmd.getTranslateX());
		actionCommand.setTranslateY(cmd.getTranslateY());
		actionCommand.setRotate(cmd.getRotate());
		actionCommand.setKick(cmd.getKick());
		actionCommand.setArm(cmd.getArm());
		actionCommand.setChipArm(cmd.getChipArm());
		actionCommand.setChipKick(cmd.getChipKick());
		actionCommand.setDribble(cmd.getDribble());
		actionCommand.setPass(cmd.getPass());
		return actionCommand;
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
		builder.addAllSkills(createParamInstances(ESkillName.values()));
		
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
	
	
	private void addBot(final ABot bot)
	{
		IBotObserver botConnectionObs = new BotConnectionListener(bot);
		bot.addObserver(botConnectionObs);
		botObservers.put(bot, botConnectionObs);
		
		BotStatus.Builder builder = BotStatus.newBuilder();
		BotColorId.Builder idBuilder = BotColorId.newBuilder();
		idBuilder.setBotId(bot.getBotID().getNumber());
		idBuilder.setColor(Color.valueOf(bot.getColor().getId()));
		builder.setBotId(idBuilder.build());
		builder.setAvailable(true);
		builder.setConnected(bot.getNetworkState() == ENetworkState.ONLINE);
		synchronized (botConnBuilder)
		{
			botConnBuilder.addStati(builder);
		}
	}
	
	
	private void removeAllBots(final Collection<ABot> bots)
	{
		for (ABot bot : bots)
		{
			IBotObserver botConnectionObs = botObservers.remove(bot);
			bot.removeObserver(botConnectionObs);
		}
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
		Messaging.getDefaultCon().connect();
		btPbLocal.start();
	}
	
	
	@Override
	public void onDisconnect()
	{
		Messaging.getDefaultCon().disconnect();
		btPbLocal.stop();
		log.info("Messaging stopped");
	}
	
	
	@Override
	public Component getComponent()
	{
		return RCMPresenter.getInstance().getComponent();
	}
	
	
	@Override
	public ISumatraView getSumatraView()
	{
		return RCMPresenter.getInstance().getView();
	}
	
	
	@Override
	public void onEmergencyStop()
	{
	}
	
	
	private class StopBotOnNoMessageThread implements Runnable
	{
		
		@Override
		public void run()
		{
			for (ABot bot : botManager.getAllBots().values())
			{
				ARCCommandInterpreter interpreter = new TigerV2Interpreter(bot);
				ActionCommand aCmd = new ActionCommand();
				interpreter.interpret(aCmd);
			}
		}
	}
	
	private class BotConnectionListener implements IBotObserver
	{
		private final ABot	bot;
		
		
		/**
		 * @param bot
		 */
		public BotConnectionListener(final ABot bot)
		{
			this.bot = bot;
		}
		
		
		@Override
		public void onNameChanged(final String name)
		{
		}
		
		
		@Override
		public void onIdChanged(final BotID oldId, final BotID newId)
		{
			BotStatus.Builder builder = getBotStatusBuilder(oldId);
			BotColorId.Builder idBuilder = BotColorId.newBuilder();
			idBuilder.setBotId(newId.getNumber());
			idBuilder.setColor(Color.valueOf(newId.getTeamColor().getId()));
			builder.setBotId(idBuilder.build());
		}
		
		
		@Override
		public void onNetworkStateChanged(final ENetworkState state)
		{
			BotStatus.Builder builder = getBotStatusBuilder(bot.getBotID());
			
			if (builder == null)
			{
				log.error("No builder found, thats strange...");
				return;
			}
			
			switch (state)
			{
				case ONLINE:
					builder.setConnected(true);
					break;
				case CONNECTING:
				case OFFLINE:
				default:
					builder.setConnected(false);
					break;
			}
			delayedBotStatiSender.send();
		}
		
		
		@Override
		public void onBlocked(final boolean blocked)
		{
			BotStatus.Builder builder = getBotStatusBuilder(bot.getBotID());
			
			if (builder == null)
			{
				log.error("No builder found, thats strange...");
				return;
			}
			
			builder.setBlocked(blocked);
			delayedBotStatiSender.send();
		}
		
		
		@Override
		public void onNewSplineData(final SplinePair3D spline)
		{
		}
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
					LogMessagesSender lms = clientsLogSenders.remove(entry.getKey());
					if (lms != null)
					{
						Logger.getRootLogger().removeAppender(lms);
					}
					removeActiveBot(entry.getKey());
					log.info("Client " + entry.getKey() + " removed");
				}
			}
		}
		
	}
	
	private class DelayedBotStatiSender implements Runnable
	{
		private ScheduledExecutorService	executor	= Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory(
																		"DelayedBotStatiSender"));
		private Future<?>						future	= null;
		
		
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
				sendAllBotStati();
			} catch (Exception e)
			{
				log.error("Could not send bot stati", e);
			}
		}
	}
	
	private class MessageReceiver implements IMessageObserver, IMessageReceivable
	{
		
		@Override
		public void onConnectionEvent(final boolean connected)
		{
			log.info("MQTT connection event: connected=" + connected);
		}
		
		
		@Override
		public void messageArrived(final ITopics mqttTopic, final Message message)
		{
			messageArrived(mqttTopic.name(), message);
		}
		
		
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
				refereeCommandReceived(msg);
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
	
	private class MqttMessageGateway implements IMessageGateway
	{
		
		@Override
		public void sendMessage(final IMessageType mt, final byte[] data)
		{
			ETopics topic;
			if (mt.getId() == EMessage.BOT_ACTION_COMMAND.getId())
			{
				topic = ETopics.BOT_ACTION_COMMAND;
			} else if (mt.getId() == EMessage.LIFE_SIGN.getId())
			{
				topic = ETopics.LIFE_SIGN;
			} else if (mt.getId() == EMessage.BOT_STATI.getId())
			{
				topic = ETopics.BOT_STATI;
			} else if (mt.getId() == EMessage.LOG_MESSAGES.getId())
			{
				topic = ETopics.LOG_MESSAGES;
			} else if (mt.getId() == EMessage.AI_CONTROL_AVAILABLE.getId())
			{
				topic = ETopics.AI_CONTROL_AVAILABLE;
			} else
			{
				log.error("No topic for message: " + mt.name());
				return;
			}
			
			// log.trace("Sending message: " + topic.name());
			Messaging.getDefaultCon().publish(topic, data);
		}
		
		
		@Override
		public boolean isConnected()
		{
			return Messaging.getDefaultCon().isSendConnected(MessageType.NONDURABLE_INFO);
		}
		
		
		@Override
		public void start()
		{
		}
		
		
		@Override
		public void stop()
		{
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
}
