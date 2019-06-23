/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.timer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.clock.Dormouse;


/**
 * Timer for Sumatra Modules.
 * 
 * @see ATimer
 * @author Gero
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class SumatraTimer extends ATimer
{
	private static final Logger log = Logger.getLogger(SumatraTimer.class.getName());
	private final Map<TimerIdentifier, Long> startTimes = new ConcurrentHashMap<>();
	private final TimerInfo timings = new TimerInfo();
	private Thread dormouse;
	
	@Override
	public void initModule()
	{
		dormouse = new Thread(Dormouse.getInstance(), "Dormouse");
		dormouse.start();
	}
	
	
	@Override
	public void startModule()
	{
		// nothing to do
	}
	
	
	@Override
	public void stopModule()
	{
		dormouse.interrupt();
		
		getObservers().clear();
		
		timings.clear();
		startTimes.clear();
	}
	
	
	@Override
	public void deinitModule()
	{
		// nothing to do
	}
	
	
	private void addTiming(final TimerIdentifier tId, final long time)
	{
		timings.putTiming(tId, time);
	}
	
	
	/**
	 * @param timable
	 * @param id
	 */
	@Override
	public void stop(final String timable, final long id)
	{
		stop(timable, id, 0);
	}
	
	
	/**
	 * @param timable
	 * @param id
	 * @param customId
	 */
	@Override
	public void stop(final String timable, final long id, final int customId)
	{
		long stopTime = System.nanoTime();
		TimerIdentifier timerId = new TimerIdentifier(timable, id, customId);
		Long startTime = startTimes.remove(timerId);
		if (startTime == null)
		{
			log.error("Asynchron timer: " + timable + " stop called before start or multiple times.");
			return;
		}
		addTiming(timerId, stopTime - startTime);
	}
	
	
	/**
	 * @param timable
	 * @param id
	 */
	@Override
	public void start(final String timable, final long id)
	{
		start(timable, id, 0);
	}
	
	
	/**
	 * @param timable
	 * @param id
	 * @param customId
	 */
	@Override
	public void start(final String timable, final long id, final int customId)
	{
		startTimes.put(new TimerIdentifier(timable, id, customId), System.nanoTime());
	}
	
	
	/**
	 * @return
	 */
	public TimerInfo getTimerInfo()
	{
		return timings;
	}
}
