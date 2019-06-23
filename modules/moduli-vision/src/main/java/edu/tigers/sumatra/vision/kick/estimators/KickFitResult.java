/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 */
package edu.tigers.sumatra.vision.kick.estimators;

import java.util.List;

import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.vision.data.ABallTrajectory;
import edu.tigers.sumatra.vision.data.FilteredVisionBall;


/**
 * @author AndreR <andre@ryll.cc>
 */
public class KickFitResult
{
	private final List<IVector2>	ground;
	private final double				avgDistance;
	private ABallTrajectory			trajectory;
	
	
	/**
	 * @param ground
	 * @param avgDistance
	 * @param trajectory
	 */
	public KickFitResult(final List<IVector2> ground, final double avgDistance, final ABallTrajectory trajectory)
	{
		this.ground = ground;
		this.avgDistance = avgDistance;
		this.trajectory = trajectory;
	}
	
	
	/**
	 * @return the ground
	 */
	public List<IVector2> getGroundProjection()
	{
		return ground;
	}
	
	
	/**
	 * @return the avgDistance
	 */
	public double getAvgDistance()
	{
		return avgDistance;
	}
	
	
	/**
	 * @return the kickPos
	 */
	public IVector2 getKickPos()
	{
		return trajectory.getKickPos();
	}
	
	
	/**
	 * @return the kickVel
	 */
	public IVector3 getKickVel()
	{
		return trajectory.getKickVel();
	}
	
	
	/**
	 * @return
	 */
	public long getKickTimestamp()
	{
		return trajectory.getKickTimestamp();
	}
	
	
	/**
	 * Get ball state at specific timestamp.
	 * 
	 * @param timestamp
	 * @return
	 */
	public FilteredVisionBall getState(final long timestamp)
	{
		return trajectory.getStateAtTimestamp(timestamp);
	}
}
