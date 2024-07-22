/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.obstacles;

import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.pathfinder.obstacles.input.CollisionInput;
import lombok.RequiredArgsConstructor;

import java.awt.Color;
import java.util.List;
import java.util.Optional;


@RequiredArgsConstructor
public class LimitedTimeObstacle implements IObstacle
{
	private final IObstacle obstacle;
	private final double tMax;


	@Override
	public double distanceTo(CollisionInput input)
	{
		return obstacle.distanceTo(input);
	}


	@Override
	public boolean canCollide(CollisionInput input)
	{
		if (input.getTimeOffset() > tMax)
		{
			return false;
		}
		return obstacle.canCollide(input);
	}


	@Override
	public boolean isMotionLess()
	{
		return obstacle.isMotionLess();
	}


	@Override
	public List<IDrawableShape> getShapes()
	{
		return obstacle.getShapes();
	}


	@Override
	public double getMaxSpeed()
	{
		return obstacle.getMaxSpeed();
	}


	@Override
	public Optional<IVector2> adaptDestinationForRobotPos(IVector2 robotPos)
	{
		return obstacle.adaptDestinationForRobotPos(robotPos);
	}


	@Override
	public Optional<IVector2> adaptDestination(IVector2 destination)
	{
		return obstacle.adaptDestination(destination);
	}


	@Override
	public boolean collisionLikely(double t, IVector2 pos)
	{
		return obstacle.collisionLikely(t, pos);
	}


	@Override
	public void setColor(Color color)
	{
		obstacle.setColor(color);
	}


	@Override
	public String getIdentifier()
	{
		return this.getClass().getSimpleName() + ":" + obstacle.getIdentifier();
	}


	@Override
	public boolean hasPriority()
	{
		return obstacle.hasPriority();
	}


	@Override
	public IVector2 velocity(IVector2 pos, double t)
	{
		if (t > tMax)
		{
			return Vector2.zero();
		}
		return obstacle.velocity(pos, t);
	}


	@Override
	public boolean isCollidingAt(IVector2 pos)
	{
		return obstacle.isCollidingAt(pos);
	}


	@Override
	public int orderId()
	{
		return obstacle.orderId();
	}


	@Override
	public boolean useDynamicMargin()
	{
		return obstacle.useDynamicMargin();
	}
}
