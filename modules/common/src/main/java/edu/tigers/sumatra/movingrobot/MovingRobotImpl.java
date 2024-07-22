/*
 * Copyright (c) 2009 - 2024, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.movingrobot;

import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.tube.ITube;
import edu.tigers.sumatra.math.tube.Tube;
import edu.tigers.sumatra.math.vector.IVector2;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class MovingRobotImpl implements IMovingRobot
{
	private final IVector2 pos;
	private final IVector2 dir;
	private final double radius;
	private final double speed;
	private final MovingOffsetFunction offsetFunction;


	@Override
	public ICircle getMovingHorizon(final double tHorizon, double tAdditionalReaction)
	{
		var p = offsetFunction.forwardBackwardOffset(tHorizon, tAdditionalReaction);

		double dynamicRadius = Math.abs(p.forward() - p.backward()) / 2;
		IVector2 center = pos.addNew(dir.multiplyNew(p.backward() + dynamicRadius));
		return Circle.createCircle(center, dynamicRadius + radius);
	}


	@Override
	public ITube getMovingHorizonTube(final double tHorizon, double tAdditionalReaction)
	{
		var p = offsetFunction.forwardBackwardOffset(tHorizon, tAdditionalReaction);

		return Tube.create(
				dir.multiplyNew(p.backward()).add(pos),
				dir.multiplyNew(p.forward()).add(pos),
				radius
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


}
