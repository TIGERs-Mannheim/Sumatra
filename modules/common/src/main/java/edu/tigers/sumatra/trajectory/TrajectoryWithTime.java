/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.trajectory;

import lombok.RequiredArgsConstructor;
import lombok.ToString;


/**
 * Trajectory with timestamp
 *
 * @param <T>
 */
@RequiredArgsConstructor
@ToString
public class TrajectoryWithTime<T>
{
	private final ITrajectory<T> trajectory;
	private final long tStart;


	/**
	 * @return the trajectory
	 */
	public ITrajectory<T> getTrajectory()
	{
		return trajectory;
	}


	/**
	 * @return the tStart
	 */
	public long gettStart()
	{
		return tStart;
	}


	public ITrajectory<T> synchronizeTo(long timestamp)
	{
		double age = (timestamp - tStart) / 1e9;
		return new TrajectoryWrapper<>(trajectory, age, trajectory.getTotalTime());
	}


	private double getTrajectoryTime(final long tCur)
	{
		return Math.max(0, Math.min((tCur - tStart) / 1e9, trajectory.getTotalTime()));
	}


	/**
	 * @param tCur
	 * @return
	 */
	public T getPositionMM(final long tCur)
	{
		return trajectory.getPositionMM(getTrajectoryTime(tCur));
	}


	/**
	 * @param tCur
	 * @return
	 */
	public T getPosition(final long tCur)
	{
		return trajectory.getPosition(getTrajectoryTime(tCur));
	}


	/**
	 * @param tCur
	 * @return
	 */
	public T getVelocity(final long tCur)
	{
		return trajectory.getVelocity(getTrajectoryTime(tCur));
	}


	/**
	 * @param tCur
	 * @return
	 */
	public T getAcceleration(final long tCur)
	{
		return trajectory.getAcceleration(getTrajectoryTime(tCur));
	}
}
