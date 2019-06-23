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

import javax.persistence.Embeddable;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.spline.HermiteSpline2D;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.spline.ISpline;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.util.units.DistanceUnit;


/**
 * A trajectory using only hermite splines.
 * 
 * @author AndreR
 * 
 */
@Embeddable
public class HermiteSplineTrajectory2D implements ITrajectory2D, ISpline
{
	/**
	 */
	@Embeddable
	public static class HermiteSplineTrajectoryPart2D
	{
		/** */
		public float				startTime;
		/** */
		public float				endTime;
		/** */
		public float				startWay;
		/** */
		public float				endWay;
		/** */
		public HermiteSpline2D	spline;
		
		
		/**
		 * 
		 * @param sT
		 * @param eT
		 * @param sW
		 * @param eW
		 * @param p
		 */
		public HermiteSplineTrajectoryPart2D(float sT, float eT, float sW, float eW, HermiteSpline2D p)
		{
			startTime = sT;
			endTime = eT;
			startWay = sW;
			endWay = eW;
			spline = p;
		}
	}
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private List<HermiteSplineTrajectoryPart2D>	parts			= new ArrayList<HermiteSplineTrajectoryPart2D>();
	private float											totalTime	= 0;
	private float											totalLength	= 0;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Create a hermite spline trajectory.
	 * 
	 * @param splines Splines for the pieces.
	 */
	public HermiteSplineTrajectory2D(List<HermiteSpline2D> splines)
	{
		init(splines);
	}
	
	
	/**
	 * Create a hermite spline trajectory with a single element.
	 * 
	 * @param spline Single spline element.
	 */
	public HermiteSplineTrajectory2D(HermiteSpline2D spline)
	{
		List<HermiteSpline2D> list = new ArrayList<HermiteSpline2D>();
		list.add(spline);
		
		init(list);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	private void init(List<HermiteSpline2D> splines)
	{
		parts.clear();
		totalTime = 0;
		totalLength = 0;
		
		for (HermiteSpline2D p : splines)
		{
			parts.add(new HermiteSplineTrajectoryPart2D(totalTime, totalTime + p.getEndTime(), totalLength, totalLength
					+ p.getLength(), p));
			
			totalLength += p.getLength();
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
	public void append(HermiteSplineTrajectory2D traj)
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
	public int findPart(float t)
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
	
	
	/**
	 * find the correct part according the already driven way
	 * 
	 * @param drivenWay
	 * @return
	 */
	public HermiteSplineTrajectoryPart2D findPartByWay(float drivenWay)
	{
		for (HermiteSplineTrajectoryPart2D part : parts)
		{
			if ((part.startWay <= drivenWay) && (part.endWay > drivenWay))
			{
				return part;
			}
		}
		
		return parts.get(parts.size() - 1);
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public Vector2 getPosition(float t)
	{
		HermiteSplineTrajectoryPart2D p = parts.get(findPart(t));
		
		return p.spline.value(t - p.startTime);
	}
	
	
	@Override
	public Vector2 getVelocity(float t)
	{
		HermiteSplineTrajectoryPart2D p = parts.get(findPart(t));
		
		return p.spline.firstDerivative(t - p.startTime);
	}
	
	
	@Override
	public Vector2 getAcceleration(float t)
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
	public HermiteSplineTrajectoryPart2D getPart(int part)
	{
		return parts.get(part);
	}
	
	
	/**
	 * Get spline part.
	 * 
	 * @param part
	 * @return
	 */
	public HermiteSpline2D getSpline(int part)
	{
		return parts.get(part).spline;
	}
	
	
	@Override
	public float getLength()
	{
		return parts.get(parts.size() - 1).endWay;
	}
	
	
	/**
	 * get the value of the spline / position of the bot after a certain driven way
	 * 
	 * @param drivenWay
	 * @return
	 */
	public IVector2 getValue(float drivenWay)
	{
		HermiteSplineTrajectoryPart2D part = findPartByWay(drivenWay);
		IVector2 tmp = part.spline.getValue(drivenWay - part.startWay);
		return tmp;
	}
	
	
	/**
	 * very accurate
	 * 
	 * @param drivenWay
	 * @return
	 */
	public float lengthToTime(float drivenWay)
	{
		HermiteSplineTrajectoryPart2D part = findPartByWay(drivenWay);
		return part.spline.lengthToTime(DistanceUnit.MILLIMETERS.toMeters(drivenWay - part.startWay));
	}
	
	
	@Override
	public IVector2 getValueByTime(float t)
	{
		HermiteSplineTrajectoryPart2D part = parts.get(findPart(t));
		return part.spline.value(t - part.startTime);
		
	}
	
	
	@Override
	public IVector2 getAccelerationByTime(float t)
	{
		HermiteSplineTrajectoryPart2D part = parts.get(findPart(t));
		return part.spline.firstDerivative(t - part.startTime);
	}
	
}
