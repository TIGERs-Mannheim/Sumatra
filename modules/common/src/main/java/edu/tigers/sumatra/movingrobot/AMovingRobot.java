package edu.tigers.sumatra.movingrobot;

import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.tube.ITube;
import edu.tigers.sumatra.math.tube.Tube;
import edu.tigers.sumatra.math.vector.IVector2;


public abstract class AMovingRobot implements IMovingRobot
{
	protected final IVector2 pos;
	protected final IVector2 dir;
	protected final double radius;
	protected final double speed;
	private final double reactionDuration;

	private final double vLimit;
	protected final double aLimit;
	protected final double brkLimit;


	protected AMovingRobot(MovingRobotParams params)
	{
		this.pos = params.position();
		this.dir = params.velocity().normalizeNew();
		this.radius = params.radius();
		this.speed = params.velocity().getLength2();
		this.reactionDuration = params.reactionTime();
		this.vLimit = params.vLimit();
		this.aLimit = params.aLimit();
		this.brkLimit = params.brkLimit();
	}


	@Override
	public ICircle getMovingHorizon(final double tHorizon, double tAdditionalReaction)
	{
		var p = forwardBackwardOffsetWithReaction(tHorizon, tAdditionalReaction);

		double dynamicRadius = Math.abs(p.forward() - p.backward()) / 2;
		IVector2 center = pos.addNew(dir.multiplyNew(p.backward() + dynamicRadius));
		return Circle.createCircle(center, dynamicRadius + radius);
	}


	@Override
	public ITube getMovingHorizonTube(final double tHorizon, double tAdditionalReaction)
	{
		var p = forwardBackwardOffsetWithReaction(tHorizon, tAdditionalReaction);

		return Tube.create(
				dir.multiplyNew(p.backward()).add(pos),
				dir.multiplyNew(p.forward()).add(pos),
				radius
		);
	}


	private MovingOffsets forwardBackwardOffsetWithReaction(double tHorizon, double additionalReactionDuration)
	{
		double tReaction = Math.min(reactionDuration + additionalReactionDuration, tHorizon);
		double t = Math.max(0, tHorizon - tReaction);
		double distReaction = speed * tReaction * 1000;

		var offset = forwardBackwardOffset(t);
		return new MovingOffsets(
				distReaction + offset.forward(),
				distReaction + offset.backward()
		);
	}


	@Override
	public IVector2 getPos()
	{
		return pos;
	}


	@Override
	public double getSpeed()
	{
		return speed;
	}


	/**
	 * @param t time horizon in seconds
	 * @return moving offsets
	 */
	abstract MovingOffsets forwardBackwardOffset(double t);


	/**
	 * Calculate maximum possible extraDistance that can be covered with a robot with a current velocity
	 * {@param vStartEnd} and a remaining timespan {@param tRemaining},while ensuring that the bot has {@param vStartEnd}
	 * exit speed after the timespan
	 *
	 * @param vStartEnd  velocity the Bot has before and after {@param tRemaining}
	 * @param tRemaining
	 * @return
	 */
	protected double maximizeDistance(
			double vStartEnd,
			double tRemaining // time tRemaining without brake time (time from vStartEnd to 0)
	)
	{
		double vMinRemaining = Math.min(vStartEnd, vLimit); // During remaining time we will never go slower
		double vMaxRemaining = vLimit; // We are trying to reach this velocity
		double velHeadroom = vMaxRemaining - vMinRemaining;

		double tAccToMaxTheoreticalVel = velHeadroom / aLimit;
		double tBrkFromMaxTheoreticalVel = velHeadroom / brkLimit;
		double tToVelMaxAndBack = tAccToMaxTheoreticalVel + tBrkFromMaxTheoreticalVel;

		double distVMin = vMinRemaining * tRemaining;
		double distVHeadroom;
		if (tToVelMaxAndBack <= tRemaining)
		{
			// Enough time to use full headroom -> Trapezoidal form
			distVHeadroom = (velHeadroom * (tRemaining - 0.5 * tToVelMaxAndBack));

		} else
		{
			// Not enough time to use full vel headroom -> Triangle form
			var velHeadroomReached = (aLimit * brkLimit * tRemaining) / (aLimit + brkLimit);
			distVHeadroom = (velHeadroomReached * 0.5 * tRemaining);
		}
		return distVMin + distVHeadroom;
	}
}
