/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.obstacles;

import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.pathfinder.obstacles.input.CollisionInput;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;


/**
 * A simple circle-based obstacle
 */
@RequiredArgsConstructor
public class GenericCircleObstacle extends AMotionlessObstacle
{
	private final String qualifier;
	private final ICircle circle;


	@Override
	public String getIdentifier()
	{
		return super.getIdentifier() + " " + qualifier;
	}


	@Override
	public double distanceTo(CollisionInput input)
	{
		return circle.center().distanceTo(input.getRobotPos()) - circle.radius();
	}


	@Override
	protected List<IDrawableShape> initializeShapes()
	{
		return List.of(
				new DrawableCircle(circle)
		);
	}


	@Override
	public Optional<IVector2> adaptDestinationForRobotPos(IVector2 robotPos)
	{
		return adaptDestinationForRobotPos(circle, robotPos);
	}


	@Override
	public Optional<IVector2> adaptDestination(IVector2 destination)
	{
		return adaptDestination(circle, destination);
	}


	@Override
	public boolean isCollidingAt(IVector2 pos)
	{
		return circle.isPointInShape(pos);
	}
}
