/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
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
	private TrajPathV2 trajPath;
	private double collisionDurationFront;
	private double collisionDurationBack;
	private double firstCollisionTime;
	private double collisionLookahead;
	private IObstacle collider;
	private double penaltyScore;
	
	
	double getLastValidTime()
	{
		return Math.min(firstCollisionTime, trajPath.getTotalTime());
	}
	
	
	/**
	 * @return true, if there is no collision on the path
	 */
	boolean isOk()
	{
		// ignoring the back collision intentionally here
		// if there is a back collision, there will be one for every path
		// the advantages of reducing this time is small and rather produces non-optimal paths
		return !hasIntermediateCollision() && !hasFrontCollision();
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
	public double getPenaltyScore()
	{
		return penaltyScore;
	}
	
	
	public void setPenaltyScore(final double penaltyScore)
	{
		this.penaltyScore = penaltyScore;
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
