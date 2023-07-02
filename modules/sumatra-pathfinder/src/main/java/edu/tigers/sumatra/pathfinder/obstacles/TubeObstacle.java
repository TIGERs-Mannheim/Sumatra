/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.obstacles;

import edu.tigers.sumatra.drawable.DrawableTube;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.math.tube.Tube;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.pathfinder.obstacles.input.CollisionInput;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;


/**
 * An obstacle in a tube shape
 */
@RequiredArgsConstructor
public class TubeObstacle extends AObstacle
{
	private final String qualifier;
	private final Tube tube;


	@Override
	public String getIdentifier()
	{
		return super.getIdentifier() + " " + qualifier;
	}


	@Override
	public double distanceTo(CollisionInput input)
	{
		return tube.nearestPointInside(input.getRobotPos()).distanceTo(input.getRobotPos());
	}


	@Override
	protected List<IDrawableShape> initializeShapes()
	{
		return List.of(
				new DrawableTube(tube)
		);
	}


	@Override
	public Optional<IVector2> adaptDestination(IVector2 robotPos, IVector2 destination)
	{
		return adaptDestination(tube, robotPos, destination);
	}
}
