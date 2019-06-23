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
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Vector2;


/**
 * @author MarkG<Mark.Geiger@dlr.de>
 */
public class CubicBezierCurve
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	protected static final Logger							log				= Logger.getLogger(CubicBezierCurve.class.getName());
	
	private List<IVector2>									controlPoints	= null;
	private double												endTime			= 1;
	private CubicBezierCurveTrajectoryInformation	trajectory		= null;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param controlPoints list of all BSpline controlPoints
	 */
	private CubicBezierCurve(final List<IVector2> controlPoints)
	{
		this.controlPoints = controlPoints;
	}
	
	
	/**
	 * generates a new CubicBSpline, preStartPoint and afterEndPoint can be null
	 * 
	 * @param preStartPoint can be null
	 * @param startPoint
	 * @param endPoint
	 * @param afterEndPoint can be null
	 * @return a CubicBSpline
	 */
	public static CubicBezierCurve newCubicBSpline(final IVector2 preStartPoint, final IVector2 startPoint,
			final IVector2 endPoint, final IVector2 afterEndPoint)
	{
		List<IVector2> controlPoints = new ArrayList<IVector2>();
		IVector2 helperPoint1 = null;
		IVector2 helperPoint2 = null;
		if (preStartPoint != null)
		{
			IVector2 startToEnd = endPoint.subtractNew(startPoint);
			IVector2 preStartToStart = startPoint.subtractNew(preStartPoint);
			double angle = 0;
			try
			{
				angle = preStartToStart.getAngle() - startToEnd.getAngle();
				angle = AngleMath.PI - angle;
				if ((preStartToStart.getAngle() - startToEnd.getAngle()) > 0)
				{
					angle = (angle * -1);
				}
			} catch (IllegalArgumentException e)
			{
				log.error("Mark soll mich fixen !");
			}
			IVector2 preStartRot = preStartPoint.turnAroundNew(startPoint, angle / 2.0);
			IVector2 rotatedPreStartToStart = startPoint.subtractNew(preStartRot);
			IVector2 dvLin = rotatedPreStartToStart.getNormalVector().normalize();
			if (dvLin.scalarProduct(endPoint.subtractNew(startPoint).normalizeNew()) < 0)
			{
				dvLin = dvLin.multiplyNew(-1);
			}
			double length = startPoint.subtractNew(endPoint).getLength2() / 4.0;
			helperPoint1 = startPoint.addNew(dvLin.multiplyNew(length));
		}
		
		if (afterEndPoint != null)
		{
			IVector2 endToAfterEnd = afterEndPoint.subtractNew(endPoint);
			IVector2 startToEnd = endPoint.subtractNew(startPoint);
			double angle = 0;
			try
			{
				angle = startToEnd.getAngle() - endToAfterEnd.getAngle();
				angle = AngleMath.PI - angle;
				if ((startToEnd.getAngle() - endToAfterEnd.getAngle()) > 0)
				{
					angle = (angle * -1);
				}
			} catch (IllegalArgumentException e)
			{
				log.error("Mark soll mich fixen !");
			}
			IVector2 startRot = startPoint.turnAroundNew(endPoint, angle / 2.0);
			IVector2 rotatedStartToEnd = endPoint.subtractNew(startRot);
			IVector2 dvLin = rotatedStartToEnd.getNormalVector().normalize();
			if (dvLin.scalarProduct(afterEndPoint.subtractNew(endPoint).normalizeNew()) > 0)
			{
				dvLin = dvLin.multiplyNew(-1);
			}
			double length = startPoint.subtractNew(endPoint).getLength2() / 4.0;
			helperPoint2 = endPoint.addNew(dvLin.multiplyNew(length));
		}
		
		if ((helperPoint1 != null) && (helperPoint2 != null))
		{
			
			IVector2 intersectionPoint = GeoMath.intersectionPointPath(startPoint,
					helperPoint1.subtractNew(startPoint), endPoint, helperPoint2.subtractNew(endPoint));
			if (intersectionPoint != null)
			{
				helperPoint1 = intersectionPoint;
				helperPoint2 = intersectionPoint;
			}
		}
		
		controlPoints.add(startPoint);
		if (helperPoint1 != null)
		{
			controlPoints.add(helperPoint1);
		}
		if (helperPoint2 != null)
		{
			controlPoints.add(helperPoint2);
		}
		controlPoints.add(endPoint);
		
		return new CubicBezierCurve(controlPoints);
	}
	
	
	// --------------------------------------------------------l------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * returns a Point on the generatedBSpline.
	 * 
	 * @param t t = 0 -> startPoint / controlPoints[0]
	 *           t = 1 -> endPoint / controlPoint[n]
	 *           else -> InterpolatedPoint
	 * @return
	 */
	public IVector2 getPointOnSpline(final double t)
	{
		double param = t;
		if (trajectory != null)
		{
			param = trajectory.getParameter(t);
		}
		if (controlPoints.size() == 2)
		{
			IVector2 p0 = controlPoints.get(0);
			IVector2 p1 = controlPoints.get(1);
			return p0.multiplyNew(1 - param).addNew(p1.multiplyNew(param));
		} else if (controlPoints.size() == 3)
		{
			IVector2 p0 = controlPoints.get(0);
			IVector2 p1 = controlPoints.get(1);
			IVector2 p2 = controlPoints.get(2);
			
			IVector2 firstSummand = p0.multiplyNew((1 - param) * (1 - param));
			IVector2 secondSummand = p1.multiplyNew(2 * (1 - param) * param);
			IVector2 thirdSummand = p2.multiplyNew(param * param);
			return firstSummand.addNew(secondSummand).addNew(thirdSummand);
		} else if (controlPoints.size() == 4)
		{
			IVector2 p0 = controlPoints.get(0);
			IVector2 p1 = controlPoints.get(1);
			IVector2 p2 = controlPoints.get(2);
			IVector2 p3 = controlPoints.get(3);
			
			IVector2 firstSummand = p0.multiplyNew((1 - param) * (1 - param) * (1 - param));
			IVector2 secondSummand = p1.multiplyNew(3 * param * ((1 - param) * (1 - param)));
			IVector2 thirdSummand = p2.multiplyNew(3 * param * param * (1 - param));
			IVector2 fourthSummand = p3.multiplyNew(param * param * param);
			return firstSummand.addNew(secondSummand).addNew(thirdSummand).addNew(fourthSummand);
		}
		log.error("A CubicBSpline with <" + controlPoints.size()
				+ "> ControlPoints cant exist. Allowed Number of elements: 2 - 4. Please check your usage of CubicBSplines");
		return null;
	}
	
	
	/**
	 * returns the first Derivative on the generatedBSpline.
	 * 
	 * @param t t = 0 -> startPoint / controlPoints[0]
	 *           t = 1 -> endPoint / controlPoint[n]
	 *           else -> InterpolatedPoint
	 * @return
	 */
	public IVector2 getFirstDerivativeOnSpline(final double t)
	{
		double param = t;
		if (trajectory != null)
		{
			param = trajectory.getParameter(t);
		}
		if (controlPoints.size() == 2)
		{
			IVector2 p0 = controlPoints.get(0);
			IVector2 p1 = controlPoints.get(1);
			IVector2 firstDeriv = p1.subtractNew(p0).multiplyNew(1.0 / endTime).multiplyNew(1.0 / endTime);
			return firstDeriv;
		}
		else if (controlPoints.size() == 3)
		{
			IVector2 p0 = controlPoints.get(0);
			IVector2 p1 = controlPoints.get(1);
			IVector2 p2 = controlPoints.get(2);
			
			IVector2 firstSummand = p1.subtractNew(p0).multiplyNew(2 * (1 - param));
			IVector2 secondSummand = p2.subtractNew(p1).multiplyNew(2 * param);
			return firstSummand.addNew(secondSummand).multiplyNew(1.0 / endTime);
		} else if (controlPoints.size() == 4)
		{
			IVector2 p0 = controlPoints.get(0);
			IVector2 p1 = controlPoints.get(1);
			IVector2 p2 = controlPoints.get(2);
			IVector2 p3 = controlPoints.get(3);
			
			IVector2 firstSummand = p1.subtractNew(p0).multiplyNew(3 * (1 - param) * (1 - param));
			IVector2 secondSummand = p2.subtractNew(p1).multiplyNew(6 * param * (1 - param));
			IVector2 thirdSummand = p3.subtractNew(p2).multiplyNew(3 * param * param);
			return firstSummand.addNew(secondSummand).addNew(thirdSummand).multiplyNew(1.0 / endTime);
		}
		log.error("A CubicBSpline with <" + controlPoints.size()
				+ "> ControlPoints cant exist. Allowed Number of elements: 2 - 4. Please check your usage of CubicBSplines");
		return null;
	}
	
	
	/**
	 * returns the first Derivative on the generatedBSpline.
	 * 
	 * @param t t = 0 -> startPoint / controlPoints[0]
	 *           t = 1 -> endPoint / controlPoint[n]
	 *           else -> InterpolatedPoint
	 * @return
	 */
	public IVector2 getSecondDerivativeOnSpline(final double t)
	{
		double param = t;
		if (trajectory != null)
		{
			param = trajectory.getParameter(t);
		}
		if (controlPoints.size() == 2)
		{
			return new Vector2(0, 0);
		}
		else if (controlPoints.size() == 3)
		{
			IVector2 p0 = controlPoints.get(0);
			IVector2 p1 = controlPoints.get(1);
			IVector2 p2 = controlPoints.get(2);
			
			IVector2 firstSummand = p1.subtractNew(p0).multiplyNew(2 * (1 - param));
			IVector2 secondSummand = p2.subtractNew(p1).multiplyNew(2 * param);
			return firstSummand.addNew(secondSummand).multiplyNew(1.0 / endTime);
		} else if (controlPoints.size() == 4)
		{
			IVector2 p0 = controlPoints.get(0);
			IVector2 p1 = controlPoints.get(1);
			IVector2 p2 = controlPoints.get(2);
			IVector2 p3 = controlPoints.get(3);
			
			IVector2 firstSummand = p2.subtractNew(p1.multiplyNew(2)).addNew(p0).multiplyNew(6 * (1 - param));
			IVector2 secondSummand = p3.subtractNew(p2.multiplyNew(2)).addNew(p1).multiplyNew(6 * param);
			return firstSummand.addNew(secondSummand).multiplyNew(1.0 / endTime);
		}
		log.error("A CubicBSpline with <" + controlPoints.size()
				+ "> ControlPoints cant exist. Allowed Number of elements: 2 - 4. Please check your usage of CubicBSplines");
		return null;
	}
	
	
	/**
	 * @return
	 */
	public double getMaxFirstDerivativeOnSpline()
	{
		IVector2 maxFD = new Vector2(0, 0);
		double max = 0;
		for (double i = 0.f; i < endTime; i = i + 0.01)
		{
			if (i > endTime)
			{
				i = endTime;
			}
			
			IVector2 tmp = getFirstDerivativeOnSpline(i);
			if (tmp.getLength2() > max)
			{
				max = tmp.getLength2();
				maxFD = getFirstDerivativeOnSpline(i);
			}
		}
		return maxFD.getLength2();
	}
	
	
	/**
	 * @return
	 */
	public double getMaxSecondDerivativeOnSpline()
	{
		IVector2 maxSD = new Vector2(0, 0);
		double max = 0;
		for (double i = 0.f; i < endTime; i = i + 0.01)
		{
			if (i > endTime)
			{
				i = endTime;
			}
			
			IVector2 tmp = getSecondDerivativeOnSpline(i);
			if (tmp.getLength2() > max)
			{
				max = tmp.getLength2();
				maxSD = getSecondDerivativeOnSpline(i);
			}
		}
		return maxSD.getLength2();
	}
	
	
	/**
	 * @param starttime from
	 * @param endtime to
	 * @return
	 */
	public double getCurveLength(final double starttime, final double endtime)
	{
		double distance = 0;
		double parameter = starttime;
		double stepsize = 0.015625; // (1.0 / 2^6)
		while (parameter < endtime)
		{
			double oldParameter = parameter;
			parameter = parameter + stepsize;
			distance = distance + GeoMath.distancePP(getPointOnSpline(oldParameter), getPointOnSpline(parameter));
		}
		return distance;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @return
	 */
	public List<IVector2> getCalculatedControlPoints()
	{
		return controlPoints;
	}
	
	
	/**
	 * @return the endTime
	 */
	public double getEndTime()
	{
		return endTime;
	}
	
	
	/**
	 * @param endTime the endTime to set
	 */
	public void setEndTime(final double endTime)
	{
		this.endTime = endTime;
	}
	
	
	/**
	 * 
	 */
	public void generateTrajectory()
	{
		// Split Spline into several subTrajectories.
		CubicBezierCurveTrajectoryInformation trajectory = new CubicBezierCurveTrajectoryInformation();
		trajectory.getRealEndTimes().add(0.0);
		trajectory.getParameterEndTimes().add(0.0);
		double maxSteps = 10;
		double timeSum = 0;
		for (int i = 0; i < maxSteps; i++)
		{
			double currentStartParam = i / maxSteps;
			double currentEndParam = (i + 1) / maxSteps;
			
			double distanceMM = getCurveLength(currentStartParam, currentEndParam);
			double speedMPerS = trajectory.getMaxVelocity();
			double minNeededTimeS = distanceMM / 1000.0 / speedMPerS;
			double angle = AngleMath.getShortestRotation(getFirstDerivativeOnSpline(currentStartParam).getAngle(),
					getFirstDerivativeOnSpline(currentEndParam).getAngle());
			double rotationPerSecond = angle / minNeededTimeS;
			
			while (rotationPerSecond > trajectory.getMaxRotation())
			{
				rotationPerSecond = angle / minNeededTimeS;
				minNeededTimeS = minNeededTimeS + 0.015625; // add ca. 15 ms to time each Step.
			}
			// set Trajectory Params
			timeSum = timeSum + minNeededTimeS;
			trajectory.getRealEndTimes().add(timeSum);
			trajectory.getParameterEndTimes().add(currentEndParam);
		}
		this.trajectory = trajectory;
		endTime = trajectory.getTotalTime();
	}
}
