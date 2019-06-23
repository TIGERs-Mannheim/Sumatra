/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.traj;

import java.util.Optional;

import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.pathfinder.IPathFinderResult;
import edu.tigers.sumatra.pathfinder.obstacles.IObstacle;
import edu.tigers.sumatra.trajectory.ITrajectory;


/**
 * Representation of a complex path collision
 * 
 * @author nicolai.ommer
 */
class PathCollision implements IPathFinderResult
{
	private static final double MAX_FIRST_COLLISION_TIME = 1.0;
	private TrajPathV2 trajPath;
	private double collisionDurationFront;
	private double collisionDurationBack;
	private double firstCollisionTime;
	private double collisionLookahead;
	private IObstacle collider;
	
	
	double getLastValidTime()
	{
		return Math.min(firstCollisionTime, trajPath.getTotalTime());
	}
	
	
	boolean isBetterThan(final PathCollision pCollision)
	{
		if (Double.isFinite(pCollision.firstCollisionTime))
		{
			// other has a first collision
			if (Double.isFinite(firstCollisionTime))
			{
				// we have a first collision as well, let's compare
				double firstCollisionTimeDiff = Math.min(MAX_FIRST_COLLISION_TIME, firstCollisionTime)
						- Math.min(MAX_FIRST_COLLISION_TIME, pCollision.firstCollisionTime);
				if (Math.abs(firstCollisionTimeDiff) > 0.2)
				{
					return firstCollisionTimeDiff > 0;
				}
			} else
			{
				return true;
			}
		} else if (Double.isFinite(firstCollisionTime))
		{
			return false;
		}
		double firstNonCollisionTimeDiff = collisionDurationFront - pCollision.collisionDurationFront;
		if (Math.abs(firstNonCollisionTimeDiff) > 0.1)
		{
			return firstNonCollisionTimeDiff < 0;
		}
		double collisionDurationBackDiff = collisionDurationBack - pCollision.collisionDurationBack;
		if (Math.abs(collisionDurationBackDiff) > 0.3)
		{
			return collisionDurationBackDiff < 0;
		}
		double totalTimeDiff = (trajPath.getTotalTime()) - (pCollision.trajPath.getTotalTime());
		return Math.abs(totalTimeDiff) > 0.1 && totalTimeDiff < 0;
	}
	
	
	/**
	 * @return true, if there is no collision on the path
	 */
	boolean isOptimal()
	{
		return !hasIntermediateCollision() && !hasFrontCollision() && !hasBackCollision();
	}
	
	
	TrajPathV2 getTrajPath()
	{
		return trajPath;
	}
	
	
	void setTrajPath(TrajPathV2 trajPath)
	{
		this.trajPath = trajPath;
	}
	
	
	void setCollisionDurationFront(double collisionDurationFront)
	{
		this.collisionDurationFront = collisionDurationFront;
	}
	
	
	void setCollisionDurationBack(double collisionDurationBack)
	{
		this.collisionDurationBack = collisionDurationBack;
	}
	
	
	void setFirstCollisionTime(double firstCollisionTime)
	{
		this.firstCollisionTime = firstCollisionTime;
	}
	
	
	public void setCollisionLookahead(final double collisionLookahead)
	{
		this.collisionLookahead = collisionLookahead;
	}
	
	
	public void setCollider(final IObstacle collider)
	{
		this.collider = collider;
	}
	
	
	@Override
	public ITrajectory<IVector2> getTrajectory()
	{
		return trajPath;
	}
	
	
	@Override
	public double getCollisionDurationFront()
	{
		return collisionDurationFront;
	}
	
	
	@Override
	public double getCollisionDurationBack()
	{
		return collisionDurationBack;
	}
	
	
	@Override
	public double getFirstCollisionTime()
	{
		return firstCollisionTime;
	}
	
	
	@Override
	public double getCollisionLookahead()
	{
		return collisionLookahead;
	}
	
	
	@Override
	public Optional<IObstacle> getCollider()
	{
		return Optional.ofNullable(collider);
	}
}
