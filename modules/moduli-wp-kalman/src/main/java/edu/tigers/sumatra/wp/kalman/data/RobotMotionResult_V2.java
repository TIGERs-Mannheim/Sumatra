package edu.tigers.sumatra.wp.kalman.data;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Vector2f;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.TrackedBot;
import edu.tigers.sumatra.wp.kalman.WPConfig;


/**
 */
public class RobotMotionResult_V2 extends ABotMotionResult
{
	/** */
	public final double	movementAngle;
	/** */
	public final double	v;
	/** */
	public final double	trackSpeed;
	/** */
	public final double	angularVelocity;
	
	
	/**
	 * @param x
	 * @param y
	 * @param orientation
	 * @param movementAngle
	 * @param v
	 * @param trackSpeed
	 * @param angularVelocity
	 * @param confidence
	 * @param onCam
	 */
	public RobotMotionResult_V2(final double x, final double y, final double orientation, final double movementAngle,
			final double v,
			final double trackSpeed, final double angularVelocity, final double confidence, final boolean onCam)
	{
		super(x, y, orientation, confidence, onCam);
		this.movementAngle = movementAngle;
		this.v = v;
		this.trackSpeed = trackSpeed;
		this.angularVelocity = angularVelocity;
	}
	
	
	/**
	 * @param botId
	 * @return
	 */
	@Override
	public ITrackedBot motionToTrackedBot(final long timestamp, final BotID botId)
	{
		IVector2 pos = new Vector2f(x / WPConfig.FILTER_CONVERT_MM_TO_INTERNAL_UNIT,
				y / WPConfig.FILTER_CONVERT_MM_TO_INTERNAL_UNIT);
		final double v = this.v / WPConfig.FILTER_CONVERT_MperS_TO_INTERNAL_V;
		
		final double xVel = v * Math.cos(movementAngle);
		final double yVel = v * Math.sin(movementAngle);
		final IVector2 vel = new Vector2f(xVel, yVel);
		
		final double angle = orientation;
		final double aVel = (angularVelocity + trackSpeed)
				/ WPConfig.FILTER_CONVERT_RadPerS_TO_RadPerInternal;
		
		TrackedBot bot = new TrackedBot(timestamp, botId);
		bot.setPos(pos);
		bot.setVel(vel);
		
		bot.setAngle(angle);
		bot.setaVel(aVel);
		return bot;
	}
}
