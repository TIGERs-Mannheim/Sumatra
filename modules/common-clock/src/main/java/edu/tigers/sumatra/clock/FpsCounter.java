/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.clock;

/**
 * Very Simple FPS Counter
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class FpsCounter
{
	private static final int	UPDATE_FREQ	= 10;
	private long					lastTime		= 0;
	private double					fps			= 0;
	private int						counter		= 0;
	private int						updateFreq	= UPDATE_FREQ;
	private long					totalFrames	= 0;
	
	
	/**
	 * Default fps counter
	 */
	public FpsCounter()
	{
		this(UPDATE_FREQ);
	}
	
	
	/**
	 * @param updateFreq how often should the fps be recalculated?
	 */
	public FpsCounter(final int updateFreq)
	{
		this.updateFreq = updateFreq;
	}
	
	
	/**
	 * Signal for new frame. Call this each time, a new frame comes in
	 * 
	 * @param timestamp
	 * @return
	 */
	public boolean newFrame(final long timestamp)
	{
		boolean fpsChanged = false;
		if (counter >= updateFreq)
		{
			double newFps = updateFreq / ((timestamp - lastTime) / 1e9);
			fpsChanged = Math.abs(fps - newFps) > 0.01;
			fps = newFps;
			lastTime = timestamp;
			counter = 0;
		}
		counter++;
		totalFrames++;
		return fpsChanged;
	}
	
	
	/**
	 * Reset counter
	 */
	public void reset()
	{
		counter = 0;
		totalFrames = 0;
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
	
	
	/**
	 * @return the totalFrames
	 */
	public final long getTotalFrames()
	{
		return totalFrames;
	}
}
