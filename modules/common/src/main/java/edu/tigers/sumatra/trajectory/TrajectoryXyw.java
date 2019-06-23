/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 26, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.trajectory;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.IVector3;
import edu.tigers.sumatra.math.Vector3;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent
public class TrajectoryXyw implements ITrajectory<IVector3>
{
	private final ITrajectory<IVector2>	trajXy;
	private final ITrajectory<Double>	trajW;
													
													
	@SuppressWarnings("unused")
	private TrajectoryXyw()
	{
		this(null, null);
	}
	
	
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 * @param trajXy
	 * @param trajW
	 */
	public TrajectoryXyw(final ITrajectory<IVector2> trajXy, final ITrajectory<Double> trajW)
	{
		this.trajXy = trajXy;
		this.trajW = trajW;
	}
	
	
	@Override
	public IVector3 getPositionMM(final double t)
	{
		return new Vector3(trajXy.getPositionMM(t), trajW.getPositionMM(t));
	}
	
	
	@Override
	public IVector3 getPosition(final double t)
	{
		return new Vector3(trajXy.getPosition(t), trajW.getPosition(t));
	}
	
	
	@Override
	public IVector3 getVelocity(final double t)
	{
		return new Vector3(trajXy.getVelocity(t), trajW.getVelocity(t));
	}
	
	
	@Override
	public IVector3 getAcceleration(final double t)
	{
		return new Vector3(trajXy.getAcceleration(t), trajW.getAcceleration(t));
	}
	
	
	@Override
	public double getTotalTime()
	{
		return Math.max(trajXy.getTotalTime(), trajW.getTotalTime());
	}
}
