/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.obstacles;

import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.pathfinder.obstacles.input.CollisionInput;


/**
 * Base class for obstacles that can not move.
 */
public abstract class AMotionlessObstacle extends AObstacle
{
	@Override
	public boolean isMotionLess()
	{
		return true;
	}


	@Override
	public double getMaxSpeed()
	{
		return 0;
	}


	@Override
	public boolean hasPriority()
	{
		return true;
	}


	@Override
	public boolean collisionLikely(double t, IVector2 pos)
	{
		return true;
	}


	@Override
	public IVector2 velocity(IVector2 pos, double t)
	{
		return Vector2.zero();
	}


	@Override
	public boolean canCollide(CollisionInput input)
	{
		return true;
	}
}
