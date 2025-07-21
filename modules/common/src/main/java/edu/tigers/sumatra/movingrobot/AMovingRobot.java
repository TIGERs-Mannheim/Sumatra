/*
 * Copyright (c) 2009 - 2024, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.movingrobot;

import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.tube.ITube;
import edu.tigers.sumatra.math.tube.Tube;
import edu.tigers.sumatra.math.vector.IVector2;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public abstract class AMovingRobot implements IMovingRobot
{
	private final IVector2 pos;
	private final IVector2 dir;
	private final double radius;
	protected final double speed;
	private final double reactionDuration;


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

}
