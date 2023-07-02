/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.obstacles;

import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.math.I2DShape;
import edu.tigers.sumatra.math.vector.IVector2;
import lombok.Getter;
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
	@Getter
	@Setter
	@Accessors(chain = true)
	private boolean motionLess = true;

	@Getter
	@Setter
	@Accessors(chain = true)
	private double maxSpeed;

	@Setter
	private Color color;

	private List<IDrawableShape> shapes;


	protected List<IDrawableShape> initializeShapes()
	{
		return List.of();
	}


	protected AObstacle()
	{
		configure();
	}


	protected void configure()
	{
		// can be overwritten to configure the numerous attributes above
	}


	@Override
	public final List<IDrawableShape> getShapes()
	{
		if (shapes == null)
		{
			shapes = initializeShapes();
			shapes.forEach(s -> s.setColor(color));
		}
		return shapes;
	}


	protected final Optional<IVector2> adaptDestination(I2DShape shape, IVector2 robotPos, IVector2 destination)
	{
		if (shape.withMargin(-10).isPointInShape(robotPos))
		{
			return Optional.of(shape.withMargin(10).nearestPointOutside(robotPos));
		}
		if (shape.isPointInShape(destination))
		{
			return Optional.of(shape.withMargin(1).nearestPointOutside(destination));
		}
		return Optional.empty();
	}
}
