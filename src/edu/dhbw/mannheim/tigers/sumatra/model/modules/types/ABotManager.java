/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 08.08.2010
 * Author(s): Gero, AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.types;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.dhbw.mannheim.tigers.moduli.AModule;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.basestation.BaseStation;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EBotType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.udp.ITransceiverUDP;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IBotManagerObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IManualBotObserver;


/**
 * A module that is capable of managing and controlling all our BattleMechs! =)
 * 
 * @author Gero
 */
public abstract class ABotManager extends AModule implements IManualBotObserver
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/** */
	public static final String	MODULE_TYPE					= "ABotManager";
	/** */
	public static final String	MODULE_ID					= "botmanager";
	
	
	/** */
	public static final String	KEY_BOTMANAGER_CONFIG	= ABotManager.class.getName() + ".botmanagerConfig";
	/** */
	public static final String	BOTMANAGER_CONFIG_PATH	= "./config/botmanager/";
	/**  */
	public static final String	VALUE_BOTMANAGER_CONFIG	= "botmanager_sim.xml";
	
	
	// --------------------------------------------------------------------------
	// --- methods ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param id
	 * @param cmd
	 */
	public abstract void execute(BotID id, ACommand cmd);
	
	
	/**
	 * @param type
	 * @param id
	 * @param name
	 * @return
	 */
	public abstract ABot addBot(EBotType type, BotID id, String name);
	
	
	/**
	 * @param bot
	 */
	public abstract void botConnectionChanged(ABot bot);
	
	
	/**
	 * @param id
	 */
	public abstract void removeBot(BotID id);
	
	
	/**
	 * @param oldId
	 * @param newId
	 */
	public abstract void changeBotId(BotID oldId, BotID newId);
	
	
	/**
	 * @return
	 */
	public abstract Map<BotID, ABot> getAllBots();
	
	
	/**
	 * @param enable
	 */
	public abstract void setUseMulticast(boolean enable);
	
	
	/**
	 * @return
	 */
	public abstract boolean getUseMulticast();
	
	
	/**
	 * @param time
	 */
	public abstract void setUpdateAllSleepTime(long time);
	
	
	/**
	 * @return
	 */
	public abstract long getUpdateAllSleepTime();
	
	
	/**
	 * @param mcastDelegateKey
	 * @return
	 */
	public abstract ITransceiverUDP getMulticastTransceiver(int mcastDelegateKey);
	
	
	/**
	 * @return
	 */
	public abstract Map<Integer, BaseStation> getBaseStations();
	
	private final List<IBotManagerObserver>	observers	= new ArrayList<IBotManagerObserver>();
	
	
	/**
	 * @param chg
	 */
	public abstract void chargeAll(int chg);
	
	
	/**
	 * 
	 */
	public abstract void dischargeAll();
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param o
	 */
	public void addObserver(final IBotManagerObserver o)
	{
		synchronized (observers)
		{
			observers.add(o);
		}
	}
	
	
	/**
	 * @param o
	 */
	public void removeObserver(final IBotManagerObserver o)
	{
		synchronized (observers)
		{
			observers.remove(o);
		}
	}
	
	
	protected void notifyBotConnectionChanged(final ABot bot)
	{
		synchronized (observers)
		{
			for (final IBotManagerObserver o : observers)
			{
				o.onBotConnectionChanged(bot);
			}
		}
	}
	
	
	protected void notifyBotAdded(final ABot bot)
	{
		synchronized (observers)
		{
			for (final IBotManagerObserver o : observers)
			{
				o.onBotAdded(bot);
			}
		}
	}
	
	
	protected void notifyBotRemoved(final ABot bot)
	{
		synchronized (observers)
		{
			for (final IBotManagerObserver o : observers)
			{
				o.onBotRemoved(bot);
			}
		}
	}
	
	
	protected void notifyBotIdChanged(final BotID oldId, final BotID newId)
	{
		synchronized (observers)
		{
			for (final IBotManagerObserver observer : observers)
			{
				observer.onBotIdChanged(oldId, newId);
			}
		}
	}
	
	
}
