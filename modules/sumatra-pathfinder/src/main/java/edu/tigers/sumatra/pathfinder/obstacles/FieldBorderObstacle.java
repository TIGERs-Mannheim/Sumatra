/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.obstacles;

import edu.tigers.sumatra.drawable.DrawableRectangle;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.math.rectangle.IRectangle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.pathfinder.obstacles.input.CollisionInput;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;


/**
 * An obstacle based on the field border (inverted rectangle obstacle)
 */
@RequiredArgsConstructor
public class FieldBorderObstacle extends AObstacle
{
	private final IRectangle field;


	@Override
	protected List<IDrawableShape> initializeShapes()
	{
		return List.of(
				new DrawableRectangle(field)
		);
	}


	@Override
	public double distanceTo(CollisionInput input)
	{
		IVector2 robotPos = input.getRobotPos();
		return distanceTo(robotPos);
	}


	private double distanceTo(IVector2 robotPos)
	{
		return field.nearestPointOutside(robotPos).distanceTo(robotPos);
	}


	@Override
	public Optional<IVector2> adaptDestination(IVector2 robotPos, IVector2 destination)
	{
		if (!field.withMargin(100).isPointInShape(robotPos))
		{
			return Optional.of(field.withMargin(-100).nearestPointInside(robotPos));
		}
		if (!field.isPointInShape(destination))
		{
			return Optional.of(field.withMargin(-1).nearestPointInside(destination));
		}
		return Optional.empty();
	}
}
