/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 22.07.2010
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ATimer;
import edu.dhbw.mannheim.tigers.sumatra.util.StopWatch.Timing;


/**
 * This is the data structure for information the {@link ATimer} has to offer, durations and time measurements in
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
	
	
	private Timing camTiming;
	private Timing wpTiming;
	private Timing aiTiming;
	
	/** Whether the {@link TimerInfo} has been decorated with a {@link Timing} for every module */
	private boolean filled = false;
	

	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public TimerInfo(Timing camTiming, Timing wpTiming, Timing aiTiming)
	{
		this.wpTiming = wpTiming;
		this.camTiming = camTiming;
		this.aiTiming = aiTiming;
	}
	
	
	public TimerInfo()
	{
		
	}
	
	
	protected void updateState()
	{
		filled = camTiming != null && wpTiming != null && aiTiming != null;
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	public Timing getCamTiming()
	{
		return camTiming;
	}


	public void setCamTiming(Timing camTiming)
	{
		this.camTiming = camTiming;
		updateState();
	}


	public Timing getWpTiming()
	{
		return wpTiming;
	}


	public void setWpTiming(Timing wpTiming)
	{
		this.wpTiming = wpTiming;
		updateState();
	}


	public Timing getAiTiming()
	{
		return aiTiming;
	}


	public void setAiTiming(Timing aiTiming)
	{
		this.aiTiming = aiTiming;
		updateState();
	}
	
	
	/**
	 * @return {@link #filled}
	 */
	public boolean isFull()
	{
		return filled;
	}
}
