/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.obstacles;

import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.math.I2DShape;
import edu.tigers.sumatra.math.vector.IVector2;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.awt.Color;
import java.util.List;
import java.util.Optional;


/**
 * Base class for obstacles.
 */
public abstract class AObstacle implements IObstacle
{
	@Setter
	private Color color;

	@Setter
	@Accessors(chain = true)
	private int orderId = 50;

	@Setter
	@Accessors(chain = true)
	private boolean useDynamicMargin = true;

	private List<IDrawableShape> shapes;


	protected List<IDrawableShape> initializeShapes()
	{
		return List.of();
	}


	@Override
	public final List<IDrawableShape> getShapes()
	{
		if (shapes == null)
		{
			shapes = initializeShapes();
			if (color != null)
			{
				shapes.forEach(s -> s.setColor(color));
			}
		}
		return shapes;
	}


	@Override
	public int orderId()
	{
		return orderId;
	}


	@Override
	public boolean useDynamicMargin()
	{
		return useDynamicMargin;
	}


	protected final Optional<IVector2> adaptDestination(I2DShape shape, IVector2 destination)
	{
		if (shape.isPointInShape(destination))
		{
			return Optional.of(shape.withMargin(1).nearestPointOutside(destination));
		}
		return Optional.empty();
	}


	protected final Optional<IVector2> adaptDestinationForRobotPos(I2DShape shape, IVector2 robotPos)
	{
		if (shape.isPointInShape(robotPos))
		{
			return Optional.of(shape.withMargin(100).nearestPointOutside(robotPos));
		}
		return Optional.empty();
	}
}
