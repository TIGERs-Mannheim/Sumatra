/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 02.08.2010
 * Author(s): Gero
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.types;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import edu.dhbw.mannheim.tigers.moduli.AModule;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Referee.SSL_Referee.Command;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.referee.RefereeMsg;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IRefereeObserver;


/**
 * The base class for all referee-implementations
 */
public abstract class AReferee extends AModule implements IRefereeMsgProducer
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/** */
	public static final String					MODULE_TYPE				= "AReferee";
	/** */
	public static final String					MODULE_ID				= "referee";
	
	private List<IRefereeMsgConsumer>		consumers				= new ArrayList<IRefereeMsgConsumer>();
	
	
	private CountDownLatch						startSignal;
	
	private final List<IRefereeObserver>	observers				= new ArrayList<IRefereeObserver>();
	
	
	private boolean								receiveExternalMsg	= true;
	
	
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
	
	
	protected void notifyNewRefereeMsg(final RefereeMsg refMsg)
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
	 * @param cmd
	 * @param goalsBlue
	 * @param goalsYellow
	 * @param timeLeft
	 */
	public abstract void sendOwnRefereeMsg(Command cmd, int goalsBlue, int goalsYellow, short timeLeft);
	
	
	/**
	 * Replace the ball in the simulator
	 * 
	 * @param pos
	 */
	public abstract void replaceBall(IVector2 pos);
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void addRefereeMsgConsumer(final IRefereeMsgConsumer consumer)
	{
		consumers.add(consumer);
		startSignal.countDown();
	}
	
	
	protected final void resetCountDownLatch()
	{
		startSignal = new CountDownLatch(1);
	}
	
	
	/**
	 * @return the consumer
	 */
	public final List<IRefereeMsgConsumer> getConsumers()
	{
		return consumers;
	}
	
	
	/**
	 * @return the startSignal
	 */
	public final CountDownLatch getStartSignal()
	{
		return startSignal;
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
