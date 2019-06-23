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

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.CamDetectionFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.FrameID;
import edu.dhbw.mannheim.tigers.sumatra.model.data.TimerInfo;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.ITimerObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ATimer;
import edu.dhbw.mannheim.tigers.sumatra.util.Dormouse;
import edu.dhbw.mannheim.tigers.sumatra.util.StopWatch;
import edu.moduli.exceptions.InitModuleException;
import edu.moduli.exceptions.StartModuleException;


/**
 * This class uses its implementations of the {@link ITimer}-interface to measure the time the single modules of
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
	protected final Logger			log	= Logger.getLogger(getClass());
	
	protected final SumatraModel	model	= SumatraModel.getInstance();
	
	private Thread						dormouse;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
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
		
		log.info("Initialized.");
	}
	

	@Override
	public void startModule() throws StartModuleException
	{
		log.info("Started.");
	}
	

	@Override
	public void stopModule()
	{
		dormouse.interrupt();
		
		synchronized (observers)
		{
			observers.clear();
		}
		
		log.info("Stopped.");
	}
	

	@Override
	public void deinitModule()
	{
		camWatch.reset();
		wpWatch.reset();
		aiWatch.reset();
		
		log.info("Deinitialized.");
	}
	
	
	// --------------------------------------------------------------------------
	// --- main data-flow -------------------------------------------------------
	// --------------------------------------------------------------------------
	/** FPS (~80 max) * measuring-points (3) * seconds (60) * minutes (30) = {@value} */
	private static final int INIT_CAPACITY = 432000;
	/** Capacity calculated:  */
	private final Map<FrameID, TimerInfo>	timings	= new HashMap<FrameID, TimerInfo>(INIT_CAPACITY);
	
	
	/**
	 * Minor memory-leak here!!! ;)
	 * 
	 * @param id
	 * @return
	 */
	private TimerInfo getTimerInfo(FrameID id)
	{
		synchronized (timings)
		{
			TimerInfo result = timings.get(id);
			if (result == null)
			{
				result = new TimerInfo();
				timings.put(id, result);
			}
			return result;
		}
	}
	
	
	// ### Cam
	private final StopWatch	camWatch	= new StopWatch();
	
	
	@Override
	public void time(CamDetectionFrame detnFrame)
	{
		camWatch.stop(detnFrame.tReceived);
		
		// Add current measurement to the correct TimerInfo
		TimerInfo info = getTimerInfo(detnFrame.id);
		info.setCamTiming(camWatch.getCurrentTiming());
	}
	
	
	// ### WP
	private final StopWatch	wpWatch			= new StopWatch();
//	private double				greaterThen2ms	= 0;
//	private double				greaterThen1ms	= 0;
	
	
	@Override
	public void startWP(CamDetectionFrame detnFrame)
	{
		wpWatch.start(new FrameID(detnFrame.cameraId, detnFrame.frameNumber));
	}
	

	@Override
	public void stopWP(WorldFrame wFrame)
	{
		wpWatch.stop(wFrame.id);
//		if (duration > 1000000)
//		{
//			if (duration > 2000000)
//			{
//				greaterThen1ms++;
//				greaterThen2ms++;
//				double ratio = greaterThen2ms / wpWatch.count() * 100;
//				log.debug("WP !!! >2ms #" + wFrame.id + " (=" + duration + "ns, ~" + wpWatch.mean() + "ns, " + ratio + "%)");
//			} else
//			{
//				greaterThen1ms++;
//				double ratio = greaterThen1ms / wpWatch.count() * 100;
//				log.debug("WP !!! >1ms #" + wFrame.id + " (=" + duration + "ns, ~" + wpWatch.mean() + "ns, " + ratio + "%)");
//			}
//		}
		
		// Add current measurement to the correct TimerInfo
		TimerInfo timer = getTimerInfo(wFrame.id);
		timer.setWpTiming(wpWatch.getCurrentTiming());
	}
	
	
	// ### AI
	private final StopWatch	aiWatch	= new StopWatch();
	private long				aiCount	= 0;
	
	
	@Override
	public void startAI(WorldFrame wFrame)
	{
		aiWatch.start(wFrame.id);
	}
	

	@Override
	public void stopAI(AIInfoFrame aiFrame)
	{
		aiCount++;
		aiWatch.stop(aiFrame.worldFrame.id);
//		if (0 == aiCount % 100) // Every 100th time...
//		{
//			log.debug("AI: " + stop + "ns (Max: " + aiWatch.max() + "ns/Mean: " + aiWatch.mean() + "ns)");
//		}
		
		// Add current measurement to the correct TimerInfo
		TimerInfo info = getTimerInfo(aiFrame.worldFrame.id);
		info.setAiTiming(aiWatch.getCurrentTiming());
		
		// Notify observers
		if (info.isFull())
		{
			notifyNewTimerInfo(info);
		}
	}
	

	// ### Skill received in skillsystem
	@Override
	public void time(FrameID wfID)
	{
		
	}
	

	private void notifyNewTimerInfo(TimerInfo info)
	{
		synchronized (observers)
		{
			for (ITimerObserver observer : observers)
			{
				observer.onNewTimerInfo(info);
			}
		}
	}
}
