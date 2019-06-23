package edu.dhbw.mannheim.tigers.sumatra.util;

import edu.dhbw.mannheim.tigers.sumatra.util.clock.IClock;
import edu.dhbw.mannheim.tigers.sumatra.util.clock.SumatraClock;


/**
 * Very Simple FPS Counter
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class FpsCounter
{
	private static final int	UPDATE_FREQ	= 30;
	private long					lastTime		= 0;
	private float					fps			= 0;
	private int						counter		= 0;
	private int						updateFreq	= UPDATE_FREQ;
	private long					totalFrames	= 0;
	private final IClock			clock;
	
	
	/**
	  * 
	  */
	public FpsCounter()
	{
		clock = null;
	}
	
	
	/**
	 * @param updateFreq how often should the fps be recalculated?
	 */
	public FpsCounter(final int updateFreq)
	{
		this();
		this.updateFreq = updateFreq;
	}
	
	
	/**
	 * @param clock
	 */
	public FpsCounter(final IClock clock)
	{
		this.clock = clock;
	}
	
	
	/**
	 * Signal for new frame. Call this each time, a new frame comes in
	 */
	public void newFrame()
	{
		if (counter >= updateFreq)
		{
			long curTime = clock == null ? SumatraClock.nanoTime() : clock.nanoTime();
			fps = updateFreq / ((curTime - lastTime) / 1e9f);
			lastTime = curTime;
			counter = 0;
		}
		counter++;
		totalFrames++;
	}
	
	
	/**
	 * Returns the average fps
	 * 
	 * @return
	 */
	public float getAvgFps()
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
