/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.vision.kick.estimators;

import edu.tigers.sumatra.ball.BallState;
import edu.tigers.sumatra.ball.trajectory.IBallTrajectory;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.List;


/**
 * Result of a fitted kick.
 */
@Value
@Builder(setterPrefix = "with", toBuilder = true)
@AllArgsConstructor
public class KickFitResult
{
	List<IVector2> groundProjection;
	double avgDistance;
	IBallTrajectory trajectory;
	long kickTimestamp;
	String solverName;


	/**
	 * @return the kickPos
	 */
	public IVector2 getKickPos()
	{
		return trajectory.getInitialPos().getXYVector();
	}


	/**
	 * @return the kickVel
	 */
	public IVector3 getKickVel()
	{
		return trajectory.getInitialVel();
	}


	/**
	 * Get ball state at specific timestamp.
	 *
	 * @param timestamp
	 * @return
	 */
	public BallState getState(final long timestamp)
	{
		return trajectory.getMilliStateAtTime((timestamp - kickTimestamp) * 1e-9);
	}
}
