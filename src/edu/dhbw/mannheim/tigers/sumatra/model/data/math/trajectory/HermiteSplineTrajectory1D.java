/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 26.03.2013
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory;

import java.util.ArrayList;
import java.util.List;

import com.sleepycat.persist.model.Persistent;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.spline.HermiteSpline;


/**
 * A trajectory using only hermite splines.
 * 
 * @author AndreR
 * 
 */
@Persistent
public class HermiteSplineTrajectory1D implements ITrajectory1D
{
	/**
	 */
	@Persistent
	public static class HermiteSplineTrajectoryPart1D
	{
		/** */
		public float			start;
		/** */
		public float			end;
		/** */
		public HermiteSpline	spline;
		
		
		@SuppressWarnings("unused")
		private HermiteSplineTrajectoryPart1D()
		{
		}
		
		
		/**
		 * 
		 * @param s
		 * @param e
		 * @param p
		 */
		public HermiteSplineTrajectoryPart1D(float s, float e, HermiteSpline p)
		{
			start = s;
			end = e;
			spline = p;
		}
		
		
		/**
		 * @param part
		 */
		public HermiteSplineTrajectoryPart1D(HermiteSplineTrajectoryPart1D part)
		{
			start = part.start;
			end = part.end;
			spline = new HermiteSpline(part.spline);
		}
	}
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private List<HermiteSplineTrajectoryPart1D>	parts			= new ArrayList<HermiteSplineTrajectoryPart1D>();
	private float											totalTime	= 0;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@SuppressWarnings("unused")
	private HermiteSplineTrajectory1D()
	{
	}
	
	
	/**
	 * Create a hermite spline trajectory.
	 * 
	 * @param splines Splines for the pieces.
	 */
	public HermiteSplineTrajectory1D(List<HermiteSpline> splines)
	{
		init(splines);
	}
	
	
	/**
	 * Create a hermite spline trajectory with a single element.
	 * 
	 * @param spline Single spline element.
	 */
	public HermiteSplineTrajectory1D(HermiteSpline spline)
	{
		List<HermiteSpline> list = new ArrayList<HermiteSpline>();
		list.add(spline);
		
		init(list);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param rotation
	 */
	public HermiteSplineTrajectory1D(HermiteSplineTrajectory1D rotation)
	{
		for (HermiteSplineTrajectoryPart1D part : rotation.parts)
		{
			parts.add(new HermiteSplineTrajectoryPart1D(part));
		}
		totalTime = rotation.totalTime;
	}
	
	
	private void init(List<HermiteSpline> splines)
	{
		parts.clear();
		totalTime = 0;
		
		for (HermiteSpline p : splines)
		{
			parts.add(new HermiteSplineTrajectoryPart1D(totalTime, totalTime + p.getEndTime(), p));
			
			totalTime += p.getEndTime();
		}
	}
	
	
	/**
	 * Append traj to <b>this</b> trajectory.<br>
	 * The function does not check the appended trajectory for any jumps in position, velocity or acceleration
	 * profiles. It is the responsibility of the user to make sure that the trajectory is still smooth after appending.
	 * 
	 * @param traj Trajectory to append.
	 */
	public void append(HermiteSplineTrajectory1D traj)
	{
		List<HermiteSpline> list = new ArrayList<HermiteSpline>();
		
		// add original parts
		for (HermiteSplineTrajectoryPart1D part : parts)
		{
			list.add(part.spline);
		}
		
		// append new parts
		for (HermiteSplineTrajectoryPart1D part : traj.parts)
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
	public int findPart(float t)
	{
		for (int i = 0; i < parts.size(); i++)
		{
			HermiteSplineTrajectoryPart1D p = parts.get(i);
			
			if ((t >= p.start) && (t < p.end))
			{
				return i;
			}
		}
		
		return parts.size() - 1;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public float getPosition(float t)
	{
		HermiteSplineTrajectoryPart1D p = parts.get(findPart(t));
		
		return p.spline.value(t - p.start);
	}
	
	
	@Override
	public float getVelocity(float t)
	{
		HermiteSplineTrajectoryPart1D p = parts.get(findPart(t));
		
		return p.spline.firstDerivative(t - p.start);
	}
	
	
	@Override
	public float getAcceleration(float t)
	{
		HermiteSplineTrajectoryPart1D p = parts.get(findPart(t));
		
		return p.spline.secondDerivative(t - p.start);
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
	public HermiteSplineTrajectoryPart1D getPart(int part)
	{
		return parts.get(part);
	}
	
	
	/**
	 * Get spline part.
	 * 
	 * @param part
	 * @return
	 */
	public HermiteSpline getSpline(int part)
	{
		return parts.get(part).spline;
	}
	
	
	/**
	 */
	public void mirror()
	{
		for (HermiteSplineTrajectoryPart1D part : parts)
		{
			part.spline.mirrorRotation();
		}
	}
}
