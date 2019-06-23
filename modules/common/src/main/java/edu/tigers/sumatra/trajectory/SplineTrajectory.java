/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 26.03.2013
 * Author(s): AndreR
 * *********************************************************
 */
package edu.tigers.sumatra.trajectory;

import java.util.ArrayList;
import java.util.List;

import com.sleepycat.persist.model.Persistent;


/**
 * A trajectory using only hermite splines.
 * 
 * @author AndreR
 * @param <ReturnType>
 */
@Persistent
public class SplineTrajectory<ReturnType> implements ITrajectory<ReturnType>
{
	private final List<SplinePart<ReturnType>>	parts			= new ArrayList<SplinePart<ReturnType>>();
	private double											totalTime	= 0;
																				
	/**
	 * @param <ReturnType>
	 */
	@Persistent(version = 1)
	public static class SplinePart<ReturnType>
	{
		/** */
		public double							startTime;
		/** */
		public double							endTime;
		/** */
		public ITrajectory<ReturnType>	spline;
													
													
		@SuppressWarnings("unused")
		private SplinePart()
		{
		}
		
		
		/**
		 * @param sT
		 * @param eT
		 * @param p
		 */
		public SplinePart(final double sT, final double eT, final ITrajectory<ReturnType> p)
		{
			startTime = sT;
			endTime = eT;
			spline = p;
		}
		
		
		/**
		 * @param part
		 */
		public SplinePart(final SplinePart<ReturnType> part)
		{
			startTime = part.startTime;
			endTime = part.endTime;
			spline = part.spline;
		}
	}
	
	
	@SuppressWarnings("unused")
	private SplineTrajectory()
	{
	}
	
	
	/**
	 * Create a hermite spline trajectory.
	 * 
	 * @param splines Splines for the pieces.
	 */
	public SplineTrajectory(final List<ITrajectory<ReturnType>> splines)
	{
		init(splines);
	}
	
	
	/**
	 * Create a hermite spline trajectory with a single element.
	 * 
	 * @param spline Single spline element.
	 */
	public SplineTrajectory(final ITrajectory<ReturnType> spline)
	{
		List<ITrajectory<ReturnType>> list = new ArrayList<ITrajectory<ReturnType>>();
		list.add(spline);
		
		init(list);
	}
	
	
	/**
	 * @param position
	 */
	public SplineTrajectory(final SplineTrajectory<ReturnType> position)
	{
		for (SplinePart<ReturnType> part : position.parts)
		{
			parts.add(new SplinePart<ReturnType>(part));
		}
		totalTime = position.totalTime;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	private void init(final List<ITrajectory<ReturnType>> splines)
	{
		parts.clear();
		totalTime = 0;
		
		for (ITrajectory<ReturnType> p : splines)
		{
			parts.add(new SplinePart<ReturnType>(totalTime, totalTime + p.getTotalTime(), p));
			totalTime += p.getTotalTime();
		}
		assert Double.isFinite(totalTime);
	}
	
	
	/**
	 * Append traj to <b>this</b> trajectory.<br>
	 * The function does not check the appended trajectory for any jumps in position, velocity or acceleration
	 * profiles. It is the responsibility of the user to make sure that the trajectory is still smooth after appending.
	 * 
	 * @param traj Trajectory to append.
	 */
	public void append(final SplineTrajectory<ReturnType> traj)
	{
		List<ITrajectory<ReturnType>> list = new ArrayList<>();
		
		// add original parts
		for (SplinePart<ReturnType> part : parts)
		{
			list.add(part.spline);
		}
		
		// append new parts
		for (SplinePart<ReturnType> part : traj.parts)
		{
			list.add(part.spline);
		}
		
		// and re-init :)
		init(list);
	}
	
	
	/**
	 * Find the part for a specific time.
	 * 
	 * @param t time
	 * @return part index
	 */
	public int findPart(final double t)
	{
		for (int i = 0; i < parts.size(); i++)
		{
			SplinePart<ReturnType> p = parts.get(i);
			
			if ((t >= p.startTime) && (t < p.endTime))
			{
				return i;
			}
		}
		
		return parts.size() - 1;
	}
	
	
	@Override
	public ReturnType getPositionMM(final double t)
	{
		SplinePart<ReturnType> p = parts.get(findPart(t));
		
		return p.spline.getPositionMM(t - p.startTime);
	}
	
	
	@Override
	public ReturnType getPosition(final double t)
	{
		SplinePart<ReturnType> p = parts.get(findPart(t));
		
		return p.spline.getPosition(t - p.startTime);
	}
	
	
	@Override
	public ReturnType getVelocity(final double t)
	{
		SplinePart<ReturnType> p = parts.get(findPart(t));
		
		return p.spline.getVelocity(t - p.startTime);
	}
	
	
	@Override
	public ReturnType getAcceleration(final double t)
	{
		SplinePart<ReturnType> p = parts.get(findPart(t));
		
		return p.spline.getAcceleration(t - p.startTime);
	}
	
	
	@Override
	public double getTotalTime()
	{
		return totalTime;
	}
	
	
	/**
	 * Number of spline parts.
	 * 
	 * @return
	 */
	public int getNumParts()
	{
		return parts.size();
	}
	
	
	/**
	 * Get a part.
	 * 
	 * @param part part index
	 * @return PartInfo
	 */
	public SplinePart<ReturnType> getPart(final int part)
	{
		return parts.get(part);
	}
	
	
	/**
	 * Get spline part.
	 * 
	 * @param part
	 * @return
	 */
	public ITrajectory<ReturnType> getSpline(final int part)
	{
		return parts.get(part).spline;
	}
}
