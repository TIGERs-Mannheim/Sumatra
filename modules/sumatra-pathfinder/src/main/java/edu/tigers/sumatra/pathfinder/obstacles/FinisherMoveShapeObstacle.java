/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.obstacles;

import edu.tigers.sumatra.drawable.DrawableFinisherMoveShape;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.math.penarea.FinisherMoveShape;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.pathfinder.obstacles.input.CollisionInput;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Optional;


@AllArgsConstructor
public class FinisherMoveShapeObstacle extends AMotionlessObstacle
{
	@Getter
	private final FinisherMoveShape shape;


	@Override
	protected List<IDrawableShape> initializeShapes()
	{
		return new DrawableFinisherMoveShape(shape).getShapes();
	}


	@Override
	public double distanceTo(CollisionInput input)
	{
		if (shape.isPointInShape(input.getRobotPos()))
		{
			return 0;
		}
		return shape.stepOnShape(shape.getStepPositionOnShape(input.getRobotPos())).distanceTo(input.getRobotPos());
	}


	@Override
	public Optional<IVector2> adaptDestinationForRobotPos(IVector2 robotPos)
	{
		return Optional.empty();
	}


	@Override
	public Optional<IVector2> adaptDestination(IVector2 destination)
	{
		return Optional.empty();
	}


	@Override
	public boolean isCollidingAt(IVector2 pos)
	{
		return shape.isPointInShape(pos);
	}
}
