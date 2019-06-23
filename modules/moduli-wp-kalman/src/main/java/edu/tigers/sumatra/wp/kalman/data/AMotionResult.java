package edu.tigers.sumatra.wp.kalman.data;

/**
 */
public abstract class AMotionResult
{
	/** */
	public final double		x;
	/** */
	public final double		y;
									
	private final double		confidence;
	private final boolean	onCam;
									
									
	AMotionResult(final double x, final double y, final double confidence, final boolean onCam)
	{
		this.x = x;
		this.y = y;
		this.confidence = confidence;
		this.onCam = onCam;
	}
	
	
	/**
	 * @return the x
	 */
	public double getX()
	{
		return x;
	}
	
	
	/**
	 * @return the y
	 */
	public double getY()
	{
		return y;
	}
	
	
	/**
	 * @return the confidence
	 */
	public double getConfidence()
	{
		return confidence;
	}
	
	
	/**
	 * @return the onCam
	 */
	public boolean isOnCam()
	{
		return onCam;
	}
	
	
}
