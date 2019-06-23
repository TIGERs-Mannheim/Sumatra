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

import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Line;
import edu.tigers.sumatra.math.ValuePoint;
import edu.tigers.sumatra.math.Vector2;


/**
 * @author MarkG<Mark.Geiger@dlr.de>
 */
public class BSpline
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final Logger	log							= Logger.getLogger(BSpline.class.getName());
																			
	private List<IVector2>			controlPoints				= null;
	private List<ValuePoint>		weigtheedControlPoints	= null;
	private double						parameter					= 0;
	private final int					maxIterations				= 100;
																			
																			
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param controlPoints list of all BSpline controlPoints
	 */
	private BSpline(final List<IVector2> controlPoints)
	{
		this.controlPoints = controlPoints;
	}
	
	
	/**
	 * @param controlPoints list of all BSpline controlPoints
	 */
	private BSpline(final List<IVector2> controlPoints, final List<Double> weights)
	{
		weigtheedControlPoints = new ArrayList<ValuePoint>();
		this.controlPoints = new ArrayList<IVector2>();
		for (int i = 0; i < weights.size(); i++)
		{
			weigtheedControlPoints.add(new ValuePoint(controlPoints.get(i), weights.get(i)));
			this.controlPoints.add(controlPoints.get(i));
		}
	}
	
	
	/**
	 * generates a new BSpline
	 * 
	 * @param controlPoints
	 * @return
	 */
	public static BSpline newBSpline(final List<IVector2> controlPoints)
	{
		return new BSpline(controlPoints);
	}
	
	
	/**
	 * generates a new weighted BSpline
	 * 
	 * @param controlPoints
	 * @return
	 */
	public static BSpline newBSplineWeighted(final List<ValuePoint> controlPoints)
	{
		List<Double> weights = new ArrayList<Double>();
		List<IVector2> points = new ArrayList<IVector2>();
		for (int i = 0; i < controlPoints.size(); i++)
		{
			points.add(controlPoints.get(i));
			weights.add(controlPoints.get(i).value);
		}
		return new BSpline(points, weights);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	private List<IVector2> calculatePoint(final List<IVector2> reducedControlPoints)
	{
		// classic Spline
		if (reducedControlPoints.size() <= 1)
		{
			return reducedControlPoints;
		}
		List<IVector2> newControlPoints = new ArrayList<IVector2>();
		for (int i = 0; i < (reducedControlPoints.size() - 1); i++)
		{
			IVector2 interpolatedControlPoint = reducedControlPoints.get(i).addNew(
					reducedControlPoints.get(i + 1).subtractNew(reducedControlPoints.get(i)).multiplyNew(parameter));
			newControlPoints.add(interpolatedControlPoint);
		}
		return calculatePoint(newControlPoints);
		
	}
	
	
	/**
	 * 
	 */
	public void invertSpline()
	{
		List<IVector2> newList = new ArrayList<IVector2>();
		for (int i = 0; i < controlPoints.size(); i++)
		{
			newList.add(controlPoints.get(controlPoints.size() - 1 - i));
		}
		controlPoints = newList;
	}
	
	
	private List<IVector2> calculatePointWeighted(final List<IVector2> reducedControlPoints)
	{
		// weigthed Spline
		if (reducedControlPoints.size() <= 1)
		{
			return reducedControlPoints;
		}
		List<IVector2> newControlPoints = new ArrayList<IVector2>();
		for (int i = 0; i < (reducedControlPoints.size() - 1); i++)
		{
			if (reducedControlPoints.size() == weigtheedControlPoints.size())
			{
				List<IVector2> tmp = new ArrayList<IVector2>();
				for (int j = 0; j < (reducedControlPoints.size()); j++)
				{
					IVector2 newCP = null;
					IVector2 leadPoint = getLeadPointOnUnweigthedSpline(reducedControlPoints.get(j));
					IVector2 leadToOldCP = reducedControlPoints.get(j).subtractNew(leadPoint);
					newCP = reducedControlPoints.get(j).addNew(leadToOldCP.multiplyNew(weigtheedControlPoints.get(j).value));
					tmp.add(newCP);
				}
				newControlPoints = tmp;
				weigtheedControlPoints.add(new ValuePoint(new Vector2(0, 0)));
				controlPoints = tmp;
				return calculatePointWeighted(newControlPoints);
			}
			IVector2 interpolatedControlPoint = reducedControlPoints.get(i).addNew(
					reducedControlPoints.get(i + 1).subtractNew(reducedControlPoints.get(i)).multiplyNew(parameter));
			newControlPoints.add(interpolatedControlPoint);
		}
		return calculatePointWeighted(newControlPoints);
	}
	
	
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
		parameter = t;
		if (weigtheedControlPoints != null)
		{
			return calculatePointWeighted(controlPoints).get(0);
		}
		return calculatePoint(controlPoints).get(0);
	}
	
	
	/**
	 * returns intersection parameters
	 * 
	 * @param line
	 * @return
	 */
	public List<Double> intersect(final Line line)
	{
		return null;
	}
	
	
	/**
	 * approximation of Splinelength from start to end.
	 * 
	 * @param start
	 * @param end
	 * @return
	 */
	public double getSplineLength(final double start, final double end)
	{
		double parameter = start;
		double distance = 0;
		int i = 0;
		while (true)
		{
			double oldParameter = parameter;
			parameter = parameter + 0.030;
			if (parameter > end)
			{
				break;
			}
			distance = distance + GeoMath.distancePP(getPointOnSpline(oldParameter), getPointOnSpline(parameter));
			if (i > maxIterations)
			{
				log.warn("getSplineLenght: maxIterations reached, this should not happen");
				break;
			}
			i = i + 1;
		}
		return distance;
	}
	
	
	/**
	 * returns closest PointParam on the Spline
	 * 
	 * @param point
	 * @return
	 */
	public double getLeadParamOnSpline(final IVector2 point)
	{
		double stepsize = 0.03;
		double param = 0;
		double minDist = Double.MAX_VALUE;
		double minParam = 0;
		int i = 0;
		while (param < 1)
		{
			double dist = GeoMath.distancePP(point, getPointOnSpline(param));
			if (dist < minDist)
			{
				minDist = dist;
				minParam = param;
			}
			param = param + stepsize;
			if (i > maxIterations)
			{
				log.warn("getLeadParamOnSpline: maxIterations reached, this should not happen");
				break;
			}
			i = i + 1;
		}
		double startParam = param - stepsize;
		double endParam = param + stepsize;
		param = startParam;
		stepsize = stepsize / 10.0;
		i = 0;
		while (param < endParam)
		{
			double dist = GeoMath.distancePP(point, getPointOnSpline(param));
			if (dist < minDist)
			{
				minDist = dist;
				minParam = param;
			}
			param = param + stepsize;
			if (i > maxIterations)
			{
				log.warn("getLeadParamOnSpline: maxIterations reached, this should not happen");
				break;
			}
			i = i + 1;
		}
		
		return minParam;
	}
	
	
	/**
	 * calculates o Parameter which is {distance} mm away from
	 * the start point (on the BSpline).
	 * 
	 * @param start
	 * @param distance
	 * @return
	 */
	public double getParamForDistantPoint(final double start, final double distance)
	{
		if (distance < 0.1)
		{
			return 0;
		}
		double maxLength = getSplineLength(0f, 1);
		double stepsize = distance / maxLength;
		double dist = getSplineLength(start, start + stepsize);
		
		int i = 0;
		while (Math.abs(dist - distance) > 100)
		{
			dist = getSplineLength(start, start + stepsize);
			if (dist > distance)
			{
				stepsize = stepsize - 0.015;
			} else
			{
				stepsize = stepsize + 0.015;
			}
			if (i > maxIterations)
			{
				log.warn("getParamForDistantPoint: maxIterations reached, this should not happen");
				break;
			}
			i = i + 1;
		}
		return start + stepsize;
	}
	
	
	/**
	 * @param t
	 * @param isDirectionPositive
	 * @return
	 */
	private double getParamDifficulty(final double t, final boolean isDirectionPositive, final double maxDistance)
	{
		double distanceF = maxDistance;
		double stepsize = 0.15;
		double factor = 3;
		if (!isDirectionPositive)
		{
			stepsize = stepsize * -1;
		}
		int i = 1;
		double distF = 0;
		double value = 1;
		
		int j = 0;
		while ((distF < distanceF))
		{
			IVector2 point1 = getTangentOnPoint(t + (i * stepsize)).normalizeNew();
			IVector2 point2 = getTangentOnPoint(t + ((i + 1) * stepsize)).normalizeNew();
			double scalarF = Math.pow(point1.scalarProduct(point2), factor);
			if (isDirectionPositive)
			{
				distF = getSplineLength(t, t + (i * stepsize));
			} else
			{
				distF = getSplineLength(t + (i * stepsize), t);
			}
			value = value * scalarF;
			
			if (j > maxIterations)
			{
				log.warn("getParamDifficulty: maxIterations reached, this should not happen");
				break;
			}
			j = j + 1;
			i++;
		}
		if (value > 0.985)
		{
			value = 1;
		} else if (value < 0.015)
		{
			value = 0;
		}
		return value;
	}
	
	
	/**
	 * @param t
	 * @return
	 */
	public double getPointDifficulty(final double t)
	{
		return getParamDifficulty(t, true, 180) * getParamDifficulty(t, false, 20);
	}
	
	
	/**
	 * @param start
	 * @param tolerance good tolerance values are around 0.01 - 0.1
	 * @return
	 */
	public double getMostDistantPointOnStraightLine(final double start, final double tolerance)
	{
		double step = 0.02;
		double scalar = 1;
		while (scalar > (1 - tolerance))
		{
			scalar = Math.abs(getTangentOnPoint(start).normalizeNew().scalarProduct(
					getTangentOnPoint(start + step).normalizeNew()));
			step = step + 0.02;
			if ((start + step) > 1)
			{
				return 1;
			}
		}
		return start + step;
	}
	
	
	/**
	 * @param t
	 * @return
	 */
	public IVector2 getTangentOnPoint(final double t)
	{
		double delta = 0.01;
		IVector2 point1 = getPointOnSpline(t - delta);
		IVector2 point2 = getPointOnSpline(t + delta);
		return point2.subtractNew(point1).normalizeNew();
	}
	
	
	private IVector2 getLeadPointOnUnweigthedSpline(final IVector2 point)
	{
		double stepsize = 0.015;
		double param = 0;
		double minDist = Double.MAX_VALUE;
		double minParam = 0;
		while (param < 1)
		{
			parameter = param;
			double dist = GeoMath.distancePP(point, calculatePoint(controlPoints).get(0));
			if (dist < minDist)
			{
				minDist = dist;
				minParam = param;
			}
			param = param + stepsize;
		}
		double startParam = param - stepsize;
		double endParam = param + stepsize;
		param = startParam;
		stepsize = stepsize / 10.0;
		while (param < endParam)
		{
			parameter = param;
			double dist = GeoMath.distancePP(point, calculatePoint(controlPoints).get(0));
			if (dist < minDist)
			{
				minDist = dist;
				minParam = param;
			}
			param = param + stepsize;
		}
		parameter = minParam;
		return calculatePoint(controlPoints).get(0);
	}
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
