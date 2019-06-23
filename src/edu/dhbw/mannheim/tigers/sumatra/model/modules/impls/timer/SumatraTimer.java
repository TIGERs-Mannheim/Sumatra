/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 22.07.2010
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.timer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.moduli.exceptions.InitModuleException;
import edu.dhbw.mannheim.tigers.moduli.exceptions.StartModuleException;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.timer.TimerInfo;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ATimer;
import edu.dhbw.mannheim.tigers.sumatra.util.Dormouse;


/**
 * Timer for Sumatra Modules.
 * 
 * @see ATimer
 * @author Gero
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class SumatraTimer extends ATimer
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final Logger					log			= Logger.getLogger(SumatraTimer.class.getName());
	
	
	private Thread										dormouse;
	private final Map<TimerIdentifier, Long>	startTimes	= new ConcurrentHashMap<TimerIdentifier, Long>();
	private final TimerInfo							timings		= new TimerInfo();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 * @param subnodeConfiguration
	 */
	public SumatraTimer(SubnodeConfiguration subnodeConfiguration)
	{
		
	}
	
	
	// --------------------------------------------------------------------------
	// --- life-cycle -----------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public void initModule() throws InitModuleException
	{
		dormouse = new Thread(Dormouse.getInstance(), "Dormouse");
		dormouse.start();
	}
	
	
	@Override
	public void startModule() throws StartModuleException
	{
	}
	
	
	@Override
	public void stopModule()
	{
		dormouse.interrupt();
		
		synchronized (getObservers())
		{
			getObservers().clear();
		}
		
		timings.clear();
		startTimes.clear();
	}
	
	
	@Override
	public void deinitModule()
	{
	}
	
	
	private void addTiming(TimerIdentifier tId, long time)
	{
		timings.putTiming(tId, time);
	}
	
	
	/**
	 * 
	 * @param timable
	 * @param id
	 */
	public void stop(ETimable timable, long id)
	{
		stop(timable, id, 0);
	}
	
	
	/**
	 * 
	 * @param timable
	 * @param id
	 * @param customId
	 */
	public void stop(ETimable timable, long id, int customId)
	{
		long stopTime = System.nanoTime();
		TimerIdentifier timerId = new TimerIdentifier(timable, id, customId);
		Long startTime = startTimes.remove(timerId);
		if (startTime == null)
		{
			log.error("Asynchron timer: " + timable + " stop called before start or multiple times.");
			return;
		}
		addTiming(timerId, (stopTime - startTime));
	}
	
	
	/**
	 * 
	 * @param timable
	 * @param id
	 */
	public void start(ETimable timable, long id)
	{
		start(timable, id, 0);
	}
	
	
	/**
	 * 
	 * @param timable
	 * @param id
	 * @param customId
	 */
	public void start(ETimable timable, long id, int customId)
	{
		startTimes.put(new TimerIdentifier(timable, id, customId), System.nanoTime());
	}
	
	
	/**
	 * 
	 * @return
	 */
	public TimerInfo getTimerInfo()
	{
		return timings;
	}
}
