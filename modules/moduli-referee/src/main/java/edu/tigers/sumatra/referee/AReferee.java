/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.referee;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import edu.tigers.moduli.AModule;
import edu.tigers.sumatra.Referee.SSL_Referee;
import edu.tigers.sumatra.referee.control.Event;
import edu.tigers.sumatra.referee.control.GcEventFactory;
import edu.tigers.sumatra.referee.source.ARefereeMessageSource;
import edu.tigers.sumatra.referee.source.ERefereeMessageSource;


/**
 * The base class for all referee-implementations
 */
public abstract class AReferee extends AModule
{
	private final List<IRefereeObserver> observers = new CopyOnWriteArrayList<>();
	
	
	/**
	  * 
	  */
	public AReferee()
	{
	}
	
	
	/**
	 * @param observer
	 */
	public void addObserver(final IRefereeObserver observer)
	{
		observers.add(observer);
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(final IRefereeObserver observer)
	{
		observers.remove(observer);
	}
	
	
	/**
	 * Send an event to the game controller. Use {@link GcEventFactory} to create new events
	 */
	public abstract void sendGameControllerEvent(Event event);
	
	
	/**
	 * Internal use only.
	 * 
	 * @param refMsg
	 */
	protected void notifyNewRefereeMsg(final SSL_Referee refMsg)
	{
		for (final IRefereeObserver observer : observers)
		{
			observer.onNewRefereeMsg(refMsg);
		}
	}
	
	
	protected void notifyRefereeMsgSourceChanged(final ARefereeMessageSource src)
	{
		for (IRefereeObserver observer : observers)
		{
			observer.onRefereeMsgSourceChanged(src);
		}
	}
	
	
	/**
	 * Get active referee message source.
	 * 
	 * @return
	 */
	public abstract ARefereeMessageSource getActiveSource();
	
	
	/**
	 * Get a specific message source.
	 * 
	 * @param type
	 * @return
	 */
	public abstract ARefereeMessageSource getSource(ERefereeMessageSource type);
	
	
	/**
	 * @return true, if the referee can be controlled locally
	 */
	public abstract boolean isControllable();
	
	
	/**
	 * Set the current time for the referee (the game-controller) if possible
	 * 
	 * @param timestamp
	 */
	public abstract void setCurrentTime(long timestamp);
	
	
	/**
	 * Reset and initialize the game controller.
	 */
	public abstract void initGameController();
}
