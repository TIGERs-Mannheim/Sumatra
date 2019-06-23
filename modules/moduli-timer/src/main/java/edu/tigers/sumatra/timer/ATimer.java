/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 22.07.2010
 * Author(s): Gero
 * *********************************************************
 */
package edu.tigers.sumatra.timer;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import edu.tigers.moduli.AModule;


/**
 * This is the base class for all timer-module implementations (there should be only one, though...). It watches the
 * packets on their way provide exact measurements and profound timing
 * advices
 * 
 * @author Gero
 */
public abstract class ATimer extends AModule
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/** */
	public static final String				MODULE_TYPE	= "ATimer";
	/** */
	public static final String				MODULE_ID	= "timer";
	
	
	private final List<ITimerObserver>	observers	= new CopyOnWriteArrayList<ITimerObserver>();
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param observer
	 */
	public void addObserver(final ITimerObserver observer)
	{
		synchronized (observers)
		{
			observers.add(observer);
		}
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(final ITimerObserver observer)
	{
		synchronized (observers)
		{
			observers.remove(observer);
		}
	}
	
	
	/**
	 * @return the observers
	 */
	public final synchronized List<ITimerObserver> getObservers()
	{
		return observers;
	}
}
