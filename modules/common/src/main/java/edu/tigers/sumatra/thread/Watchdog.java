/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.thread;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * The watchdog monitors timeout events.
 * The watchdog sets a variable that needs to be reset during each watchdog
 * period. If this is not done, the watchdog will call its observers and
 * terminate.
 * 
 * @author AndreR
 */
public class Watchdog
{
	private final List<IWatchdogObserver> observers = new CopyOnWriteArrayList<>();
	private boolean reset = false;
	private int period;
	private Thread watchdogThread = null;
	private final String name;
	
	
	/**
	 * @param period
	 * @param name
	 */
	public Watchdog(final int period, final String name)
	{
		this.period = period;
		this.name = name;
	}
	
	
	public void addObserver(final IWatchdogObserver o)
	{
		observers.add(o);
	}
	
	
	/**
	 * @return
	 */
	public int getPeriod()
	{
		return period;
	}
	
	
	/**
	 * @param period
	 */
	public void setPeriod(final int period)
	{
		this.period = period;
	}
	
	
	/**
	 */
	public void start()
	{
		stop();
		
		watchdogThread = new Thread(new WatchdogRun(), "Watchdog " + name);
		watchdogThread.start();
	}
	
	
	/**
	 * reset the watchdog
	 */
	public void reset()
	{
		reset = true;
	}
	
	
	/**
	 * stop the watchdog
	 */
	public void stop()
	{
		if (watchdogThread != null)
		{
			watchdogThread.interrupt();
			watchdogThread = null;
		}
	}
	
	
	/**
	 * @return
	 */
	public boolean isActive()
	{
		return watchdogThread != null;
	}
	
	
	protected void timeout()
	{
		watchdogThread = null;
		
		for (final IWatchdogObserver o : observers)
		{
			o.onWatchdogTimeout();
		}
	}
	
	protected class WatchdogRun implements Runnable
	{
		@SuppressWarnings("squid:S2583")
		@Override
		public void run()
		{
			while (!Thread.currentThread().isInterrupted())
			{
				reset = false;
				
				try
				{
					Thread.sleep(period);
				} catch (final InterruptedException err)
				{
					Thread.currentThread().interrupt();
				}
				
				if (!reset)
				{
					timeout();
					Thread.currentThread().interrupt();
				}
			}
			
			observers.clear();
		}
	}
}
