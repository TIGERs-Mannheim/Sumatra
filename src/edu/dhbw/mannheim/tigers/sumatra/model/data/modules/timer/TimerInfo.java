/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 22.07.2010
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.modules.timer;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import edu.dhbw.mannheim.tigers.sumatra.util.StopWatch.Timing;


/**
 * This is the data structure for information the {@link edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ATimer}
 * has to offer, durations and time measurements in
 * particular
 * 
 * @author Gero
 * 
 */
public class TimerInfo
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private final Map<String, Timing>	timings	= new HashMap<String, Timing>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 *
	 */
	public TimerInfo()
	{
		
	}
	
	
	/**
	 * @param initialKeys
	 */
	public TimerInfo(Collection<String> initialKeys)
	{
		for (String str : initialKeys)
		{
			timings.put(str, new Timing(0, 0, 0, 0, 0, 0));
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 * @param moduleName
	 * @param timing
	 */
	public void addTiming(String moduleName, Timing timing)
	{
		timings.put(moduleName, timing);
	}
	
	
	/**
	 * @return the timings
	 */
	public Map<String, Timing> getTimings()
	{
		return timings;
	}
}
