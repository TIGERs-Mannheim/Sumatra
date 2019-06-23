/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 17.07.2015
 * Author(s): JulianT
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.types;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import edu.dhbw.mannheim.tigers.moduli.AModule;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.MultiTeamMessage;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IMultiTeamMessageObserver;


/**
 * @author JulianT
 */
public abstract class AMultiTeamMessage extends AModule implements IMultiTeamMessageProducer
{
	/**  */
	public static final String								MODULE_TYPE	= "AMultiTeamMessage";
	/** */
	public static final String								MODULE_ID	= "multiTeamMessage";
	
	private List<IMultiTeamMessageConsumer>			consumers	= new ArrayList<>();
	private final List<IMultiTeamMessageObserver>	observers	= new ArrayList<>();
	
	private CountDownLatch									startSignal;
	
	
	/**
	 * @param observer
	 */
	public void addObserver(final IMultiTeamMessageObserver observer)
	{
		observers.add(observer);
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(final IMultiTeamMessageObserver observer)
	{
		synchronized (observers)
		{
			observers.remove(observer);
		}
	}
	
	
	protected void notifyNewMultiTeamMessage(final MultiTeamMessage message)
	{
		synchronized (observers)
		{
			for (final IMultiTeamMessageObserver observer : observers)
			{
				observer.onNewMultiTeamMessage(message);
			}
			for (final IMultiTeamMessageConsumer observer : consumers)
			{
				observer.onNewMultiTeamMessage(message);
			}
		}
	}
	
	
	@Override
	public void addMultiTeamMessageConsumer(final IMultiTeamMessageConsumer consumer)
	{
		consumers.add(consumer);
		startSignal.countDown();
	}
	
	
	/**
	 * @return
	 */
	public final List<IMultiTeamMessageConsumer> getConsumers()
	{
		return consumers;
	}
	
	
	protected final void resetCountDownLatch()
	{
		startSignal = new CountDownLatch(1);
	}
	
	
	/**
	 * @return
	 */
	public final CountDownLatch getStartSignal()
	{
		return startSignal;
	}
}
