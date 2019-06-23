/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - Tigers Mannheim
 */
package edu.tigers.sumatra.wp.data;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.vision.data.ABallTrajectory;
import edu.tigers.sumatra.vision.data.FilteredVisionBall;


/**
 * @author AndreR <andre@ryll.cc>
 */
@Persistent
public class BallKickFitState
{
	private final IVector2 kickPos;
	private final IVector3 kickVel;
	private final long kickTimestamp;
	
	
	@SuppressWarnings("unused")
	private BallKickFitState()
	{
		kickPos = null;
		kickVel = null;
		kickTimestamp = 0;
	}
	
	
	/**
	 * @param kickPos
	 * @param kickVel
	 * @param kickTimestamp
	 */
	public BallKickFitState(final IVector2 kickPos, final IVector3 kickVel, final long kickTimestamp)
	{
		this.kickPos = kickPos;
		this.kickVel = kickVel;
		this.kickTimestamp = kickTimestamp;
	}
	
	
	/**
	 * @param filteredBallState
	 * @param timestampNow
	 */
	public BallKickFitState(final FilteredVisionBall filteredBallState, final long timestampNow)
	{
		ABallTrajectory trajectory = filteredBallState.getTrajectory(timestampNow);
		kickPos = trajectory.getKickPos();
		kickVel = trajectory.getKickVel();
		kickTimestamp = trajectory.getKickTimestamp();
	}
	
	
	/**
	 * @return the kickPos
	 */
	public IVector2 getKickPos()
	{
		return kickPos;
	}
	
	
	/**
	 * @return the kickVel
	 */
	public IVector3 getKickVel()
	{
		return kickVel;
	}
	
	
	/**
	 * @return the kickTimestamp
	 */
	public long getKickTimestamp()
	{
		return kickTimestamp;
	}
}
