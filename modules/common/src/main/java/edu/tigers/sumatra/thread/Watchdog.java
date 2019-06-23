/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 21.08.2010
 * Author(s): AndreR
 * *********************************************************
 */
package edu.tigers.sumatra.thread;

import java.util.ArrayList;
import java.util.List;


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
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final List<IWatchdogObserver>	observers		= new ArrayList<IWatchdogObserver>();
	private boolean								reset				= false;
	private int										period			= 1000;
	private Thread									watchdogThread	= null;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
 * 
 */
	public Watchdog()
	{
	}
	
	
	/**
	 * @param period
	 */
	public Watchdog(final int period)
	{
		this.period = period;
	}
	
	
	/**
	 * @param period
	 * @param o
	 */
	public Watchdog(final int period, final IWatchdogObserver o)
	{
		this.period = period;
		
		addObserver(o);
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	private void addObserver(final IWatchdogObserver o)
	{
		synchronized (observers)
		{
			observers.add(o);
		}
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
	 * @param o
	 */
	public void start(final IWatchdogObserver o)
	{
		stop();
		
		addObserver(o);
		
		watchdogThread = new Thread(new WatchdogRun(), "Watchdog " + o.getName());
		watchdogThread.start();
	}
	
	
	/**
	 *
	 */
	public void reset()
	{
		reset = true;
	}
	
	
	/**
	 * 
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
		return (watchdogThread != null);
	}
	
	
	protected void timeout()
	{
		watchdogThread = null;
		
		synchronized (observers)
		{
			for (final IWatchdogObserver o : observers)
			{
				o.onWatchdogTimeout();
			}
		}
	}
	
	protected class WatchdogRun implements Runnable
	{
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
