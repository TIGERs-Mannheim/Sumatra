/*
 * *********************************************************
 * Copyright (c) 2009 - 2012, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 26.04.2012
 * Author(s): Manuel
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm.inputDevice.network;

import java.util.ArrayList;
import java.util.List;

import edu.dhbw.mannheim.tigers.shared.AndroidBot;
import edu.dhbw.mannheim.tigers.shared.AndroidCommand;
import edu.dhbw.mannheim.tigers.shared.AndroidMessage;
import edu.dhbw.mannheim.tigers.shared.EAndroidMessageType;
import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm.ActionCommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm.RobotControlManager;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm.inputDevice.sendAction.interpreter.AndroidTigerInterpreter;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IBotManagerObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ARobotControlManager;
import edu.moduli.exceptions.ModuleNotFoundException;


/**
 * 
 * @author Manuel
 * 
 */
public final class CommandInterpreter implements IBotManagerObserver
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static CommandInterpreter	instance			= null;
	
	private final SumatraModel				sumatraModel;
	private RobotControlManager			rcmModel;
	
	private final ActionCommand			actionCommand;
	private final AndroidMessage			androidMessage;
	
	private final ArrayList<ABot>			allBots;
	private boolean							updateBotlist	= true;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	private CommandInterpreter()
	{
		sumatraModel = SumatraModel.getInstance();
		try
		{
			rcmModel = (RobotControlManager) sumatraModel.getModule(ARobotControlManager.MODULE_ID);
		} catch (final ModuleNotFoundException e)
		{
			
		}
		
		actionCommand = new ActionCommand();
		androidMessage = new AndroidMessage(EAndroidMessageType.KEEP_ALIVE_FROM_SERVER);
		
		allBots = new ArrayList<ABot>();
		
		allBots.addAll(rcmModel.getAllBots());
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Singleton
	 * @return
	 */
	public static synchronized CommandInterpreter getInstance()
	{
		if (instance == null)
		{
			instance = new CommandInterpreter();
		}
		return instance;
	}
	
	
	/**
	 * @param message
	 */
	public void interpretAndroidMessage(AndroidMessage message)
	{
		if (message == null)
		{
			return;
		}
		if (message.getEAndroidMessageType() == EAndroidMessageType.KEEP_ALIVE_FROM_ANDROID)
		{
			return;
		} else if (message.getEAndroidMessageType() == EAndroidMessageType.BOT_COMMAND)
		{
			AndroidTigerInterpreter.getInstance().interpret(mapAndroidCommandToActionCommand(message.getAndroidCommand()),
					mapAndroidBotToABot(message.getAndroidBot()));
		}
	}
	
	
	private ActionCommand mapAndroidCommandToActionCommand(AndroidCommand command)
	{
		actionCommand.fillCommandWithNulls();
		
		if (command.isMoveLeft() && command.isMoveRight())
		{
			actionCommand.setTranslateX(0.0);
		} else if (command.isMoveLeft())
		{
			actionCommand.setTranslateX(-1.0);
		} else if (command.isMoveRight())
		{
			actionCommand.setTranslateX(1.0);
		}
		
		if (command.isMoveAhead() && command.isMoveBack())
		{
			actionCommand.setTranslateX(0.0);
		} else if (command.isMoveAhead())
		{
			actionCommand.setTranslateY(1.0);
		} else if (command.isMoveBack())
		{
			actionCommand.setTranslateY(-1.0);
		}
		
		if (command.isRotateLeft() && command.isRotateRight())
		{
			actionCommand.setRotate(0.0);
		} else if (command.isRotateLeft())
		{
			actionCommand.setRotate(1.0);
		} else if (command.isRotateRight())
		{
			actionCommand.setRotate(-1.0);
		}
		
		if (command.isChipKick())
		{
			actionCommand.setChipKick(1.0);
		}
		
		if (command.isForce())
		{
			actionCommand.setKick(1.0);
		}
		
		if (command.isArm())
		{
			actionCommand.setArm(1.0);
		}
		
		if (command.isPass())
		{
			actionCommand.setPass(1.0);
		}
		
		if (command.isDribble())
		{
			actionCommand.setDribble(1.0);
		}
		
		return actionCommand;
	}
	
	
	private ABot mapAndroidBotToABot(AndroidBot androidBot)
	{
		return rcmModel.getBotByBotID(androidBot.getBotId());
	}
	
	
	@Override
	public void onBotAdded(ABot bot)
	{
		synchronized (allBots)
		{
			allBots.add(bot);
			updateBotlist = true;
		}
	}
	
	
	@Override
	public void onBotRemoved(ABot bot)
	{
		synchronized (allBots)
		{
			allBots.remove(bot);
			updateBotlist = true;
		}
	}
	
	
	@Override
	public void onBotIdChanged(BotID oldId, BotID newId)
	{
		
	}
	
	
	/**
	 * @return
	 */
	public AndroidMessage createAndroidMessage()
	{
		androidMessage.resetAndroidMessage();
		updateBotlist = true;
		if (updateBotlist)
		{
			androidMessage.setEAndroidMessageType(EAndroidMessageType.UPDATE_TEAMBOTLIST);
			synchronized (allBots)
			{
				androidMessage.setActiveBotlist(mapABotListToAndroidBotList(allBots));
			}
			updateBotlist = false;
		} else
		{
			androidMessage.setEAndroidMessageType(EAndroidMessageType.KEEP_ALIVE_FROM_SERVER);
		}
		
		return new AndroidMessage(androidMessage);
	}
	
	
	private List<AndroidBot> mapABotListToAndroidBotList(List<ABot> bots)
	{
		final ArrayList<AndroidBot> androidBots = new ArrayList<AndroidBot>();
		
		for (final ABot bot : bots)
		{
			androidBots.add(new AndroidBot(bot.getBotID().getNumber(), bot.getName()));
		}
		
		return androidBots;
	}
	
	
	@Override
	public void onBotConnectionChanged(ABot bot)
	{
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}