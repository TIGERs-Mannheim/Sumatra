/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 02.08.2010
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.types;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Referee.SSL_Referee.Command;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.referee.RefereeMsg;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IRefereeObserver;
import edu.moduli.AModule;


/**
 * The base class for all referee-implementations
 */
public abstract class AReferee extends AModule implements IRefereeMsgProducer
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/** */
	public static final String					MODULE_TYPE	= "AReferee";
	/** */
	public static final String					MODULE_ID	= "referee";
	
	private IRefereeMsgConsumer				consumer;
	
	
	private CountDownLatch						startSignal;
	
	private final List<IRefereeObserver>	observers	= new ArrayList<IRefereeObserver>();
	
	
	/**
	  * 
	  */
	public AReferee()
	{
		resetCountDownLatch();
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 * @param observer
	 */
	public void addObserver(IRefereeObserver observer)
	{
		synchronized (observers)
		{
			observers.add(observer);
		}
	}
	
	
	/**
	 * 
	 * @param observer
	 */
	public void removeObserver(IRefereeObserver observer)
	{
		synchronized (observers)
		{
			observers.remove(observer);
		}
	}
	
	
	protected void notifyNewRefereeMsg(RefereeMsg refMsg)
	{
		synchronized (observers)
		{
			for (final IRefereeObserver observer : observers)
			{
				observer.onNewRefereeMsg(refMsg);
			}
		}
	}
	
	
	/**
	 * 
	 * @param id
	 * @param cmd
	 * @param goalsBlue
	 * @param goalsYellow
	 * @param timeLeft
	 */
	public abstract void sendOwnRefereeMsg(int id, Command cmd, int goalsBlue, int goalsYellow, short timeLeft);
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void setRefereeMsgConsumer(IRefereeMsgConsumer consumer)
	{
		this.consumer = consumer;
		startSignal.countDown();
	}
	
	
	protected final void resetCountDownLatch()
	{
		startSignal = new CountDownLatch(1);
	}
	
	
	/**
	 * @return the consumer
	 */
	public final IRefereeMsgConsumer getConsumer()
	{
		return consumer;
	}
	
	
	/**
	 * @return the startSignal
	 */
	public final CountDownLatch getStartSignal()
	{
		return startSignal;
	}
}
