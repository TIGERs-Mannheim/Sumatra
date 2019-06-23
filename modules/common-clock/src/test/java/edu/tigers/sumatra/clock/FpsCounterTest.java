/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 11, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.clock;

import org.junit.Ignore;
import org.junit.Test;

import junit.framework.AssertionFailedError;


/**
 * Test class for FpsCounter
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Ignore
public class FpsCounterTest
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private final FpsCounter	fpsCounter	= new FpsCounter();
	private static final int	NUM_FRAMES	= 500;
	private static final int	SLEEP_TIME	= 16;
	/** tolerance is quite high, because the server seems to be not that fast to reach to desired fps ^^ */
	private static final int	TOLERANCE	= 10;
														
														
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Simple test for fps counter
	 */
	@Test
	public void testSimple()
	{
		for (int i = 0; i < NUM_FRAMES; i++)
		{
			fpsCounter.newFrame(System.nanoTime());
			
			try
			{
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		double desiredFps = 1000.0 / SLEEP_TIME;
		double avgFps = fpsCounter.getAvgFps();
		if ((avgFps < (desiredFps - TOLERANCE)) || (avgFps > (desiredFps + TOLERANCE)))
		{
			throw new AssertionFailedError("FPS is not correct. avg:" + avgFps + " desired:" + desiredFps);
		}
	}
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
