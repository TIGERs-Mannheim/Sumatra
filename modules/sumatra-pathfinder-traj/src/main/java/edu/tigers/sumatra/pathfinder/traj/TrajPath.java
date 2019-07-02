/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.pathfinder.traj;

import java.text.DecimalFormat;

import edu.tigers.sumatra.bot.MoveConstraints;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.pathfinder.TrajectoryGenerator;
import edu.tigers.sumatra.trajectory.ITrajectory;


/**
 * A path of connected segments, based on trajectories.
 */
public class TrajPath implements ITrajectory<IVector2>
{
	private final ITrajectory<IVector2> trajectory;
	private final double tEnd;
	private final TrajPath child;
	
	
	public TrajPath(final ITrajectory<IVector2> trajectory, final double tEnd, final TrajPath child)
	{
		assert (tEnd >= -0.2) : "Invalid tEnd: " + tEnd;
		this.trajectory = trajectory;
		this.tEnd = tEnd;
		this.child = child;
	}
	
	
	/**
	 * Create a new path with the given inputs.
	 * 
	 * @param moveConstraints the limits to apply
	 * @param curPos the current position
	 * @param curVel the current velocity
	 * @param dest the desired target position
	 * @return a new path
	 */
	public static TrajPath with(
			final MoveConstraints moveConstraints,
			final IVector2 curPos,
			final IVector2 curVel,
			final IVector2 dest)
	{
		ITrajectory<IVector2> traj = TrajectoryGenerator.generatePositionTrajectory(moveConstraints, curPos, curVel,
				dest);
		return new TrajPath(traj, traj.getTotalTime(), null);
	}
	
	
	/**
	 * Append a new segment to this path at the given time and with the given new destination
	 * 
	 * @param moveConstraints the limits to apply
	 * @param connectionTime the time on the current path where the new segment should be appended
	 * @param dest the new desired target position
	 * @return a new path
	 */
	public TrajPath append(
			final MoveConstraints moveConstraints,
			final double connectionTime,
			final IVector2 dest)
	{
		IVector2 curPos = this.getPositionMM(connectionTime);
		IVector2 curVel = this.getVelocity(connectionTime);
		TrajPath childPath = with(moveConstraints, curPos, curVel, dest);
		return this.connect(childPath, connectionTime);
	}
	
	
	/**
	 * Put path behind this path, cutting any nodes that are behind tConnect.
	 * The result will start with one new path node, with path as its child
	 * and tConnect as node duration
	 * 
	 * @param path
	 * @param tConnect
	 * @return
	 */
	public TrajPath connect(final TrajPath path, final double tConnect)
	{
		if ((child != null) && (tConnect > tEnd))
		{
			return new TrajPath(trajectory, tEnd, child.connect(path, tConnect - tEnd));
		}
		return new TrajPath(trajectory, tConnect, path);
	}
	
	
	/**
	 * @param tNew
	 * @return
	 */
	public TrajPath removeOldParts(final double tNew)
	{
		if ((tNew >= tEnd) && (child != null))
		{
			return child.removeOldParts(tNew - tEnd);
		}
		return this;
	}
	
	
	@Override
	public IVector2 getFinalDestination()
	{
		if (child != null)
		{
			return child.getFinalDestination();
		}
		return trajectory.getPositionMM(tEnd);
	}
	
	
	@Override
	public IVector2 getPositionMM(final double t)
	{
		if (t <= tEnd)
		{
			return trajectory.getPositionMM(t);
		}
		if (child != null)
		{
			return child.getPositionMM(t - tEnd);
		}
		return trajectory.getPositionMM(tEnd);
	}
	
	
	@Override
	public IVector2 getPosition(final double t)
	{
		if (t <= tEnd)
		{
			return trajectory.getPosition(t);
		}
		if (child != null)
		{
			return child.getPosition(t - tEnd);
		}
		return trajectory.getPosition(tEnd);
	}
	
	
	@Override
	public IVector2 getVelocity(final double t)
	{
		if (t <= tEnd)
		{
			return trajectory.getVelocity(t);
		}
		if (child != null)
		{
			return child.getVelocity(t - tEnd);
		}
		return trajectory.getVelocity(tEnd);
	}
	
	
	@Override
	public IVector2 getAcceleration(final double t)
	{
		if (t <= tEnd)
		{
			return trajectory.getAcceleration(t);
		}
		if (child != null)
		{
			return child.getAcceleration(t - tEnd);
		}
		return trajectory.getAcceleration(tEnd);
	}
	
	
	@Override
	public double getTotalTime()
	{
		if (child != null)
		{
			return tEnd + child.getTotalTime();
		}
		return tEnd;
	}
	
	
	@Override
	public String toString()
	{
		IVector2 p0 = trajectory.getPositionMM(0);
		IVector2 p1 = trajectory.getPositionMM(tEnd);
		DecimalFormat format = new DecimalFormat("0.000");
		
		StringBuilder builder = new StringBuilder();
		builder.append("[");
		builder.append(p0);
		builder.append("->");
		builder.append(p1);
		builder.append(": ");
		builder.append(format.format(tEnd));
		builder.append("/");
		builder.append(format.format(getTotalTime()));
		builder.append("s");
		if (child != null)
		{
			builder.append("\n=>");
			builder.append(child.toString());
		}
		builder.append("]");
		return builder.toString();
	}
	
	
	/**
	 * @return the trajectory
	 */
	protected ITrajectory<IVector2> getTrajectory()
	{
		return trajectory;
	}
	
	
	/**
	 * @return the tEnd
	 */
	protected double gettEnd()
	{
		return tEnd;
	}
	
	
	/**
	 * @return the child
	 */
	protected TrajPath getChild()
	{
		return child;
	}
	
	
	@Override
	public IVector2 getNextDestination(final double t)
	{
		if ((child == null) || (t < tEnd))
		{
			return trajectory.getPositionMM(trajectory.getTotalTime());
		}
		return child.getNextDestination(t - tEnd);
	}
	
	
	@Override
	public TrajPath mirrored()
	{
		return new TrajPath(trajectory.mirrored(), tEnd, child == null ? null : child.mirrored());
	}
}
