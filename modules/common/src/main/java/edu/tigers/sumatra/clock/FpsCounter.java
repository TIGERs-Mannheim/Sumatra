/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.clock;

/**
 * Simple FPS Counter
 */
public class FpsCounter
{
	private static final double TIME_WINDOW = 0.5;
	private long lastTime = 0;
	private double fps = 0;
	private int counter = 0;


	/**
	 * Signal for new frame. Call this each time, a new frame comes in
	 *
	 * @param timestamp
	 * @return
	 */
	public boolean newFrame(final long timestamp)
	{
		boolean fpsChanged = false;
		if (timestamp < lastTime)
		{
			reset();
		}
		double timeDiff = (timestamp - lastTime) / 1e9;
		if (timeDiff > TIME_WINDOW)
		{
			double newFps = counter / timeDiff;
			fpsChanged = Math.abs(fps - newFps) > 0.1;
			fps = newFps;
			lastTime = timestamp;
			counter = 0;
		}
		counter++;
		return fpsChanged;
	}


	/**
	 * Reset counter
	 */
	public void reset()
	{
		counter = 0;
		lastTime = 0;
		fps = 0;
	}


	/**
	 * Returns the average fps
	 *
	 * @return
	 */
	public double getAvgFps()
	{
		return fps;
	}
}
