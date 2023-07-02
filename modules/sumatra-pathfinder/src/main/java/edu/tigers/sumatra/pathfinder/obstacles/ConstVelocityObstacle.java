/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.obstacles;

import edu.tigers.sumatra.drawable.DrawableTube;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.math.tube.ITube;
import edu.tigers.sumatra.math.tube.Tube;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.pathfinder.obstacles.input.CollisionInput;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;


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
	private final double minVelocity;


	@Override
	protected void configure()
	{
		setMotionLess(false);
	}


	@Override
	protected List<IDrawableShape> initializeShapes()
	{
		return List.of(
				new DrawableTube(tube())
		);
	}


	@Override
	public boolean canCollide(CollisionInput input)
	{
		return input.getRobotVel().getLength2() > minVelocity || input.accelerating();
	}


	@Override
	public double distanceTo(CollisionInput input)
	{
		double tt = Math.min(tMax, input.getTimeOffset());
		IVector2 predictedPos = vel.multiplyNew(tt * 1000).add(start);
		double currentDistance = predictedPos.distanceTo(input.getRobotPos());
		return currentDistance - radius;
	}


	private ITube tube()
	{
		IVector2 end = start.addNew(vel.multiplyNew(tMax * 1000));
		return Tube.create(start, end, radius);
	}
}
