/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.obstacles;

import edu.tigers.sumatra.drawable.DrawableTube;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.circle.Circle;
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
public class ConstVelocityObstacle extends AMovingObstacle
{
	private final BotID botID;
	private final IVector2 start;
	private final IVector2 vel;
	private final double radius;
	private final double tMax;
	private final double minVelocity;


	@Override
	public String getIdentifier()
	{
		return super.getIdentifier() + " " + botID.getSaveableString();
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
	public Optional<IVector2> adaptDestinationForRobotPos(IVector2 robotPos)
	{
		return adaptDestinationForRobotPos(Circle.createCircle(start, radius), robotPos);
	}


	@Override
	public Optional<IVector2> adaptDestination(IVector2 destination)
	{
		return adaptDestination(Circle.createCircle(start, radius), destination);
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


	@Override
	public boolean collisionLikely(double t, IVector2 pos)
	{
		// certain, if robot is not moving
		return vel.getLength2() < 0.3;
	}


	@Override
	public IVector2 velocity(IVector2 pos, double t)
	{
		return vel;
	}


	@Override
	public boolean isCollidingAt(IVector2 pos)
	{
		return Circle.createCircle(start, radius).isPointInShape(pos);
	}
}
