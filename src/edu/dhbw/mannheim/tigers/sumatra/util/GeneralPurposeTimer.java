/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 11.09.2010
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.util;

import java.util.Timer;


/**
 * This general purpose timer is intended for tasks
 * that need to be executed regularly but without high
 * demands on the required precision.
 * Use this singleton to keep the overall thread count low.
 * 
 * @author AndreR
 * 
 */
public final class GeneralPurposeTimer extends Timer
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static GeneralPurposeTimer	instance	= null;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	private GeneralPurposeTimer()
	{
		super("GeneralPurposeTimer");
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return
	 */
	public static synchronized GeneralPurposeTimer getInstance()
	{
		if (instance == null)
		{
			instance = new GeneralPurposeTimer();
		}
		
		return instance;
	}
}
