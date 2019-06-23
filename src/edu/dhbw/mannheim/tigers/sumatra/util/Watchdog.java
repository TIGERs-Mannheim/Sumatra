/* 
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 21.08.2010
 * Author(s): AndreR
 *
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.util;

import java.util.ArrayList;
import java.util.List;

/**
 * The watchdog monitors timeout events.
 * 
 * The watchdog sets a variable that needs to be reset during each watchdog
 * period. If this is not done, the watchdog will call its observers and
 * terminate.
 * 
 * @author AndreR
 * 
 */
public class Watchdog
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private List<IWatchdogObserver> observers = new ArrayList<IWatchdogObserver>();
	private boolean reset = false;
	private int period = 1000;
	private Thread watchdogThread = null;

	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public Watchdog()
	{
	}
	
	public Watchdog(int period)
	{
		this.period = period;
	}
	
	public Watchdog(int period, IWatchdogObserver o)
	{
		this.period = period;
		
		addObserver(o);
	}

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	private void addObserver(IWatchdogObserver o)
	{
		synchronized(observers)
		{
			observers.add(o);
		}
	}
		
	public int getPeriod()
	{
		return period;
	}

	public void setPeriod(int period)
	{
		this.period = period;
	}
	
	public void start(IWatchdogObserver o)
	{
		stop();
		
		addObserver(o);
		
		watchdogThread = new Thread(new WatchdogRun());
		watchdogThread.start();
	}

	public void reset()
	{
		reset = true;
	}
	
	public void stop()
	{
		if(watchdogThread != null)
		{
			watchdogThread.interrupt();
			watchdogThread = null;
		}
	}
	
	public boolean isActive()
	{
		return (watchdogThread != null);
	}
	
	protected void timeout()
	{
		watchdogThread = null;
		
		synchronized(observers)
		{
			for(IWatchdogObserver o : observers)
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
			Thread.currentThread().setName("Watchdog");
			
			while(!Thread.currentThread().isInterrupted())
			{
				reset = false;
				
				try
				{
					Thread.sleep(period);
				}
				catch (InterruptedException err)
				{
					Thread.currentThread().interrupt();
				}
				
				if(!reset)
				{
					timeout();
					Thread.currentThread().interrupt();
				}
			}
			
			observers.clear();
		}
	}
}
