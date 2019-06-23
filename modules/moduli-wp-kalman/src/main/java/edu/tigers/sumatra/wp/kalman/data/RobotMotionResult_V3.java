package edu.tigers.sumatra.wp.kalman.data;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Vector2f;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.TrackedBot;
import edu.tigers.sumatra.wp.kalman.WPConfig;


/**
 */
public class RobotMotionResult_V3 extends ABotMotionResult
{
	private final double	vx, vy, vw;
	private final double	ax, ay, aw;
	
	
	/**
	 * @param x
	 * @param y
	 * @param w
	 * @param vx
	 * @param vy
	 * @param vw
	 * @param ax
	 * @param ay
	 * @param aw
	 * @param confidence
	 * @param onCam
	 */
	public RobotMotionResult_V3(final double x, final double y, final double w, final double vx, final double vy,
			final double vw, final double ax, final double ay,
			final double aw, final double confidence, final boolean onCam)
	{
		super(x, y, w, confidence, onCam);
		this.vx = vx;
		this.vy = vy;
		this.vw = vw;
		this.ax = ax;
		this.ay = ay;
		this.aw = aw;
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
		
		final double xVel = vx / WPConfig.FILTER_CONVERT_MperS_TO_INTERNAL_V;
		final double yVel = vy / WPConfig.FILTER_CONVERT_MperS_TO_INTERNAL_V;
		final IVector2 vel = new Vector2f(xVel, yVel);
		
		final double xAcc = ax / WPConfig.FILTER_CONVERT_MperS_TO_INTERNAL_V;
		final double yAcc = ay / WPConfig.FILTER_CONVERT_MperS_TO_INTERNAL_V;
		final IVector2 acc = new Vector2f(xAcc, yAcc);
		
		final double angle = orientation;
		final double aVel = vw / WPConfig.FILTER_CONVERT_RadPerS_TO_RadPerInternal;
		final double aAcc = aw / WPConfig.FILTER_CONVERT_RadPerS_TO_RadPerInternal;
		
		TrackedBot bot = new TrackedBot(timestamp, botId);
		bot.setPos(pos);
		bot.setVel(vel);
		bot.setAcc(acc);
		bot.setAngle(angle);
		bot.setaVel(aVel);
		bot.setaAcc(aAcc);
		return bot;
	}
	
	
	/**
	 * @return the vx
	 */
	public double getVx()
	{
		return vx;
	}
	
	
	/**
	 * @return the vy
	 */
	public double getVy()
	{
		return vy;
	}
	
	
	/**
	 * @return the vw
	 */
	public double getVw()
	{
		return vw;
	}
	
	
	/**
	 * @return the ax
	 */
	public double getAx()
	{
		return ax;
	}
	
	
	/**
	 * @return the ay
	 */
	public double getAy()
	{
		return ay;
	}
	
	
	/**
	 * @return the aw
	 */
	public double getAw()
	{
		return aw;
	}
}
