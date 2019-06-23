/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
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
	private final List<ITimerObserver> observers = new CopyOnWriteArrayList<>();
	
	/**
	 * @param observer
	 */
	public void addObserver(final ITimerObserver observer)
	{
		observers.add(observer);
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(final ITimerObserver observer)
	{
		observers.remove(observer);
	}
	
	
	/**
	 * @return the observers
	 */
	public final List<ITimerObserver> getObservers()
	{
		return observers;
	}
}
