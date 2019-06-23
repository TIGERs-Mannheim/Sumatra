/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 25.07.2010
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.util;

import java.util.HashMap;
import java.util.Map;


/**
 * A simple/naive implementation of a stop-watch for nanoTime()-measurement.
 * 
 * @author Gero
 * 
 */
public class StopWatch
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long			NS_IN_S					= 1000000000;
	private static final long			CPS_UPDATE_INTERVAL	= 500000000;
	
	
	private final Map<Object, Long>	series					= new HashMap<Object, Long>();
	private final Object					sync						= new Object();
	
	private long							cumulative				= 0;
	private long							count						= 0;
	private long							maximum					= 0;
	
	private long							mean						= 0;
	private long							start						= 0;
	private long							end						= 0;
	private long							lastDuration			= 0;
	
	private long							lastCPSPoint			= 0;
	private long							cpsSize					= 0;
	private float							cps						= -1;
	
	private int								idCounter				= 0;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- mutators -------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Use only if you're sure that start() and stop() are called synchronously, otherwise use {@link #start(Object)}
	 * respectively {@link #stop(Object)} with external identifiers.
	 */
	public void start()
	{
		synchronized (sync)
		{
			final Long startTime = System.nanoTime();
			start(startTime, idCounter);
		}
	}
	
	
	/**
	 * Gives you the possibility to associate asynchronous measurements via an identifier (If you're sure that
	 * start(...) and stop(...) are called synchronously you may want to use {@link #start()}/{@link #stop()}).
	 * @param id
	 */
	public void start(Object id)
	{
		synchronized (sync)
		{
			final Long startTime = System.nanoTime();
			start(startTime, id);
		}
	}
	
	
	private void start(Long startTime, Object id)
	{
		series.put(id, startTime);
	}
	
	
	/**
	 * Use only if you're sure that start() and stop() are called synchronously, otherwise use {@link #start(Object)}
	 * respectively {@link #stop(Object)} with external identifiers.
	 * @return
	 */
	public long stop()
	{
		synchronized (sync)
		{
			final long endTime = System.nanoTime();
			idCounter++;
			final Long startTime = series.remove(idCounter);
			return stop(startTime, endTime);
		}
	}
	
	
	/**
	 * Gives you the possibility to associate asynchronous measurements via an identifier (If you're sure that
	 * start(...) and stop(...) are called synchronously you may want to use {@link #start()}/{@link #stop()}).
	 * @param id
	 * @return The time between the call to start and now. <strong>-1, if <i>id</i> is unknown.</strong>
	 */
	public long stop(Object id)
	{
		synchronized (sync)
		{
			final long endTime = System.nanoTime();
			final Long startTime = series.remove(id);
			return stop(startTime, endTime);
		}
	}
	
	
	/**
	 * Use this method, if you want to use the benefits of this class, but the start-point of your timing is measured
	 * somewhere else.
	 * 
	 * @param startTime The time when the measurement started, in nanoseconds (system-time)
	 * @return The time between the call to start and now.
	 */
	public long stop(long startTime)
	{
		synchronized (sync)
		{
			final long endTime = System.nanoTime();
			return stop(startTime, endTime);
		}
	}
	
	
	private long stop(Long startTime, long endTime)
	{
		if (startTime == null)
		{
			// No start time in #series, unknown
			return -1;
		}
		
		// Save to provide getCurrentTiming access!
		start = startTime;
		end = endTime;
		
		final long duration = endTime - startTime;
		
		if (duration > maximum)
		{
			maximum = duration;
		}
		
		cumulative += duration;
		count++;
		
		mean = count == 0 ? -1 : cumulative / count;
		
		lastDuration = duration;
		
		return duration;
	}
	
	
	/**
	 * @return Calls per second
	 */
	public float cps()
	{
		synchronized (sync)
		{
			final long now = System.nanoTime();
			if ((now - lastCPSPoint) > CPS_UPDATE_INTERVAL)
			{
				cps = (count - cpsSize) * (NS_IN_S / CPS_UPDATE_INTERVAL);
				
				lastCPSPoint = now;
				cpsSize = count;
			}
			return cps;
		}
	}
	
	
	/**
	 * Deletes all measurements and resets all values to <code>0</code>
	 */
	public void reset()
	{
		synchronized (sync)
		{
			series.clear();
			
			cumulative = 0;
			idCounter = 0;
			maximum = 0;
			count = 0;
			
			cps = 0;
			cpsSize = 0;
			lastCPSPoint = 0;
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 * @return
	 */
	public long mean()
	{
		synchronized (sync)
		{
			return mean;
		}
	}
	
	
	/**
	 * 
	 * @return
	 */
	public long max()
	{
		synchronized (sync)
		{
			return maximum;
		}
	}
	
	
	/**
	 * 
	 * @return
	 */
	public long count()
	{
		synchronized (sync)
		{
			return count;
		}
	}
	
	
	/**
	 * 
	 * @return
	 */
	public Timing getCurrentTiming()
	{
		synchronized (sync)
		{
			return new Timing(start, end, lastDuration, maximum, count, mean, cps());
		}
	}
	
	
	/**
	 * This class is a data-holder for the values of a certain measurement
	 * 
	 * @author Gero
	 */
	public static class Timing
	{
		/** */
		public final long		start;
		/** */
		public final long		end;
		/** */
		public final long		duration;
		/** */
		public final long		maximum;
		/** */
		public final long		count;
		/** */
		public final long		mean;
		/** */
		public final float	cps;
		
		
		/**
		 * @param start
		 * @param end
		 * @param duration
		 * @param maximum
		 * @param count
		 * @param mean
		 * @param cps
		 */
		public Timing(long start, long end, long duration, long maximum, long count, long mean, float cps)
		{
			this.start = start;
			this.end = end;
			this.duration = duration;
			this.maximum = maximum;
			this.count = count;
			this.mean = mean;
			this.cps = cps;
		}
		
		
		/**
		 * @param start
		 * @param end
		 * @param duration
		 * @param maximum
		 * @param count
		 * @param mean
		 */
		public Timing(long start, long end, long duration, long maximum, long count, long mean)
		{
			this.start = start;
			this.end = end;
			this.duration = duration;
			this.maximum = maximum;
			this.count = count;
			this.mean = mean;
			cps = -1;
		}
	}
}
