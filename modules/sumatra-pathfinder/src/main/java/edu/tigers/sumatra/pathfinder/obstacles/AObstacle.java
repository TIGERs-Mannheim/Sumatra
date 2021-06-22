/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.obstacles;

import java.util.ArrayList;
import java.util.List;

import edu.tigers.sumatra.drawable.IDrawableShape;


/**
 * Base class for obstacles.
 */
public abstract class AObstacle implements IObstacle
{
	private boolean emergencyBrakeFor = false;
	private boolean activelyEvade = false;
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


	@Override
	public final boolean isEmergencyBrakeFor()
	{
		return emergencyBrakeFor;
	}


	public void setEmergencyBrakeFor(final boolean emergencyBrakeFor)
	{
		this.emergencyBrakeFor = emergencyBrakeFor;
	}


	@Override
	public boolean isActivelyEvade()
	{
		return activelyEvade;
	}


	public void setActivelyEvade(final boolean activelyEvade)
	{
		this.activelyEvade = activelyEvade;
	}
}
