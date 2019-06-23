/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botmanager;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import edu.tigers.moduli.AModule;
import edu.tigers.sumatra.botmanager.basestation.IBaseStation;
import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.ids.BotID;


/**
 * A module that is capable of managing and controlling all our BattleMechs! =)
 * 
 * @author Gero
 */
public abstract class ABotManager extends AModule implements IBotProvider
{
	private final List<IBotManagerObserver> observers = new CopyOnWriteArrayList<>();
	
	
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
		observers.add(o);
	}
	
	
	/**
	 * @param o
	 */
	public void removeObserver(final IBotManagerObserver o)
	{
		observers.remove(o);
	}
	
	
	protected void notifyBotAdded(final ABot bot)
	{
		for (final IBotManagerObserver o : observers)
		{
			o.onBotAdded(bot);
		}
	}
	
	
	protected void notifyBotRemoved(final ABot bot)
	{
		for (final IBotManagerObserver o : observers)
		{
			o.onBotRemoved(bot);
		}
	}
	
	
	protected void notifyIncomingBotCommand(ABot bot, ACommand command)
	{
		for (final IBotManagerObserver o : observers)
		{
			o.onIncomingBotCommand(bot, command);
		}
	}
}
