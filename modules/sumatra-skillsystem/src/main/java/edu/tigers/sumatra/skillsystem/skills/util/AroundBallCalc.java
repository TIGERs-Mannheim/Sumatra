/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills.util;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import org.apache.commons.lang.Validate;

import static java.lang.Math.abs;
import static java.lang.Math.min;
import static java.lang.Math.signum;


/**
 * Calculate the destination to get around the ball
 */
public class AroundBallCalc
{
	@Configurable(defValue = "0.5")
	private static double shiftAngle = 0.5;
	
	static
	{
		ConfigRegistration.registerClass("skills", AroundBallCalc.class);
	}
	
	private final IVector2 ballPos;
	private final ITrackedBot tBot;
	private final double maxMargin;
	private final double minMargin;
	private final IVector2 destination;
	
	
	private AroundBallCalc(final Builder builder)
	{
		ballPos = builder.ballPos;
		tBot = builder.tBot;
		maxMargin = builder.maxMargin;
		minMargin = builder.minMargin;
		destination = builder.destination;
	}
	
	
	/**
	 * @return a new factory
	 */
	public static Builder aroundBall()
	{
		return new Builder();
	}
	
	
	/**
	 * @return a destination to safely move around the ball
	 */
	public IVector2 getAroundBallDest()
	{
		// if we are far away from ball, choose dest a bit more clever
		// else, we would drive directly towards the ball and would need
		// to change direction when near enough
		if (getBallPos().distanceTo(getTBot().getPos()) > 300)
		{
			ILine bot2Dest = Line.fromPoints(getPos(), destination);
			IVector2 lp = bot2Dest.leadPointOf(getBallPos());
			if (bot2Dest.isPointOnLineSegment(lp))
			{
				IVector2 ball2Lp = lp.subtractNew(getBallPos());
				double relMargin = getRelativeMarginToBall();
				double distance = getRequiredDistanceToBall(relMargin);
				return getBallPos().addNew(ball2Lp.scaleToNew(distance));
			}
		}
		
		double relMargin = getRelativeMarginToBall();
		double distance = getRequiredDistanceToBall(relMargin);
		IVector2 ball2Dest = destination.subtractNew(getBallPos());
		IVector2 ball2Bot = getPos().subtractNew(getBallPos());
		double remainingDestRotation = ball2Dest.angleTo(ball2Bot).orElse(0.0);
		double relShiftAngle = -signum(remainingDestRotation)
				* min(abs(remainingDestRotation), AroundBallCalc.shiftAngle);
		return getBallPos().addNew(ball2Bot.turnNew(relShiftAngle).scaleToNew(distance));
	}
	
	
	/**
	 * @return relative margin [0..1]
	 */
	private double getRelativeMarginToBall()
	{
		double margin = maxMargin * (getAngleDiff() / AngleMath.PI);
		if (margin < 10)
		{
			margin = 0;
		}
		return margin;
	}
	
	
	private double getAngleDiff()
	{
		IVector2 ball2Bot = getPos().subtractNew(getBallPos());
		IVector2 ball2Dest = destination.subtractNew(getBallPos());
		return ball2Bot.angleToAbs(ball2Dest).orElse(0.0);
	}
	
	
	private double getRequiredDistanceToBall(double margin)
	{
		return getTBot().getCenter2DribblerDist() + Geometry.getBallRadius() + minMargin + margin;
	}
	
	
	private IVector2 getBallPos()
	{
		return ballPos;
	}
	
	
	private ITrackedBot getTBot()
	{
		return tBot;
	}
	
	
	private IVector2 getPos()
	{
		return tBot.getPos();
	}
	
	
	/**
	 * {@code AroundBallCalc} factory static inner class.
	 */
	public static final class Builder
	{
		private IVector2 ballPos;
		private ITrackedBot tBot;
		private Double maxMargin;
		private Double minMargin;
		private IVector2 destination;
		
		
		private Builder()
		{
		}
		
		
		/**
		 * Sets the {@code ballPos} and returns a reference to this Builder so that the methods can be chained together.
		 *
		 * @param ballPos the {@code ballPos} to set
		 * @return a reference to this Builder
		 */
		public Builder withBallPos(final IVector2 ballPos)
		{
			this.ballPos = ballPos;
			return this;
		}
		
		
		/**
		 * Sets the {@code tBot} and returns a reference to this Builder so that the methods can be chained together.
		 *
		 * @param tBot the {@code tBot} to set
		 * @return a reference to this Builder
		 */
		public Builder withTBot(final ITrackedBot tBot)
		{
			this.tBot = tBot;
			return this;
		}
		
		
		/**
		 * Sets the {@code maxMargin} and returns a reference to this Builder so that the methods can be chained together.
		 *
		 * @param maxMargin the {@code maxMargin} to set
		 * @return a reference to this Builder
		 */
		public Builder withMaxMargin(final double maxMargin)
		{
			this.maxMargin = maxMargin;
			return this;
		}
		
		
		/**
		 * Sets the {@code minMargin} and returns a reference to this Builder so that the methods can be chained together.
		 *
		 * @param minMargin the {@code minMargin} to set
		 * @return a reference to this Builder
		 */
		public Builder withMinMargin(final double minMargin)
		{
			this.minMargin = minMargin;
			return this;
		}
		
		
		/**
		 * Sets the {@code destination} and returns a reference to this Builder so that the methods can be chained
		 * together.
		 *
		 * @param destination the {@code destination} to set
		 * @return a reference to this Builder
		 */
		public Builder withDestination(final IVector2 destination)
		{
			this.destination = destination;
			return this;
		}
		
		
		/**
		 * Returns a {@code AroundBallCalc} built from the parameters previously set.
		 *
		 * @return a {@code AroundBallCalc} built with parameters of this {@code AroundBallCalc.Builder}
		 */
		public AroundBallCalc build()
		{
			Validate.notNull(ballPos);
			Validate.notNull(tBot);
			Validate.notNull(maxMargin);
			Validate.notNull(minMargin);
			Validate.notNull(destination);
			return new AroundBallCalc(this);
		}
	}
}
