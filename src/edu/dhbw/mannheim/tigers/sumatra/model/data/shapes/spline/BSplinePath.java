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

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory.ITrajectory2D;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector3;
import edu.dhbw.mannheim.tigers.sumatra.util.clock.SumatraClock;


/**
 * @author MarkG<Mark.Geiger@dlr.de>
 */
public class BSplinePath implements ISpline, ITrajectory2D
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	protected static final Logger		log			= Logger.getLogger(BSplinePath.class.getName());
	
	// a complex BSpline path consinst of several cubic bezier curves.
	private List<CubicBezierCurve>	path			= new ArrayList<CubicBezierCurve>();
	private float							endTime		= 1f;
	private float							startTime	= 0;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * generates a simple BSpline fitting all controlPoints.
	 * 
	 * @param controlPoints
	 * @return
	 */
	public static BSplinePath newBSplinePath(final List<IVector2> controlPoints)
	{
		return new BSplinePath(controlPoints, AVector2.ZERO_VECTOR, AVector2.ZERO_VECTOR, false);
	}
	
	
	/**
	 * @param controlPoints list of all BSpline controlPoints
	 * @param initVel
	 * @param finalVel
	 * @param generateTrajectory
	 */
	public BSplinePath(final List<IVector2> controlPoints, final IVector2 initVel, final IVector2 finalVel,
			final boolean generateTrajectory)
	{
		startTime = SumatraClock.nanoTime();
		for (int i = 0; i < (controlPoints.size() - 1); i++)
		{
			IVector2 preStart = null;
			if (i > 0)
			{
				preStart = controlPoints.get(i - 1);
			} else if (generateTrajectory)
			{
				if (!initVel.isZeroVector())
				{
					preStart = controlPoints.get(0).addNew(initVel.multiplyNew(-1000));
				}
			}
			
			IVector2 start = controlPoints.get(i);
			IVector2 end = controlPoints.get(i + 1);
			
			IVector2 afterEnd = null;
			if (i < (controlPoints.size() - 1 - 1))
			{
				afterEnd = controlPoints.get(i + 2);
			} else if (generateTrajectory)
			{
				if (!finalVel.isZeroVector())
				{
					afterEnd = controlPoints.get(controlPoints.size() - 1).addNew(finalVel.multiplyNew(1));
				}
			}
			
			CubicBezierCurve spline;
			if (generateTrajectory)
			{
				spline = CubicBezierCurve.newCubicBSpline(preStart, start, end, afterEnd);
				for (CubicBezierCurve curve : generateSubSplines(spline, preStart, afterEnd))
				{
					spline = generateCurveTrajectory(curve);
					path.add(spline);
				}
			} else
			{
				spline = CubicBezierCurve.newCubicBSpline(preStart, start, end, afterEnd);
				path.add(spline);
			}
		}
		
		float time = 0;
		for (CubicBezierCurve curve : path)
		{
			time = time + curve.getEndTime();
		}
		endTime = time;
	}
	
	
	// --------------------------------------------------------l------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * returns a Point on the generatedBSpline.
	 * 
	 * @param t t = 0 -> startPoint
	 *           t = endTime -> endPoint
	 *           else -> InterpolatedPoint
	 * @return
	 */
	public IVector2 getPointOnSpline(final float t)
	{
		float param = t;
		if (param > endTime)
		{
			param = endTime;
		}
		for (int i = 0; i < path.size(); i++)
		{
			float currentEndTime = path.get(i).getEndTime();
			if ((param <= currentEndTime) || (i == (path.size() - 1)))
			{
				return path.get(i).getPointOnSpline(param);
			}
			param = param - currentEndTime;
		}
		log.error("invalid BSplinePath parameter, endTime is: " + endTime + " -- your param is: " + param + " cpSize: "
				+ path.size());
		return null;
	}
	
	
	/**
	 * @param t
	 * @return
	 */
	public IVector2 getFirstDerivativeOnSpline(final float t)
	{
		float param = t;
		if (param > endTime)
		{
			param = endTime;
		}
		for (int i = 0; i < path.size(); i++)
		{
			float currentEndTime = path.get(i).getEndTime();
			if ((param <= currentEndTime) || (i == (path.size() - 1)))
			{
				return path.get(i).getFirstDerivativeOnSpline(param);
			}
			param = param - currentEndTime;
		}
		log.error("invalid BSplinePath parameter, endTime is: " + endTime + " -- your param is: " + param);
		return null;
	}
	
	
	/**
	 * @param t
	 * @return
	 */
	public IVector2 getSecondDerivativeOnSpline(final float t)
	{
		float param = t;
		if (param > endTime)
		{
			param = endTime;
		}
		for (int i = 0; i < path.size(); i++)
		{
			float currentEndTime = path.get(i).getEndTime();
			if ((param <= currentEndTime) || (i == (path.size() - 1)))
			{
				return path.get(i).getSecondDerivativeOnSpline(param);
			}
			param = param - currentEndTime;
		}
		log.error("invalid BSplinePath parameter, endTime is: " + endTime + " -- your param is: " + param);
		return null;
	}
	
	
	/**
	 * the position on the field after a given time
	 * 
	 * @param t [s] the time from the beginning
	 * @return [mm,mm,rad]
	 */
	@Override
	public IVector3 getPositionByTime(final float t)
	{
		return new Vector3(getPointOnSpline(t), 0);
	}
	
	
	/**
	 * The velocity, basically the derivative of the position
	 * 
	 * @param t [s]
	 * @return [m/s,m/s,rad/s]
	 */
	@Override
	public IVector3 getVelocityByTime(final float t)
	{
		return new Vector3(getFirstDerivativeOnSpline(t), 0);
	}
	
	
	/**
	 * @param t
	 * @return
	 */
	@Override
	public IVector3 getAccelerationByTime(final float t)
	{
		return new Vector3(getSecondDerivativeOnSpline(t), 0);
	}
	
	
	/**
	 * total time in s
	 * 
	 * @return [s]
	 */
	@Override
	public float getTotalTime()
	{
		return endTime;
	}
	
	
	/**
	 * @return
	 */
	@Override
	public float getCurrentTime()
	{
		return ((SumatraClock.nanoTime() - startTime) / (1e9f));
	}
	
	
	@Override
	public Vector2 getPosition(final float t)
	{
		return (Vector2) getPointOnSpline(t);
	}
	
	
	@Override
	public Vector2 getVelocity(final float t)
	{
		return (Vector2) getFirstDerivativeOnSpline(t);
	}
	
	
	@Override
	public Vector2 getAcceleration(final float t)
	{
		return (Vector2) getSecondDerivativeOnSpline(t);
	}
	
	
	private CubicBezierCurve generateCurveTrajectory(final CubicBezierCurve curveIn)
	{
		CubicBezierCurve curveOut = curveIn;
		curveOut.generateTrajectory();
		return curveOut;
	}
	
	
	private List<CubicBezierCurve> generateSubSplines(final CubicBezierCurve curve, final IVector2 preCurvePosition,
			final IVector2 afterCurvePosition)
	{
		List<CubicBezierCurve> list = new ArrayList<CubicBezierCurve>();
		if (curve.getCurveLength(0f, curve.getEndTime()) < Float.MAX_VALUE)
		{
			list.add(curve);
			return list;
		}
		// function currently disabled
		return list;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @return
	 */
	public List<IVector2> getAllCalculatedControlPoints()
	{
		List<IVector2> tmpList = new ArrayList<IVector2>();
		for (CubicBezierCurve spline : path)
		{
			for (IVector2 value : spline.getCalculatedControlPoints())
			{
				tmpList.add(value);
			}
		}
		return tmpList;
	}
	
	
	/**
	 * @return
	 */
	public float getSplineLength()
	{
		float distance = 0;
		for (CubicBezierCurve curve : path)
		{
			distance = distance + curve.getCurveLength(0f, curve.getEndTime());
		}
		return distance;
	}
	
}
