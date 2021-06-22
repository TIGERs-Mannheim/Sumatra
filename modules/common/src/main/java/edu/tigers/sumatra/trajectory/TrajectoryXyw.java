/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.trajectory;

import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector3;
import lombok.Value;


/**
 * Three dimensional trajectory.
 */
@Value
public class TrajectoryXyw implements ITrajectory<IVector3>
{
	ITrajectory<IVector2> trajXy;
	ITrajectory<Double> trajW;


	@Override
	public IVector3 getPositionMM(final double t)
	{
		return Vector3.from2d(trajXy.getPositionMM(t), trajW.getPosition(t));
	}


	@Override
	public IVector3 getPosition(final double t)
	{
		return Vector3.from2d(trajXy.getPosition(t), trajW.getPosition(t));
	}


	@Override
	public IVector3 getVelocity(final double t)
	{
		return Vector3.from2d(trajXy.getVelocity(t), trajW.getVelocity(t));
	}


	@Override
	public IVector3 getAcceleration(final double t)
	{
		return Vector3.from2d(trajXy.getAcceleration(t), trajW.getAcceleration(t));
	}


	@Override
	public double getTotalTime()
	{
		return Math.max(trajXy.getTotalTime(), trajW.getTotalTime());
	}


	@Override
	public TrajectoryXyw mirrored()
	{
		return new TrajectoryXyw(trajXy.mirrored(), trajW.mirrored());
	}
}
