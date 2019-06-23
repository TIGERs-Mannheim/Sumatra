/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
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
	public static final String						MODULE_TYPE					= "ABotManager";
	/** */
	public static final String						MODULE_ID					= "botmanager";
	
	
	/** */
	public static final String						KEY_BOTMANAGER_CONFIG	= ABotManager.class.getName()
			+ ".botmanagerConfig";
	/** */
	public static final String						BOTMANAGER_CONFIG_PATH	= "./config/botmanager/";
	/**  */
	public static final String						VALUE_BOTMANAGER_CONFIG	= "botmanager_sumatra.xml";
	
	
	private final Map<BotID, ABot>				botTable						= new ConcurrentSkipListMap<>(
			BotID.getComparator());
	
	
	private final List<IBotManagerObserver>	observers					= new ArrayList<>();
	
	
	/**
	 * @param id
	 */
	public abstract void removeBot(BotID id);
	
	
	/**
	 * @param baseStation
	 */
	public abstract void addBasestation(IBaseStation baseStation);
	
	
	/**
	 * @return
	 */
	public abstract Map<BotID, ABot> getAllBots();
	
	
	/**
	 * @return
	 */
	public abstract List<IBaseStation> getBaseStations();
	
	
	/**
	 * Charge kicker of all bots
	 */
	public abstract void chargeAll();
	
	
	/**
	 * Discharge kicker of all bots
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
