/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.trajectory;

import com.sleepycat.persist.model.Persistent;


/**
 * Trajectory with timestamp
 * 
 * @param <R>
 * @param <T>
 */
@Persistent
public class TrajWTimeImpl<R, T extends ITrajectory<R>>
{
	private final T trajectory;
	private final long tStart;
	
	
	@SuppressWarnings("unused")
	protected TrajWTimeImpl()
	{
		trajectory = null;
		tStart = 0;
	}
	
	
	/**
	 * @param trajectory
	 * @param tStart
	 */
	public TrajWTimeImpl(final T trajectory, final long tStart)
	{
		super();
		assert trajectory != null;
		this.trajectory = trajectory;
		this.tStart = tStart;
	}
	
	
	/**
	 * @return the trajectory
	 */
	public T getTrajectory()
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
	
	
	/**
	 * @param tCur
	 * @return
	 */
	public double getTrajectoryTime(final long tCur)
	{
		return Math.max(0, Math.min((tCur - tStart) / 1e9, trajectory.getTotalTime()));
	}
	
	
	/**
	 * @param tCur
	 * @return
	 */
	public double getRemainingTrajectoryTime(final long tCur)
	{
		return Math.max(0, trajectory.getTotalTime() - getTrajectoryTime(tCur));
	}
	
	
	/**
	 * @param tCur
	 * @return
	 */
	public R getPositionMM(final long tCur)
	{
		return trajectory.getPositionMM(getTrajectoryTime(tCur));
	}
	
	
	/**
	 * @param tCur
	 * @return
	 */
	public R getPosition(final long tCur)
	{
		return trajectory.getPosition(getTrajectoryTime(tCur));
	}
	
	
	/**
	 * @param tCur
	 * @return
	 */
	public R getVelocity(final long tCur)
	{
		return trajectory.getVelocity(getTrajectoryTime(tCur));
	}
	
	
	/**
	 * @param tCur
	 * @return
	 */
	public R getAcceleration(final long tCur)
	{
		return trajectory.getAcceleration(getTrajectoryTime(tCur));
	}
	
	
	/**
	 * @param tCur
	 * @return
	 */
	public R getNextDestination(final long tCur)
	{
		return trajectory.getNextDestination(getTrajectoryTime(tCur));
	}
	
	
	/**
	 * @return
	 */
	public R getFinalDestination()
	{
		return trajectory.getPositionMM(trajectory.getTotalTime());
	}
	
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("[traj=");
		builder.append(trajectory);
		builder.append(", tStart=");
		builder.append(tStart);
		builder.append("]");
		return builder.toString();
	}
}
