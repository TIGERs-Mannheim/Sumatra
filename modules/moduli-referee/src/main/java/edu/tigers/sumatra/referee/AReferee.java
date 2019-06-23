/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.referee;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import edu.tigers.moduli.AModule;
import edu.tigers.sumatra.RefboxRemoteControl.SSL_RefereeRemoteControlRequest;
import edu.tigers.sumatra.Referee.SSL_Referee;
import edu.tigers.sumatra.ids.BotID;
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
	 * Handle an external control request.
	 * 
	 * @param request
	 */
	public abstract void handleControlRequest(final SSL_RefereeRemoteControlRequest request);
	
	
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
	 * Set a specific message source.
	 * 
	 * @param type
	 */
	public abstract void setActiveSource(final ERefereeMessageSource type);
	
	
	/**
	 * Update a keeper id
	 *
	 * @param keeperId
	 */
	public abstract void updateKeeperId(BotID keeperId);
}
