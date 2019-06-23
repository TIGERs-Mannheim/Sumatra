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

import edu.tigers.sumatra.math.AVector2;
import edu.tigers.sumatra.math.IVector2;


/**
 * @author MarkG<Mark.Geiger@dlr.de>
 */
public class BSplinePath implements ITrajectory<IVector2>
{
	protected static final Logger				log		= Logger.getLogger(BSplinePath.class.getName());
																	
	// a complex BSpline path consinst of several cubic bezier curves.
	private final List<CubicBezierCurve>	path		= new ArrayList<CubicBezierCurve>();
	private double									endTime	= 1;
																	
																	
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
		
		double time = 0;
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
	public IVector2 getPointOnSpline(final double t)
	{
		double param = t;
		if (param > endTime)
		{
			param = endTime;
		}
		for (int i = 0; i < path.size(); i++)
		{
			double currentEndTime = path.get(i).getEndTime();
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
	public IVector2 getFirstDerivativeOnSpline(final double t)
	{
		double param = t;
		if (param > endTime)
		{
			param = endTime;
		}
		for (int i = 0; i < path.size(); i++)
		{
			double currentEndTime = path.get(i).getEndTime();
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
	public IVector2 getSecondDerivativeOnSpline(final double t)
	{
		double param = t;
		if (param > endTime)
		{
			param = endTime;
		}
		for (int i = 0; i < path.size(); i++)
		{
			double currentEndTime = path.get(i).getEndTime();
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
	 * total time in s
	 * 
	 * @return [s]
	 */
	@Override
	public double getTotalTime()
	{
		return endTime;
	}
	
	
	@Override
	public IVector2 getPositionMM(final double t)
	{
		return getPointOnSpline(t);
	}
	
	
	@Override
	public IVector2 getPosition(final double t)
	{
		return getPositionMM(t).multiplyNew(1e-3);
	}
	
	
	@Override
	public IVector2 getVelocity(final double t)
	{
		return getFirstDerivativeOnSpline(t);
	}
	
	
	@Override
	public IVector2 getAcceleration(final double t)
	{
		return getSecondDerivativeOnSpline(t);
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
		if (curve.getCurveLength(0f, curve.getEndTime()) < Double.MAX_VALUE)
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
	public double getSplineLength()
	{
		double distance = 0;
		for (CubicBezierCurve curve : path)
		{
			distance = distance + curve.getCurveLength(0f, curve.getEndTime());
		}
		return distance;
	}
	
}
