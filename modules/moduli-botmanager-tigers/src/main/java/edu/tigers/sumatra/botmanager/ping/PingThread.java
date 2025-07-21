/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Dec 1, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.botmanager.ping;

import edu.tigers.sumatra.botmanager.bots.CommandBasedBot;
import edu.tigers.sumatra.botmanager.commands.tiger.TigerSystemPing;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * Thread for performing pings to a bot
 */
public class PingThread implements Runnable
{
	private static final long UPDATE_RATE = 100000000;
	
	private final List<IPingThreadObserver> observers = new CopyOnWriteArrayList<>();
	
	private final List<PingDatum> pings = new LinkedList<>();
	private final List<PingDatum> completed = new LinkedList<>();
	
	private final int payloadSize;
	private final CommandBasedBot bot;
	
	
	private int id = 0;
	private long lastStatTime = 0;
	
	
	public PingThread(final int payloadSize, final CommandBasedBot bot)
	{
		this.payloadSize = payloadSize;
		this.bot = bot;
	}
	
	
	/**
	 * @param observer
	 */
	public void addObserver(final IPingThreadObserver observer)
	{
		observers.add(observer);
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(final IPingThreadObserver observer)
	{
		observers.remove(observer);
	}
	
	
	private void notifyNewPingStats(final PingStats pingStats)
	{
		for (IPingThreadObserver observer : observers)
		{
			observer.onNewPingStats(pingStats);
		}
	}
	
	
	@Override
	public void run()
	{
		synchronized (pings)
		{
			pings.add(new PingDatum(id));
		}
		
		bot.sendCommand(new TigerSystemPing(id, payloadSize));
		id++;
		
		synchronized (pings)
		{
			while (!pings.isEmpty())
			{
				PingDatum dat = pings.get(0);
				
				if ((System.nanoTime() - dat.startTime) < 1000000000)
				{
					break;
				}
				
				dat.endTime = System.nanoTime();
				dat.delay = (dat.endTime - dat.startTime) / 1e9;
				dat.lost = true;
				
				pings.remove(0);
				
				completed.add(dat);
			}
		}
		
		processCompleted();
	}
	
	
	/**
	 * @param id
	 */
	public void pongArrived(final int id)
	{
		PingDatum dat;
		
		synchronized (pings)
		{
			while (!pings.isEmpty())
			{
				dat = pings.remove(0);
				
				dat.endTime = System.nanoTime();
				dat.delay = (dat.endTime - dat.startTime) / 1e9;
				
				completed.add(dat);
				
				if (dat.id == id)
				{
					break;
				}
				
				dat.lost = true;
			}
		}
		
		processCompleted();
	}
	
	
	private synchronized void processCompleted()
	{
		PingDatum dat;
		
		while (!completed.isEmpty())
		{
			dat = completed.get(0);
			
			if ((System.nanoTime() - dat.endTime) > 1e9)
			{
				completed.remove(0);
			} else
			{
				break;
			}
		}
		
		if ((System.nanoTime() - lastStatTime) > UPDATE_RATE)
		{
			lastStatTime = System.nanoTime();
			
			PingStats stats = new PingStats();
			
			for (PingDatum d : completed)
			{
				if (d.delay < stats.minDelay)
				{
					stats.minDelay = d.delay;
				}
				
				if (d.delay > stats.maxDelay)
				{
					stats.maxDelay = d.delay;
				}
				
				stats.avgDelay += d.delay;
				
				if (d.lost)
				{
					stats.lostPings++;
				}
			}
			
			stats.avgDelay /= completed.size();
			
			if (completed.isEmpty())
			{
				stats.minDelay = 0;
				stats.avgDelay = 0;
				stats.maxDelay = 0;
				stats.lostPings = 0;
			}
			
			notifyNewPingStats(stats);
		}
	}
	
	
	private static class PingDatum
	{
		private long startTime;
		private long endTime;
		private double delay;
		private int id;
		private boolean lost;
		
		
		public PingDatum(final int id)
		{
			this.id = id;
			startTime = System.nanoTime();
			endTime = 0;
			delay = 0;
			lost = false;
		}
	}
	
	/**
	 * Ping thread observer.
	 */
	@FunctionalInterface
	public interface IPingThreadObserver
	{
		/**
		 * @param stats
		 */
		void onNewPingStats(PingStats stats);
	}
	
	
	/**
	 * Remove all observers.
	 */
	public void clearObservers()
	{
		observers.clear();
	}
}
