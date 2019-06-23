/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 27, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.presenter.rcm;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.google.protobuf.Message;

import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.IBotObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.ENetworkState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm.ActionCommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm.RobotControlManager;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm.inputDevice.sendAction.ARCCommandInterpreter;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm.inputDevice.sendAction.interpreter.TigerInterpreter;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm.inputDevice.sendAction.interpreter.TigerV2Interpreter;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ABotManager;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ARobotControlManager;
import edu.dhbw.mannheim.tigers.sumatra.presenter.moduli.IModuliStateObserver;
import edu.dhbw.mannheim.tigers.sumatra.presenter.moduli.ModuliStateAdapter;
import edu.dhbw.mannheim.tigers.sumatra.proto.model.modules.impls.rcm.ActionCommandProtos.BotActionCommand;
import edu.dhbw.mannheim.tigers.sumatra.proto.model.modules.impls.rcm.BotConnProtos.BotConn;
import edu.dhbw.mannheim.tigers.sumatra.proto.model.modules.impls.rcm.BotConnProtos.BotStatus;
import edu.dhbw.mannheim.tigers.sumatra.proto.model.modules.impls.rcm.LifeSignProtos.LifeSign;
import edu.dhbw.mannheim.tigers.sumatra.util.messaging.Messaging;
import edu.dhbw.mannheim.tigers.sumatra.util.messaging.receiving.IMessageReceivable;
import edu.dhbw.mannheim.tigers.sumatra.util.messaging.topics.ETopics;
import edu.dhbw.mannheim.tigers.sumatra.view.rcm.IMessagingGUIObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.rcm.ShowRCMMainPanel;
import edu.moduli.exceptions.ModuleNotFoundException;
import edu.moduli.listenerVariables.ModulesState;


/**
 * This class enables clients to send commands directly to the bots
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class RCMPresenterMqtt implements IModuliStateObserver, IMessageReceivable, IMessagingGUIObserver
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final Logger					log							= Logger.getLogger(RCMPresenterMqtt.class
																									.getName());
	
	// --- modules ---
	private final SumatraModel						model							= SumatraModel.getInstance();
	private RobotControlManager					rcmModule					= null;
	private ABotManager								botManager					= null;
	
	private ScheduledExecutorService				executor						= Executors.newSingleThreadScheduledExecutor();
	private ScheduledFuture<?>						thread;
	
	private Map<BotID, ARCCommandInterpreter>	botInterpreters			= new HashMap<BotID, ARCCommandInterpreter>();
	
	private final Map<ABot, IBotObserver>		botObservers				= new HashMap<ABot, IBotObserver>();
	
	private final Map<String, Long>				clients						= new HashMap<String, Long>();
	
	private ScheduledExecutorService				deleteClientsExecutor;
	
	private final BotConn.Builder					botConnBuilder				= BotConn.newBuilder();
	
	private final DelayedBotStatiSender			delayedBotStatiSender	= new DelayedBotStatiSender();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	  * 
	  */
	public RCMPresenterMqtt()
	{
		Messaging.getDefaultCon().setConnectionInfo(Messaging.DEFAULT_HOST, Messaging.DEFAULT_PORT);
		Messaging.getDefaultCon().addMessageReceiver(ETopics.BOT_ACTION_COMMAND, this);
		Messaging.getDefaultCon().addMessageReceiver(ETopics.LIFE_SIGN, this);
		ModuliStateAdapter.getInstance().addObserver(this);
		ShowRCMMainPanel.getInstance().addMessgingGUIObserver(this);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	public void onModuliStateChanged(ModulesState state)
	{
		switch (state)
		{
			case ACTIVE:
			{
				try
				{
					rcmModule = (RobotControlManager) model.getModule(ARobotControlManager.MODULE_ID);
				} catch (final ModuleNotFoundException err)
				{
					log.fatal("RCMModule not found");
				}
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
				deleteClientsExecutor = Executors.newSingleThreadScheduledExecutor();
				deleteClientsExecutor.scheduleAtFixedRate(new DeleteOldClients(), 0, 1, TimeUnit.MINUTES);
				
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
	}
	
	
	private ARCCommandInterpreter getInterpreter(ABot bot)
	{
		ARCCommandInterpreter interpreter = botInterpreters.get(bot.getBotID());
		if (interpreter == null)
		{
			switch (bot.getType())
			{
				case GRSIM:
				case TIGER:
					interpreter = new TigerInterpreter(bot);
					break;
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
	
	
	@Override
	public void messageArrived(ETopics mqttTopic, Message message)
	{
		ABot bot = null;
		switch (mqttTopic)
		{
			case BOT_ACTION_COMMAND:
				if (!(message instanceof BotActionCommand))
				{
					throw new IllegalStateException();
				}
				BotActionCommand cmd = (BotActionCommand) message;
				ActionCommand aCmd = interpretActionCmdMessage(cmd);
				bot = rcmModule.getBotByBotID(cmd.getBotId());
				
				if (bot == null)
				{
					log.error("bot " + cmd.getBotId() + " is not available");
					return;
				}
				
				ARCCommandInterpreter interpreter = getInterpreter(bot);
				interpreter.interpret(aCmd);
				break;
			case LIFE_SIGN:
				if (!(message instanceof LifeSign))
				{
					throw new IllegalStateException();
				}
				LifeSign lifeSign = (LifeSign) message;
				if (!clients.containsKey(lifeSign.getName()))
				{
					log.info("New client: " + lifeSign.getName());
				}
				clients.put(lifeSign.getName(), System.nanoTime());
				
				if (lifeSign.hasActiveBot())
				{
					bot = rcmModule.getBotByBotID(lifeSign.getActiveBot());
					if ((bot == null) || !bot.getControlledBy().equals(lifeSign.getName()))
					{
						removeActiveBot(lifeSign.getName());
						addActiveBot(lifeSign.getActiveBot());
						if (bot != null)
						{
							bot.setControlledBy(lifeSign.getName());
						}
					}
				}
				break;
			default:
				return;
		}
		if (thread != null)
		{
			thread.cancel(true);
		}
		thread = executor.schedule(new StopBotOnNoMessageThread(), 500, TimeUnit.MILLISECONDS);
	}
	
	
	/**
	 * @param botId
	 */
	public void addActiveBot(int botId)
	{
		BotStatus.Builder botStatus = getBotStatusBuilder(botId);
		if (botStatus != null)
		{
			botStatus.setBlocked(true);
			sendAllBotStati();
		}
	}
	
	
	private ABot removeActiveBot(String clientName)
	{
		for (ABot bot : botManager.getAllBots().values())
		{
			if (bot.getControlledBy().equals(clientName))
			{
				bot.setControlledBy("");
				BotStatus.Builder botStatus = getBotStatusBuilder(bot.getBotID().getNumber());
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
	
	
	private ActionCommand interpretActionCmdMessage(BotActionCommand cmd)
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
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	private class StopBotOnNoMessageThread implements Runnable
	{
		
		@Override
		public void run()
		{
			for (ABot bot : rcmModule.getAllBots())
			{
				ARCCommandInterpreter interpreter = new TigerInterpreter(bot);
				ActionCommand aCmd = new ActionCommand();
				interpreter.interpret(aCmd);
			}
		}
		
	}
	
	
	@Override
	public void onConnectionEvent(boolean connected)
	{
	}
	
	
	private void addBot(ABot bot)
	{
		IBotObserver botConnectionObs = new BotConnectionListener(bot);
		bot.addObserver(botConnectionObs);
		botObservers.put(bot, botConnectionObs);
		
		BotStatus.Builder builder = BotStatus.newBuilder();
		builder.setBotId(bot.getBotID().getNumber());
		builder.setAvailable(true);
		synchronized (botConnBuilder)
		{
			botConnBuilder.addBotStatus(builder);
		}
	}
	
	
	private void removeAllBots(Collection<ABot> bots)
	{
		for (ABot bot : bots)
		{
			IBotObserver botConnectionObs = botObservers.remove(bot);
			bot.removeObserver(botConnectionObs);
		}
		synchronized (botConnBuilder)
		{
			botConnBuilder.clearBotStatus();
		}
	}
	
	
	private BotStatus.Builder getBotStatusBuilder(int id)
	{
		for (BotStatus.Builder botStatus : botConnBuilder.getBotStatusBuilderList())
		{
			if (botStatus.getBotId() == id)
			{
				return botStatus;
			}
		}
		return null;
	}
	
	private class BotConnectionListener implements IBotObserver
	{
		private final ABot	bot;
		
		
		/**
		 * @param bot
		 */
		public BotConnectionListener(ABot bot)
		{
			this.bot = bot;
		}
		
		
		@Override
		public void onNameChanged(String name)
		{
		}
		
		
		@Override
		public void onIdChanged(BotID oldId, BotID newId)
		{
		}
		
		
		@Override
		public void onNetworkStateChanged(ENetworkState state)
		{
			BotStatus.Builder builder = getBotStatusBuilder();
			
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
		public void onBlocked(boolean blocked)
		{
			BotStatus.Builder builder = getBotStatusBuilder();
			
			if (builder == null)
			{
				log.error("No builder found, thats strange...");
				return;
			}
			
			builder.setBlocked(blocked);
			delayedBotStatiSender.send();
		}
		
		
		private BotStatus.Builder getBotStatusBuilder()
		{
			BotStatus.Builder builder = null;
			synchronized (botConnBuilder)
			{
				for (BotStatus.Builder b : botConnBuilder.getBotStatusBuilderList())
				{
					if (b.getBotId() == bot.getBotID().getNumber())
					{
						builder = b;
					}
				}
			}
			return builder;
		}
	}
	
	
	private void sendAllBotStati()
	{
		Messaging.getDefaultCon().publish(ETopics.BOT_CONNECTION, botConnBuilder.build().toByteArray());
	}
	
	private class DeleteOldClients implements Runnable
	{
		
		private static final long	CLIENT_TIMEOUT	= 60;
		
		
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
		private ScheduledExecutorService	executor	= Executors.newSingleThreadScheduledExecutor();
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
			Messaging.getDefaultCon().publish(ETopics.BOT_CONNECTION, botConnBuilder.build().toByteArray());
		}
		
	}
	
	
	@Override
	public void onConnect()
	{
		Messaging.getDefaultCon().connect();
	}
	
	
	@Override
	public void onDisconnect()
	{
		Messaging.getDefaultCon().disconnect();
	}
}
