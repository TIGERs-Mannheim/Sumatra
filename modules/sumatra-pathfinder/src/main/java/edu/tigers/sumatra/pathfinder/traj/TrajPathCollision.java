/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.traj;

import edu.tigers.sumatra.pathfinder.IPathFinderResult;
import edu.tigers.sumatra.pathfinder.obstacles.IObstacle;

import java.util.Optional;


/**
 * Data structure of a collision on a trajectory path.
 */
class TrajPathCollision implements IPathFinderResult
{
	private TrajPath trajPath;
	private double collisionDurationFront;
	private double collisionDurationBack;
	private double firstCollisionTime;
	private double collisionLookahead;
	private IObstacle collider;
	private double penaltyScore;
	private double collisionPenalty;


	double getLastValidTime()
	{
		return Math.min(firstCollisionTime, trajPath.getTotalTime());
	}


	/**
	 * @return true, if there is no collision on the path
	 */
	boolean isOk()
	{
		return !hasCollision();
	}


	TrajPath getTrajPath()
	{
		return trajPath;
	}


	void setTrajPath(TrajPath trajPath)
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


	public void setCollisionPenalty(final double collisionPenalty)
	{
		this.collisionPenalty = collisionPenalty;
	}


	@Override
	public double getPenaltyScore()
	{
		return penaltyScore;
	}


	public void setPenaltyScore(final double penaltyScore)
	{
		this.penaltyScore = penaltyScore;
	}


	@Override
	public TrajPath getTrajectory()
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


	@Override
	public double getCollisionPenalty()
	{
		return collisionPenalty;
	}


	@Override
	public double getFirstPossibleCollision()
	{
		if (hasBackCollision())
		{
			return getLastNonCollisionTime();
		} else if (hasIntermediateCollision())
		{
			return getFirstCollisionTime();
		}
		return collisionLookahead;
	}
}
