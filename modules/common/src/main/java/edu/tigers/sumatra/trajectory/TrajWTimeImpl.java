/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 26, 2016
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.trajectory;

import com.sleepycat.persist.model.Persistent;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * @param <RETURN_TYPE>
 * @param <TRAJ>
 */
@Persistent
public class TrajWTimeImpl<RETURN_TYPE, TRAJ extends ITrajectory<RETURN_TYPE>>
{
	private final TRAJ	trajectory;
	private final long	tStart;
	
	
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
	public TrajWTimeImpl(final TRAJ trajectory, final long tStart)
	{
		super();
		assert trajectory != null;
		this.trajectory = trajectory;
		this.tStart = tStart;
	}
	
	
	/**
	 * @return the trajectory
	 */
	public TRAJ getTrajectory()
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
	public RETURN_TYPE getPositionMM(final long tCur)
	{
		return trajectory.getPositionMM(getTrajectoryTime(tCur));
	}
	
	
	/**
	 * @param tCur
	 * @return
	 */
	public RETURN_TYPE getPosition(final long tCur)
	{
		return trajectory.getPosition(getTrajectoryTime(tCur));
	}
	
	
	/**
	 * @param tCur
	 * @return
	 */
	public RETURN_TYPE getVelocity(final long tCur)
	{
		return trajectory.getVelocity(getTrajectoryTime(tCur));
	}
	
	
	/**
	 * @param tCur
	 * @return
	 */
	public RETURN_TYPE getAcceleration(final long tCur)
	{
		return trajectory.getAcceleration(getTrajectoryTime(tCur));
	}
	
	
	/**
	 * @param tCur
	 * @return
	 */
	public RETURN_TYPE getNextDestination(final long tCur)
	{
		return trajectory.getNextDestination(getTrajectoryTime(tCur));
	}
	
	
	/**
	 * @return
	 */
	public RETURN_TYPE getFinalDestination()
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
