package edu.dhbw.mannheim.tigers.sumatra.util;


/**
 * Very Simple FPS Counter
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class FpsCounter
{
	private static final int	UPDATE_FREQ	= 30;
	private long					lastTime		= 0;
	private float					fps			= 0;
	private int						counter		= 0;
	private int						updateFreq	= UPDATE_FREQ;
	private long					totalFrames	= 0;
	
	
	/**
	  * 
	  */
	public FpsCounter()
	{
		// nothing to do
	}
	
	
	/**
	 * @param updateFreq
	 */
	public FpsCounter(int updateFreq)
	{
		this.updateFreq = updateFreq;
	}
	
	
	/**
	 * Signal for new frame. Call this each time, a new frame comes in
	 * 
	 */
	public void newFrame()
	{
		long curTime = System.nanoTime();
		if (counter >= updateFreq)
		{
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
