/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.obstacles;

import java.util.List;

import edu.tigers.sumatra.drawable.DrawableTube;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.tube.Tube;
import edu.tigers.sumatra.math.vector.IVector2;
import lombok.RequiredArgsConstructor;


/**
 * Obstacle with a constant velocity and a limited time horizon.
 */
@RequiredArgsConstructor
public class ConstVelocityObstacle extends AObstacle
{
	private final IVector2 start;
	private final IVector2 vel;
	private final double radius;
	private final double tMax;


	@Override
	protected void initializeShapes(final List<IDrawableShape> shapes)
	{
		IVector2 end = start.addNew(vel.multiplyNew(tMax * 1000));
		shapes.add(new DrawableTube(Tube.create(start, end, radius)));
	}


	@Override
	public boolean isPointCollidingWithObstacle(final IVector2 point, final double t, final double margin)
	{
		double tt = Math.min(tMax, t);
		IVector2 p = vel.multiplyNew(tt * 1000).add(start);
		return p.distanceToSqr(point) < SumatraMath.square(radius + margin);
	}
}
