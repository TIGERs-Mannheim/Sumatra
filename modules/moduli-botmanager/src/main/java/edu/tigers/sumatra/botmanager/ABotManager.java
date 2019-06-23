/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 08.08.2010
 * Author(s): Gero, AndreR
 * *********************************************************
 */
package edu.tigers.sumatra.botmanager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

import edu.tigers.moduli.AModule;
import edu.tigers.sumatra.botmanager.basestation.IBaseStation;
import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.ids.BotID;


/**
 * A module that is capable of managing and controlling all our BattleMechs! =)
 * 
 * @author Gero
 */
public abstract class ABotManager extends AModule
{
	/** */
	public static final String			MODULE_TYPE					= "ABotManager";
	/** */
	public static final String			MODULE_ID					= "botmanager";
																				
																				
	/** */
	public static final String			KEY_BOTMANAGER_CONFIG	= ABotManager.class.getName()
																						+ ".botmanagerConfig";
	/** */
	public static final String			BOTMANAGER_CONFIG_PATH	= "./config/botmanager/";
	/**  */
	public static final String			VALUE_BOTMANAGER_CONFIG	= "botmanager_sumatra.xml";
																				
																				
	private final Map<BotID, ABot>	botTable						= new ConcurrentSkipListMap<BotID, ABot>(
																						BotID.getComparator());
																						
																						
	/**
	 * @param id
	 */
	public abstract void removeBot(BotID id);
	
	
	/**
	 * @param baseStation
	 */
	public abstract void addBasestation(IBaseStation baseStation);
	
	
	/**
	 * @param baseStation
	 */
	public abstract void removeBasestation(IBaseStation baseStation);
	
	
	/**
	 * @return
	 */
	public abstract Map<BotID, ABot> getAllBots();
	
	
	/**
	 * @return
	 */
	public abstract List<IBaseStation> getBaseStations();
	
	
	private final List<IBotManagerObserver> observers = new ArrayList<IBotManagerObserver>();
	
	
	/**
	 */
	public abstract void chargeAll();
	
	
	/**
	 * 
	 */
	public abstract void dischargeAll();
	
	
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
	
	
	/**
	 * @return the botTable
	 */
	public Map<BotID, ABot> getBotTable()
	{
		return botTable;
	}
}
