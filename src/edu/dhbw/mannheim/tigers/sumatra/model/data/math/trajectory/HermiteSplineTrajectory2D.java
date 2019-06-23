/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 26.03.2013
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory;

import java.util.ArrayList;
import java.util.List;

import com.sleepycat.persist.model.Persistent;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.spline.HermiteSpline2D;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;


/**
 * A trajectory using only hermite splines.
 * 
 * @author AndreR
 */
@Persistent(version = 1)
public class HermiteSplineTrajectory2D implements ITrajectory2D
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private List<HermiteSplineTrajectoryPart2D>	parts			= new ArrayList<HermiteSplineTrajectoryPart2D>();
	private float											totalTime	= 0;
	
	/**
	 */
	@Persistent(version = 1)
	public static class HermiteSplineTrajectoryPart2D
	{
		/** */
		public float				startTime;
		/** */
		public float				endTime;
		/** */
		public HermiteSpline2D	spline;
		
		
		@SuppressWarnings("unused")
		private HermiteSplineTrajectoryPart2D()
		{
		}
		
		
		/**
		 * @param sT
		 * @param eT
		 * @param p
		 */
		public HermiteSplineTrajectoryPart2D(final float sT, final float eT, final HermiteSpline2D p)
		{
			startTime = sT;
			endTime = eT;
			spline = p;
		}
		
		
		/**
		 * @param part
		 */
		public HermiteSplineTrajectoryPart2D(final HermiteSplineTrajectoryPart2D part)
		{
			startTime = part.startTime;
			endTime = part.endTime;
			spline = new HermiteSpline2D(part.spline);
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@SuppressWarnings("unused")
	private HermiteSplineTrajectory2D()
	{
	}
	
	
	/**
	 * Create a hermite spline trajectory.
	 * 
	 * @param splines Splines for the pieces.
	 */
	public HermiteSplineTrajectory2D(final List<HermiteSpline2D> splines)
	{
		init(splines);
	}
	
	
	/**
	 * Create a hermite spline trajectory with a single element.
	 * 
	 * @param spline Single spline element.
	 */
	public HermiteSplineTrajectory2D(final HermiteSpline2D spline)
	{
		List<HermiteSpline2D> list = new ArrayList<HermiteSpline2D>();
		list.add(spline);
		
		init(list);
	}
	
	
	/**
	 * @param position
	 */
	public HermiteSplineTrajectory2D(final HermiteSplineTrajectory2D position)
	{
		for (HermiteSplineTrajectoryPart2D part : position.parts)
		{
			parts.add(new HermiteSplineTrajectoryPart2D(part));
		}
		totalTime = position.totalTime;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	private void init(final List<HermiteSpline2D> splines)
	{
		parts.clear();
		totalTime = 0;
		
		for (HermiteSpline2D p : splines)
		{
			parts.add(new HermiteSplineTrajectoryPart2D(totalTime, totalTime + p.getEndTime(), p));
			totalTime += p.getEndTime();
		}
		assert Float.isFinite(totalTime);
	}
	
	
	/**
	 */
	public void mirror()
	{
		for (HermiteSplineTrajectoryPart2D part : parts)
		{
			part.spline.mirror();
		}
	}
	
	
	/**
	 * Append traj to <b>this</b> trajectory.<br>
	 * The function does not check the appended trajectory for any jumps in position, velocity or acceleration
	 * profiles. It is the responsibility of the user to make sure that the trajectory is still smooth after appending.
	 * 
	 * @param traj Trajectory to append.
	 */
	public void append(final HermiteSplineTrajectory2D traj)
	{
		List<HermiteSpline2D> list = new ArrayList<HermiteSpline2D>();
		
		// add original parts
		for (HermiteSplineTrajectoryPart2D part : parts)
		{
			list.add(part.spline);
		}
		
		// append new parts
		for (HermiteSplineTrajectoryPart2D part : traj.parts)
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
	public int findPart(final float t)
	{
		for (int i = 0; i < parts.size(); i++)
		{
			HermiteSplineTrajectoryPart2D p = parts.get(i);
			
			if ((t >= p.startTime) && (t < p.endTime))
			{
				return i;
			}
		}
		
		return parts.size() - 1;
	}
	
	
	@Override
	public Vector2 getPosition(final float t)
	{
		HermiteSplineTrajectoryPart2D p = parts.get(findPart(t));
		
		return p.spline.value(t - p.startTime);
	}
	
	
	@Override
	public Vector2 getVelocity(final float t)
	{
		HermiteSplineTrajectoryPart2D p = parts.get(findPart(t));
		
		return p.spline.firstDerivative(t - p.startTime);
	}
	
	
	@Override
	public Vector2 getAcceleration(final float t)
	{
		HermiteSplineTrajectoryPart2D p = parts.get(findPart(t));
		
		return p.spline.secondDerivative(t - p.startTime);
	}
	
	
	@Override
	public float getTotalTime()
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
	public HermiteSplineTrajectoryPart2D getPart(final int part)
	{
		return parts.get(part);
	}
	
	
	/**
	 * Get spline part.
	 * 
	 * @param part
	 * @return
	 */
	public HermiteSpline2D getSpline(final int part)
	{
		return parts.get(part).spline;
	}
}
