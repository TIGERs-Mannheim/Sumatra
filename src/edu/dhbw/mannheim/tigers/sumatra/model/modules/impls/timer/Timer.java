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

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.FrameID;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.timer.TimerInfo;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.ITimerObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ATimer;
import edu.dhbw.mannheim.tigers.sumatra.util.Dormouse;
import edu.dhbw.mannheim.tigers.sumatra.util.StopWatch;
import edu.moduli.exceptions.InitModuleException;
import edu.moduli.exceptions.StartModuleException;


/**
 * This class uses its implementations of the {@link edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ITimer}
 * -interface to measure the time the single modules of
 * Sumatra. The problem is, that these measurements/timings are asynchronous, so there is a need for synchronization.
 * 
 * @see ATimer
 * @author Gero
 */
public class Timer extends ATimer
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger						log			= Logger.getLogger(Timer.class.getName());
	
	protected final SumatraModel						model			= SumatraModel.getInstance();
	
	private Thread											dormouse;
	
	private final Map<String, StopWatch>			stopWatches	= new HashMap<String, StopWatch>();
	
	/** Capacity calculated: */
	private final SortedMap<FrameID, TimerInfo>	timings		= new TreeMap<FrameID, TimerInfo>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 * @param subnodeConfiguration
	 */
	public Timer(SubnodeConfiguration subnodeConfiguration)
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
		
		log.debug("Initialized.");
	}
	
	
	@Override
	public void startModule() throws StartModuleException
	{
		log.debug("Started.");
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
		
		log.debug("Stopped.");
	}
	
	
	@Override
	public void deinitModule()
	{
		for (final StopWatch stopWatch : stopWatches.values())
		{
			stopWatch.reset();
		}
		
		log.debug("Deinitialized.");
	}
	
	
	// --------------------------------------------------------------------------
	// --- main data-flow -------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @param id
	 * @return
	 */
	private TimerInfo getTimerInfo(FrameID id)
	{
		if (id == null)
		{
			log.error("FrameID for timerinfo was null");
			return new TimerInfo(stopWatches.keySet());
		}
		synchronized (timings)
		{
			TimerInfo result = timings.get(id);
			if (result == null)
			{
				result = new TimerInfo(stopWatches.keySet());
				timings.put(id, result);
			}
			return result;
		}
	}
	
	
	@Override
	public void notifyNewTimerInfo(FrameID frameId)
	{
		synchronized (getObservers())
		{
			for (final ITimerObserver observer : getObservers())
			{
				observer.onNewTimerInfo(getTimerInfo(frameId));
			}
			synchronized (timings)
			{
				timings.remove(frameId);
			}
		}
	}
	
	
	@Override
	public void stop(String moduleName, FrameID frameId)
	{
		final StopWatch stopWatch = stopWatches.get(moduleName);
		if (stopWatch != null)
		{
			stopWatch.stop(frameId);
			final TimerInfo info = getTimerInfo(frameId);
			info.addTiming(moduleName, stopWatch.getCurrentTiming());
		}
	}
	
	
	@Override
	public void start(String moduleName, FrameID frameId)
	{
		StopWatch stopWatch = stopWatches.get(moduleName);
		if (stopWatch == null)
		{
			stopWatch = new StopWatch();
			stopWatches.put(moduleName, stopWatch);
		}
		stopWatch.start(frameId);
	}
}
