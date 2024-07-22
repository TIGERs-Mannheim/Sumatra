/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.obstacles;

import lombok.Setter;
import lombok.experimental.Accessors;


/**
 * Base class for obstacles that are moving.
 */
public abstract class AMovingObstacle extends AObstacle
{
	@Setter
	@Accessors(chain = true)
	private double maxSpeed;

	@Setter
	@Accessors(chain = true)
	private boolean hasPriority = false;


	@Override
	public boolean isMotionLess()
	{
		return false;
	}


	@Override
	public double getMaxSpeed()
	{
		return maxSpeed;
	}


	@Override
	public boolean hasPriority()
	{
		return hasPriority;
	}
}
