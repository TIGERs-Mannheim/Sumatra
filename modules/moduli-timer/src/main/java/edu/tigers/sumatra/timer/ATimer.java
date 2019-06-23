/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
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
public abstract class ATimer extends AModule implements ITimer
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/** */
	public static final String				MODULE_TYPE	= "ATimer";
	/** */
	public static final String				MODULE_ID	= "timer";
	
	
	private final List<ITimerObserver>	observers	= new CopyOnWriteArrayList<>();
	
	
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
