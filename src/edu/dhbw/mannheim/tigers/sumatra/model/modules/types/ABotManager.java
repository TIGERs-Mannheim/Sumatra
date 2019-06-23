/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 08.08.2010
 * Author(s): Gero, AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.types;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EBotType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.udp.ITransceiverUDP;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IBotManagerObserver;
import edu.moduli.AModule;


/**
 * A module that is capable of managing and controlling all our BattleMechs! =)
 * 
 * @author Gero
 * 
 */
public abstract class ABotManager extends AModule
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	public static final String	MODULE_TYPE						= "ABotManager";
	public static final String	MODULE_ID						= "botmanager";
	

	public final static String	BOTMANAGER_CONFIG_PATH		= "./config/botmanager/";
	public final static String	BOTMANAGER_DEFAULT_CONFIG	= "botmanager_default.xml";
	
	protected static String		selectedPersistentConfig	= BOTMANAGER_DEFAULT_CONFIG;
	
	
	// --------------------------------------------------------------------------
	// --- methods ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public abstract void execute(int id, ACommand cmd);
	

	public abstract ABot addBot(EBotType type, int id, String name);
	

	public abstract void removeBot(int id);
	

	public abstract void changeBotId(int oldId, int newId);
	

	public abstract Map<Integer, ABot> getAllBots();
	

	public abstract void removeAllBots();
	
	/**
	 * Internally used to save selected config during Sumatra shutdown.
	 * 
	 * @param currentConfig
	 */
	public static void setSelectedPersistentConfig(String currentConfig)
	{
		ABotManager.selectedPersistentConfig = currentConfig;
	}
	
	/**
	 * Internally used to set selected config after Sumatra start.
	 * 
	 * @return config
	 */
	public static String getSelectedPersistentConfig()
	{
		return selectedPersistentConfig;
	}
	
	public abstract void loadConfig(String config);
	public abstract List<String> getAvailableConfigs();
	public abstract String getLoadedConfig();
	public abstract void saveConfig(String filename);
	public abstract void deleteConfig(String config);

	public abstract Map<String, EBotType> getBotTypeMap();

	public abstract void setUseMulticast(boolean enable);
	public abstract boolean getUseMulticast();
	public abstract void setUpdateAllSleepTime(long time);
	public abstract long getUpdateAllSleepTime();
	
	public abstract ITransceiverUDP getMulticastTransceiver();

	private List<IBotManagerObserver>	observers	= new ArrayList<IBotManagerObserver>();
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	public void addObserver(IBotManagerObserver o)
	{
		synchronized (observers)
		{
			observers.add(o);
		}
	}
	

	public void removeObserver(IBotManagerObserver o)
	{
		synchronized (observers)
		{
			observers.remove(o);
		}
	}
	

	protected void notifyBotAdded(ABot bot)
	{
		synchronized (observers)
		{
			for (IBotManagerObserver o : observers)
			{
				o.onBotAdded(bot);
			}
		}
	}
	

	protected void notifyBotRemoved(ABot bot)
	{
		synchronized (observers)
		{
			for (IBotManagerObserver o : observers)
			{
				o.onBotRemoved(bot);
			}
		}
	}
	

	protected void notifyBotIdChanged(int oldId, int newId)
	{
		synchronized (observers)
		{
			for (IBotManagerObserver observer : observers)
			{
				observer.onBotIdChanged(oldId, newId);
			}
		}
	}
}
