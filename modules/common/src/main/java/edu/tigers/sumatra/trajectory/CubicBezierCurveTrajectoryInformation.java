/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 22.10.2014
 * Author(s): MarkG
 * *********************************************************
 */
package edu.tigers.sumatra.trajectory;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.math.AngleMath;


/**
 * @author MarkG<Mark.Geiger@dlr.de>
 */
public class CubicBezierCurveTrajectoryInformation
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	protected static final Logger	log					= Logger.getLogger(CubicBezierCurveTrajectoryInformation.class
																			.getName());
																			
	private final List<Double>		parameterEndTimes	= new ArrayList<Double>();
	private final List<Double>		realEndTimes		= new ArrayList<Double>();
																	
	private static double			maxVelocity			= 2.0;
																	
	private static double			maxRotation			= AngleMath.PI_HALF;
																	
																	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 */
	public CubicBezierCurveTrajectoryInformation()
	{
	}
	
	
	/**
	 * @return
	 */
	public double getTotalTime()
	{
		return realEndTimes.get(realEndTimes.size() - 1);
	}
	
	
	/**
	 * This method maps time parameters into BezierCurve Parameters
	 * using linear interpolation.
	 * 
	 * @param time
	 * @return
	 */
	public double getParameter(final double time)
	{
		if (time > realEndTimes.get(realEndTimes.size() - 1))
		{
			return 1;
		}
		for (int i = 0; i < (realEndTimes.size() - 1); i++)
		{
			double lowerBound = realEndTimes.get(i);
			double upperBound = realEndTimes.get(i + 1);
			
			if ((time > lowerBound) && (time <= upperBound))
			{
				double distBounds = upperBound - lowerBound;
				double progress = (time - lowerBound) / distBounds;
				double param = parameterEndTimes.get(i) +
						((parameterEndTimes.get(i + 1) - parameterEndTimes.get(i)) * progress);
				return param;
			} else if (Math.abs(time - lowerBound) < 0.001)
			{
				return parameterEndTimes.get(i);
			}
		}
		log.error("given value out of bounds! CubicBezierTrajectory - param: " + time + " bounds: " + realEndTimes.get(0)
				+ " " + realEndTimes.get(realEndTimes.size() - 1));
		return 1;
	}
	
	
	/**
	 * @return
	 */
	public double getMaxVelocity()
	{
		return maxVelocity;
	}
	
	
	/**
	 * @return
	 */
	public double getMaxRotation()
	{
		return maxRotation;
	}
	
	
	/**
	 * @return the parameterEndTimes
	 */
	public List<Double> getParameterEndTimes()
	{
		return parameterEndTimes;
	}
	
	
	/**
	 * @return the realEndTimes
	 */
	public List<Double> getRealEndTimes()
	{
		return realEndTimes;
	}
}
