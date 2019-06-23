/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 25, 2016
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.pathfinder.traj;

import java.text.DecimalFormat;

import com.github.g3force.configurable.Configurable;
import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.trajectory.ITrajectory;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent
public class TrajPathV2 implements ITrajectory<IVector2>
{
	private final ITrajectory<IVector2>	trajectory;
	private final double						tEnd;
	private final TrajPathV2				child;
	
	@Configurable
	private static double					collisionStepSize	= 0.1;
	
	@Configurable
	private static double					minPathLength		= 1;
	
	
	/**
	 * 
	 */
	@SuppressWarnings("unused")
	private TrajPathV2()
	{
		trajectory = null;
		tEnd = 0;
		child = null;
	}
	
	
	/**
	 * @param trajectory
	 * @param tEnd
	 * @param child
	 */
	public TrajPathV2(final ITrajectory<IVector2> trajectory, final double tEnd, final TrajPathV2 child)
	{
		assert (tEnd >= -0.2) : "Invalid tEnd: " + tEnd;
		this.trajectory = trajectory;
		this.tEnd = tEnd;
		this.child = child;
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
	public TrajPathV2 connect(final TrajPathV2 path, final double tConnect)
	{
		if ((child != null) && (tConnect > tEnd))
		{
			return new TrajPathV2(trajectory, tEnd, child.connect(path, tConnect - tEnd));
		}
		return new TrajPathV2(trajectory, tConnect, path);
	}
	
	
	/**
	 * @param tail
	 * @return
	 */
	public TrajPathV2 replaceTail(final TrajPathV2 tail)
	{
		if (child != null)
		{
			return new TrajPathV2(trajectory, tEnd, child.replaceTail(tail));
		}
		return tail;
	}
	
	
	/**
	 * @param tNew
	 * @return
	 */
	public TrajPathV2 removeOldParts(final double tNew)
	{
		if ((tNew >= tEnd) && (child != null))
		{
			return child.removeOldParts(tNew - tEnd);
		}
		return this;
	}
	
	
	/**
	 * @param tMax
	 * @return
	 */
	public TrajPathV2 cut(final double tMax)
	{
		if (tMax < tEnd)
		{
			return new TrajPathV2(trajectory, tMax, null);
		}
		if (child != null)
		{
			TrajPathV2 np = child.cut(tMax - tEnd);
			if (np != child)
			{
				return new TrajPathV2(trajectory, tEnd, np);
			}
		}
		return this;
	}
	
	// ##########################################################################
	
	
	/**
	 * @param tOffset
	 * @return
	 */
	public boolean isLastPart(final double tOffset)
	{
		if (child == null)
		{
			return true;
		}
		if (tOffset > tEnd)
		{
			return child.isLastPart(tOffset - tEnd);
		}
		return false;
	}
	
	
	/**
	 * @param dest
	 * @return
	 */
	public int getNumSubNodes(final IVector2 dest)
	{
		int n = 0;
		if (child != null)
		{
			n += child.getNumSubNodes(dest);
		}
		if (!trajectory.getPositionMM(Double.MAX_VALUE).isCloseTo(dest, 1))
		{
			n++;
		}
		return n;
	}
	
	
	/**
	 * @return
	 */
	@Override
	public IVector2 getFinalDestination()
	{
		if (child != null)
		{
			return child.getFinalDestination();
		}
		return trajectory.getPositionMM(tEnd);
	}
	
	
	// ##########################################################################
	
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
	
	// ##########################################################################
	
	
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
	protected TrajPathV2 getChild()
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
	
}
