/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 31, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.trajectory;

import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.VectorMath;


/**
 * Util class for splines
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public final class TrajectoryMath
{
	private static final double DT_PRECISE = 0.01;
	
	
	private TrajectoryMath()
	{
	}
	
	
	/**
	 * for the defense points, LOW PRECISION, BAD PERFORMANCE
	 * 
	 * @param spline
	 * @param drivenWay [mm]
	 * @return
	 */
	public static double timeAfterDrivenWay(final ITrajectory<IVector2> spline, final double drivenWay)
	{
		return timeAfterDrivenWay(spline, drivenWay, DT_PRECISE);
	}
	
	
	/**
	 * @param spline
	 * @param drivenWay [mm]
	 * @param dt [s]
	 * @return
	 */
	public static double timeAfterDrivenWay(final ITrajectory<IVector2> spline, final double drivenWay, final double dt)
	{
		double length = 0;
		double drivenWayMeters = drivenWay / 1000;
		for (double t = 0.0; t < spline.getTotalTime(); t += dt)
		{
			length += spline.getVelocity(t).getLength2() * dt;
			if (length >= drivenWayMeters)
			{
				return t;
			}
		}
		return spline.getTotalTime();
	}
	
	
	/**
	 * Calculate the length of the spline by integration
	 * 
	 * @param spline
	 * @return distance [mm]
	 */
	public static double length(final ITrajectory<IVector3> spline)
	{
		return length(spline, DT_PRECISE);
	}
	
	
	/**
	 * Calculate the length of the spline by integration
	 * 
	 * @param spline
	 * @param dt smaller->more precision, less performance; e.g. 0.01
	 * @return distance [mm]
	 */
	public static double length(final ITrajectory<IVector3> spline, final double dt)
	{
		double length = 0;
		for (double t = 0.0; t < spline.getTotalTime(); t += dt)
		{
			length += spline.getVelocity(t).getLength2() * dt;
		}
		return length * 1000;
	}
	
	
	/**
	 * Find the time where p is nearest to the spline
	 * 
	 * @param spline
	 * @param p
	 * @return
	 */
	public static double timeNearest2Point(final ITrajectory<IVector3> spline, final IVector2 p)
	{
		return timeNearest2Point(spline, p, 0.1);
	}
	
	
	/**
	 * Find the time where p is nearest to the spline
	 * Take care of the size of dt!
	 * 
	 * @param spline
	 * @param p
	 * @param dt
	 * @return
	 */
	public static double timeNearest2Point(final ITrajectory<IVector3> spline, final IVector2 p, final double dt)
	{
		double nearestDist = Double.MAX_VALUE;
		double time = 0;
		assert Double.isFinite(spline.getTotalTime());
		for (double t = 0; t < spline.getTotalTime(); t += dt)
		{
			IVector2 p2 = spline.getPositionMM(t).getXYVector();
			double dist = VectorMath.distancePP(p, p2);
			if (dist < nearestDist)
			{
				nearestDist = dist;
				time = t;
			}
		}
		return time;
	}
}
