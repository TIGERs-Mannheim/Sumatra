package edu.dhbw.mannheim.tigers.sumatra.util;


/**
 * Very Simple FPS Counter
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class FpsCounter
{
	private static final int	UPDATE_FREQ			= 30;
	private static final float	MILIS_TO_SECONDS	= 1000f;
	private long					lastTime				= 0;
	private float					fps					= 0;
	private int						counter				= 0;
	
	
	/**
	 * Signal for new frame. Call this each time, a new frame comes in
	 * 
	 */
	public void newFrame()
	{
		long curTime = System.currentTimeMillis();
		if (counter >= UPDATE_FREQ)
		{
			fps = UPDATE_FREQ / ((curTime - lastTime) / MILIS_TO_SECONDS);
			lastTime = curTime;
			counter = 0;
		}
		counter++;
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
}
