/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 29.03.2014
 * Author(s): KaiE
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.exceptions.MathException;
import edu.dhbw.mannheim.tigers.sumatra.util.NamedThreadFactory;


/**
 * triggering a Hash-Map of IFpsTriggered-Objects.
 * 
 * @author KaiE
 */
public class TpsTrigger implements Runnable
{
	
	private Map<String, ITpsTriggered>	notifyTriggered	= new HashMap<>();
	private ScheduledExecutorService		trigger				= null;
	private final String						threadName;
	
	
	/**
	 * Constructor of this class. Pass the amount of times per second you want to trigger all Subscribers
	 * 
	 * @param threadName
	 */
	public TpsTrigger(final String threadName)
	{
		super();
		this.threadName = threadName;
	}
	
	
	/**
	 * Start the Self-triggering
	 * 
	 * @param timesPerSeconds
	 * @throws MathException
	 */
	
	public final void start(final int timesPerSeconds) throws MathException
	{
		if (trigger == null)
		{
			trigger = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory(threadName));
		}
		if (timesPerSeconds == 0)
		{
			throw new MathException();
		}
		trigger.scheduleAtFixedRate(this, 0, 1000 / timesPerSeconds, TimeUnit.MILLISECONDS);
	}
	
	
	/**
	 * Used to stop the Self-triggering
	 */
	public final void stop()
	{
		trigger.shutdown();
		trigger = null;
	}
	
	
	/**
	 * With this method it is possible to add a new Object to the trigger
	 * all added Objects are triggered within the run()-method
	 * 
	 * @param key
	 * @param value
	 */
	public final void addTpsTriggered(final String key, final ITpsTriggered value)
	{
		synchronized (notifyTriggered)
		{
			notifyTriggered.put(key, value);
		}
	}
	
	
	/**
	 * This method is used to remove a subscriber from the publisher.
	 * 
	 * @param key
	 */
	public final void removeTpsTriggered(final String key)
	{
		synchronized (notifyTriggered)
		{
			notifyTriggered.remove(key);
		}
	}
	
	
	@Override
	public void run()
	{
		for (Entry<String, ITpsTriggered> entries : notifyTriggered.entrySet())
		{
			entries.getValue().onElementTriggered();
		}
	}
}
