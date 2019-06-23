/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 02.08.2010
 * Author(s): Gero
 * *********************************************************
 */
package edu.tigers.sumatra.referee;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import edu.tigers.moduli.AModule;
import edu.tigers.sumatra.Referee.SSL_Referee;
import edu.tigers.sumatra.Referee.SSL_Referee.Command;
import edu.tigers.sumatra.cam.IBallReplacer;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.IVector3;


/**
 * The base class for all referee-implementations
 */
public abstract class AReferee extends AModule implements IBallReplacer
{
	/** */
	public static final String					MODULE_TYPE				= "AReferee";
	/** */
	public static final String					MODULE_ID				= "referee";
	
	private final List<IRefereeObserver>	observers				= new CopyOnWriteArrayList<>();
	
	private boolean								receiveExternalMsg	= true;
	private long									lastRefMsgCounter		= 0;
	
	
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
		synchronized (observers)
		{
			observers.add(observer);
		}
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(final IRefereeObserver observer)
	{
		synchronized (observers)
		{
			observers.remove(observer);
		}
	}
	
	
	protected void notifyNewRefereeMsg(final SSL_Referee refMsg)
	{
		for (final IRefereeObserver observer : observers)
		{
			observer.onNewRefereeMsg(refMsg);
		}
	}
	
	
	/**
	 * @param cmd
	 * @param goalsBlue
	 * @param goalsYellow
	 * @param timeLeft
	 * @param timestamp
	 * @param placementPos
	 */
	public abstract void sendOwnRefereeMsg(Command cmd, int goalsBlue, int goalsYellow, int timeLeft,
			final long timestamp, IVector2 placementPos);
	
	
	/**
	 * Replace the ball in the simulator
	 * 
	 * @param pos
	 */
	@Override
	public abstract void replaceBall(IVector3 pos, IVector3 vel);
	
	
	/**
	 * @param msg The recently received message
	 * @return Whether this message does really new game-state information
	 */
	protected boolean isNewMessage(final SSL_Referee msg)
	{
		if (msg.getCommandCounter() != lastRefMsgCounter)
		{
			lastRefMsgCounter = msg.getCommandCounter();
			return true;
		}
		
		return false;
	}
	
	
	/**
	 * @return the receiveExternalMsg
	 */
	public boolean isReceiveExternalMsg()
	{
		return receiveExternalMsg;
	}
	
	
	/**
	 * @param receiveExternalMsg the receiveExternalMsg to set
	 */
	public void setReceiveExternalMsg(final boolean receiveExternalMsg)
	{
		this.receiveExternalMsg = receiveExternalMsg;
	}
}
