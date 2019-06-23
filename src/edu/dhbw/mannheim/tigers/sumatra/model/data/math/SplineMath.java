/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 31, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.math;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.spline.ISpline;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;
import edu.dhbw.mannheim.tigers.sumatra.util.units.DistanceUnit;


/**
 * Util class for splines
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public final class SplineMath
{
	@Configurable(comment = "dt [s] to use for sampling/integrating on splines")
	private static float	splineSamplingDtPrecise	= 0.01f;
	
	
	private SplineMath()
	{
	}
	
	
	/**
	 * for the defense points, LOW PRECISION, BAD PERFORMANCE
	 * 
	 * @param spline
	 * @param drivenWay [mm]
	 * @return
	 */
	public static float timeAfterDrivenWay(final ISpline spline, final float drivenWay)
	{
		return timeAfterDrivenWay(spline, drivenWay, splineSamplingDtPrecise);
	}
	
	
	/**
	 * @param spline
	 * @param drivenWay [mm]
	 * @param dt [s]
	 * @return
	 */
	public static float timeAfterDrivenWay(final ISpline spline, final float drivenWay, final float dt)
	{
		float length = 0;
		float drivenWayMeters = DistanceUnit.MILLIMETERS.toMeters(drivenWay);
		for (float t = 0.0f; t < spline.getTotalTime(); t += dt)
		{
			length += spline.getVelocityByTime(t).getLength2() * dt;
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
	public static float length(final ISpline spline)
	{
		return length(spline, splineSamplingDtPrecise);
	}
	
	
	/**
	 * Calculate the length of the spline by integration
	 * 
	 * @param spline
	 * @param dt smaller->more precision, less performance; e.g. 0.01
	 * @return distance [mm]
	 */
	public static float length(final ISpline spline, final float dt)
	{
		float length = 0;
		for (float t = 0.0f; t < spline.getTotalTime(); t += dt)
		{
			length += spline.getVelocityByTime(t).getLength2() * dt;
		}
		return DistanceUnit.METERS.toMillimeters(length);
	}
	
	
	/**
	 * Find the time where p is nearest to the spline
	 * 
	 * @param spline
	 * @param p
	 * @return
	 */
	public static float timeNearest2Point(final ISpline spline, final IVector2 p)
	{
		return timeNearest2Point(spline, p, 0.1f);
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
	public static float timeNearest2Point(final ISpline spline, final IVector2 p, final float dt)
	{
		float nearestDist = Float.MAX_VALUE;
		float time = 0;
		assert Float.isFinite(spline.getTotalTime());
		for (float t = 0; t < spline.getTotalTime(); t += dt)
		{
			IVector2 p2 = spline.getPositionByTime(t).getXYVector();
			float dist = GeoMath.distancePP(p, p2);
			if (dist < nearestDist)
			{
				nearestDist = dist;
				time = t;
			}
		}
		return time;
	}
}
