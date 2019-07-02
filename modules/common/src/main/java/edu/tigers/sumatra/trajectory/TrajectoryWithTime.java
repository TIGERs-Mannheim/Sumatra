/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 23, 2016
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.trajectory;

import com.sleepycat.persist.model.Persistent;


/**
 * Trajectory with timestamp
 * 
 * @param <T>
 */
@Persistent
public class TrajectoryWithTime<T> extends TrajWTimeImpl<T, ITrajectory<T>>
{
	@SuppressWarnings("unused")
	private TrajectoryWithTime()
	{
		super();
	}
	
	
	/**
	 * @param trajectory
	 * @param tStart
	 */
	public TrajectoryWithTime(final ITrajectory<T> trajectory, final long tStart)
	{
		super(trajectory, tStart);
	}
}
