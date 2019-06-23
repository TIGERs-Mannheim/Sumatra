/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 22.10.2014
 * Author(s): MarkG
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.spline;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


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
	
	private List<Float>				parameterEndTimes	= new ArrayList<Float>();
	private List<Float>				realEndTimes		= new ArrayList<Float>();
	
	@Configurable(comment = "max speed in [m/s]")
	private static float				maxVelocity			= 2.0f;
	
	@Configurable(comment = "max rotation in [angle/second] [rad/s]")
	private static float				maxRotation			= AngleMath.PI_HALF;
	
	
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
	public float getTotalTime()
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
	public float getParameter(final float time)
	{
		if (time > realEndTimes.get(realEndTimes.size() - 1))
		{
			return 1f;
		}
		for (int i = 0; i < (realEndTimes.size() - 1); i++)
		{
			float lowerBound = realEndTimes.get(i);
			float upperBound = realEndTimes.get(i + 1);
			
			if ((time > lowerBound) && (time <= upperBound))
			{
				float distBounds = upperBound - lowerBound;
				float progress = (time - lowerBound) / distBounds;
				float param = parameterEndTimes.get(i) +
						((parameterEndTimes.get(i + 1) - parameterEndTimes.get(i)) * progress);
				return param;
			} else if (Math.abs(time - lowerBound) < 0.001f)
			{
				return parameterEndTimes.get(i);
			}
		}
		log.error("given value out of bounds! CubicBezierTrajectory - param: " + time + " bounds: " + realEndTimes.get(0)
				+ " " + realEndTimes.get(realEndTimes.size() - 1));
		return 1f;
	}
	
	
	/**
	 * @return
	 */
	public float getMaxVelocity()
	{
		return maxVelocity;
	}
	
	
	/**
	 * @return
	 */
	public float getMaxRotation()
	{
		return maxRotation;
	}
	
	
	/**
	 * @return the parameterEndTimes
	 */
	public List<Float> getParameterEndTimes()
	{
		return parameterEndTimes;
	}
	
	
	/**
	 * @return the realEndTimes
	 */
	public List<Float> getRealEndTimes()
	{
		return realEndTimes;
	}
}
