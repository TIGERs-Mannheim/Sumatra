/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.obstacles;

import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.math.vector.IVector2;
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
	public Optional<IVector2> adaptDestination(IVector2 robotPos, IVector2 destination)
	{
		return obstacle.adaptDestination(robotPos, destination);
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
}
