/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.obstacles;

import edu.tigers.sumatra.drawable.DrawableShapeBoundary;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.math.penaltyarea.IPenaltyArea;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.pathfinder.obstacles.input.CollisionInput;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;


/**
 * A penalty area obstacle with efficient data storage and caching for painting
 */
@RequiredArgsConstructor
public class PenaltyAreaObstacle extends AObstacle
{
	private final IPenaltyArea penaltyArea;


	@Override
	public String getIdentifier()
	{
		return super.getIdentifier() + " " + (penaltyArea.getGoalCenter().x() > 0 ? "THEIR" : "OUR");
	}


	@Override
	protected List<IDrawableShape> initializeShapes()
	{
		return List.of(new DrawableShapeBoundary(penaltyArea));
	}


	@Override
	public double distanceTo(CollisionInput input)
	{
		return distanceTo(input.getRobotPos());
	}


	private double distanceTo(IVector2 pos)
	{
		if (penaltyArea.isBehindPenaltyArea(pos))
		{
			return 0;
		}
		return penaltyArea.distanceTo(pos);
	}


	@Override
	public Optional<IVector2> adaptDestination(IVector2 robotPos, IVector2 destination)
	{
		if (distanceTo(robotPos) <= 0)
		{
			return Optional.of(penaltyArea.withMargin(100).nearestPointOutside(robotPos));
		}
		if (distanceTo(destination) <= 0)
		{
			return Optional.of(penaltyArea.withMargin(1).nearestPointOutside(destination));
		}
		return Optional.empty();
	}
}
