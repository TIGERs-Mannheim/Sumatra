/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.obstacles;

import edu.tigers.sumatra.drawable.IDrawableShape;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;


/**
 * Base class for obstacles.
 */
public abstract class AObstacle implements IObstacle
{
	@Getter
	@Setter(AccessLevel.PROTECTED)
	private boolean emergencyBrakeFor = false;
	@Getter
	@Setter(AccessLevel.PROTECTED)
	private boolean brakeInside = false;
	@Getter
	@Setter(AccessLevel.PROTECTED)
	private boolean activelyEvade = false;
	@Getter
	@Setter(AccessLevel.PROTECTED)
	private int priority;
	private List<IDrawableShape> shapes;


	protected abstract void initializeShapes(final List<IDrawableShape> shapes);


	@Override
	public final List<IDrawableShape> getShapes()
	{
		if (shapes == null)
		{
			shapes = new ArrayList<>();
			initializeShapes(shapes);
		}
		return shapes;
	}
}
