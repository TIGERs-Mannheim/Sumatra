package edu.tigers.sumatra.wp.kalman.data;

import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.IVector3;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.math.Vector3;
import edu.tigers.sumatra.wp.data.TrackedBall;
import edu.tigers.sumatra.wp.kalman.WPConfig;


/**
 */
public class BallMotionResult extends AMotionResult
{
	/** */
	public final double	z;
	/** */
	public final double	vx;
	/** */
	public final double	vy;
	/** */
	public final double	vz;
	/** */
	public final double	ax;
	/** */
	public final double	ay;
	/** */
	public final double	az;
								
								
	/**
	 * @param x
	 * @param y
	 * @param z
	 * @param vx
	 * @param vy
	 * @param vz
	 * @param ax
	 * @param ay
	 * @param az
	 * @param confidence
	 * @param onCam
	 */
	public BallMotionResult(final double x, final double y, final double z, final double vx, final double vy,
			final double vz, final double ax, final double ay,
			final double az, final double confidence, final boolean onCam)
	{
		super(x, y, confidence, onCam);
		this.z = z;
		
		this.vx = vx;
		this.vy = vy;
		this.vz = vz;
		this.ax = ax;
		this.ay = ay;
		this.az = az;
	}
	
	
	/**
	 * @return
	 */
	public TrackedBall toTrackedBall()
	{
		final double xPos = x / WPConfig.FILTER_CONVERT_MM_TO_INTERNAL_UNIT;
		final double xVel = vx / WPConfig.FILTER_CONVERT_MperS_TO_INTERNAL_V;
		final double yPos = y / WPConfig.FILTER_CONVERT_MM_TO_INTERNAL_UNIT;
		final double yVel = vy / WPConfig.FILTER_CONVERT_MperS_TO_INTERNAL_V;
		final double height = z / WPConfig.FILTER_CONVERT_MM_TO_INTERNAL_UNIT;
		final double zVel = vz / WPConfig.FILTER_CONVERT_MperS_TO_INTERNAL_V;
		
		final double xAcc = ax / WPConfig.FILTER_CONVERT_MperSS_TO_INTERNAL_A;
		final double yAcc = ay / WPConfig.FILTER_CONVERT_MperSS_TO_INTERNAL_A;
		final double zAcc = az / WPConfig.FILTER_CONVERT_MperSS_TO_INTERNAL_A;
		
		IVector2 pos = new Vector2(xPos, yPos);
		IVector2 vel = new Vector2(xVel, yVel);
		IVector3 acc = new Vector3(xAcc, yAcc, zAcc);
		TrackedBall ball = new TrackedBall(pos, height, vel, zVel, acc);
		ball.setConfidence(getConfidence());
		ball.setOnCam(isOnCam());
		return ball;
	}
}
