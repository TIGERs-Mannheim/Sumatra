/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 17, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.shapes.ellipse;

import java.util.ArrayList;
import java.util.List;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.ILine;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Vector2;


/**
 * Default implementation of an ellipse
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent
public class Ellipse implements IEllipse
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final double	MAX_STEP_CURVE			= 5;
	private static final double	POINT_ON_CURVE_TOL	= 5;
	private static final double	STEP_TOLERANCE			= 0.01;
	private IVector2					center;
	private double						radiusX;
	private double						radiusY;
	private double						turnAngle;
											
											
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@SuppressWarnings("unused")
	protected Ellipse()
	{
	
	}
	
	
	/**
	 * @param center
	 * @param radiusX
	 * @param radiusY
	 * @param turnAngle
	 */
	public Ellipse(final IVector2 center, final double radiusX, final double radiusY, final double turnAngle)
	{
		if ((radiusX <= 0) || (radiusY <= 0))
		{
			throw new IllegalArgumentException("radius may not be equal or smaller than zero");
		}
		if (center == null)
		{
			throw new IllegalAccessError("center may not be null");
		}
		this.center = center;
		this.radiusX = radiusX;
		this.radiusY = radiusY;
		this.turnAngle = AngleMath.normalizeAngle(turnAngle);
	}
	
	
	/**
	 * @param center
	 * @param radiusX
	 * @param radiusY
	 */
	public Ellipse(final IVector2 center, final double radiusX, final double radiusY)
	{
		this(center, radiusX, radiusY, 0);
	}
	
	
	/**
	 * Copy constructor
	 * 
	 * @param ellipse
	 */
	public Ellipse(final IEllipse ellipse)
	{
		this(ellipse.getCenter(), ellipse.getRadiusX(), ellipse.getRadiusY(), ellipse.getTurnAngle());
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public IVector2 getApex(final EApexType apexType)
	{
		final IVector2 res;
		final double bigRad = getDiameterMax();
		
		switch (apexType)
		{
			case MAIN_POS:
				res = center.addNew(getFocusFromCenter().scaleToNew(Math.max(radiusX, radiusY)));
				break;
			case MAIN_NEG:
				res = center.addNew(getFocusFromCenter().scaleToNew(-Math.max(radiusX, radiusY)));
				break;
			case SEC_POS:
				res = center.addNew(getFocusFromCenter().turnNew(Math.PI / 2.0)
						.scaleToNew(Math.min(radiusX, radiusY)));
				break;
			case SEC_NEG:
				res = center.addNew(getFocusFromCenter().turnNew(Math.PI / 2.0).scaleToNew(
						-Math.min(radiusX, radiusY)));
				break;
			case CENTER_NORTH:
				res = getIntersectingPoints(center, center.addNew(new Vector2(0, bigRad))).get(0);
				break;
			case CENTER_EAST:
				res = getIntersectingPoints(center, center.addNew(new Vector2(bigRad, 0))).get(0);
				break;
			case CENTER_SOUTH:
				res = getIntersectingPoints(center, center.addNew(new Vector2(0, -bigRad))).get(0);
				break;
			case CENTER_WEST:
				res = getIntersectingPoints(center, center.addNew(new Vector2(-bigRad, 0))).get(0);
				break;
			case EASTERNMOST:
			case NORTHERNMOST:
			case SOUTHERNMOST:
			case WESTERNMOST:
			default:
				throw new IllegalStateException("Unhandled apexType");
		}
		
		return res;
		
	}
	
	
	@Override
	public IVector2 getFocusPositive()
	{
		return center.addNew(getFocusFromCenter());
	}
	
	
	@Override
	public IVector2 getFocusNegative()
	{
		return center.addNew(getFocusFromCenter().multiplyNew(-1));
	}
	
	
	@Override
	public IVector2 getFocusFromCenter()
	{
		final double x;
		final double y;
		if (getRadiusX() > getRadiusY())
		{
			x = Math.sqrt((radiusX * radiusX) - (radiusY * radiusY));
			y = 0;
		} else
		{
			x = 0;
			y = Math.sqrt((radiusY * radiusY) - (radiusX * radiusX));
		}
		return new Vector2(x, y).turn(turnAngle);
	}
	
	
	@Override
	public boolean isPointInShape(final IVector2 point)
	{
		final double lenPos = getFocusPositive().subtractNew(point).getLength2();
		final double lenNeg = getFocusNegative().subtractNew(point).getLength2();
		if ((lenPos + lenNeg) <= getDiameterMax())
		{
			return true;
		}
		return false;
	}
	
	
	@Override
	public boolean isPointInShape(final IVector2 point, final double margin)
	{
		Ellipse marginEllipse = new Ellipse(getCenter(), getRadiusX() + margin, getRadiusY() + margin, getTurnAngle());
		return marginEllipse.isPointInShape(point);
	}
	
	
	@Override
	public boolean isLineIntersectingShape(final ILine line)
	{
		return !lineIntersections(line).isEmpty();
	}
	
	
	@Override
	public IVector2 nearestPointOutside(final IVector2 point)
	{
		// get a point, that lies on one line with center and point and is outside field (greater radius distance from
		// center)
		IVector2 pointOutside = getCenter().addNew(point.subtractNew(getCenter()).scaleTo(getDiameterMax()));
		List<IVector2> intsPt = getIntersectingPoints(getCenter(), pointOutside);
		if (intsPt.size() != 1)
		{
			throw new IllegalStateException("Exactly one intersecting point expected, but was " + intsPt.size());
		}
		return intsPt.get(0);
	}
	
	
	@Override
	public IVector2 stepOnCurve(final IVector2 start, final double step)
	{
		if (GeoMath.distancePP(start, nearestPointOutside(start)) > POINT_ON_CURVE_TOL)
		{
			throw new IllegalArgumentException(
					"The start point is not on the ellipse border. Use nearestPointOutside to get an appropriate point");
		}
		
		IVector2 curPt = transformToNotTurned(start);
		double nextStep = step;
		final double maxStep = MAX_STEP_CURVE;
		do
		{
			double curStep;
			if (nextStep > 0)
			{
				curStep = Math.min(nextStep, maxStep);
			} else
			{
				curStep = Math.max(nextStep, -maxStep);
			}
			
			final IVector2 relStart = curPt.subtractNew(getCenter());
			// tangent formula, see Wikipedia, not sure if it is correct for a turned ellipse
			final IVector2 dir = new Vector2((-getRadiusX() * relStart.y()) / getRadiusY(), (getRadiusY() * relStart.x())
					/ getRadiusX());
			final IVector2 tmpP1 = curPt.addNew(dir.scaleToNew(curStep)).add(relStart);
			// ensure that we cross the ellipse border
			final IVector2 tmpP2 = GeoMath.stepAlongLine(tmpP1, getCenter(), -getDiameterMax());
			
			final List<IVector2> intsPts = getIntersectingPoints(getCenter(), transformToTurned(tmpP2));
			if (intsPts.size() != 1)
			{
				throw new IllegalStateException("Only one intersection point expected, but " + intsPts.size() + " found");
			}
			final IVector2 newP = transformToNotTurned(intsPts.get(0));
			
			// actual step
			double actStep = GeoMath.distancePP(curPt, newP);
			nextStep = nextStep - ((curStep > 0) ? actStep : -actStep);
			// if we passed zero
			if (isNegPos(nextStep, curStep) || (actStep < 0.001))
			{
				// exit
				nextStep = 0;
			}
			
			curPt = newP;
			
		} while (Math.abs(nextStep) > STEP_TOLERANCE);
		return transformToTurned(curPt);
	}
	
	
	/**
	 * Checks if a is neg and b is pos or wise versa
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	private boolean isNegPos(final double a, final double b)
	{
		return ((a > 0) && (b < 0)) || ((a < 0) && (b > 0));
	}
	
	
	@Override
	public List<IVector2> lineIntersections(final ILine line)
	{
		final IVector2 pt1 = line.supportVector();
		final IVector2 pt2 = line.supportVector().addNew(line.directionVector());
		
		return getIntersectingPoints(pt1, pt2, true);
	}
	
	
	@Override
	public List<IVector2> getIntersectingPoints(final IVector2 p1, final IVector2 p2)
	{
		return getIntersectingPoints(p1, p2, false);
	}
	
	
	/**
	 * Main method for getting intersecting points.
	 * Will calc the intersecting points between the two points
	 * 
	 * @param point1
	 * @param point2
	 * @param endlessLine are p1 and p2 two points on an endless line or should only the line between p1 and p2 be
	 *           considered?
	 * @return
	 */
	private List<IVector2> getIntersectingPoints(final IVector2 point1, final IVector2 point2, final boolean endlessLine)
	{
		final List<IVector2> result = new ArrayList<IVector2>(2);
		
		final IVector2 p0 = getCenter();
		final IVector2 p1 = transformToNotTurned(point1);
		final IVector2 p2 = transformToNotTurned(point2);
		
		// using double to avoid inaccurate results. (its fast enough)
		final double rrx = getRadiusX() * getRadiusX();
		final double rry = getRadiusY() * getRadiusY();
		final double x21 = p2.x() - p1.x();
		final double y21 = p2.y() - p1.y();
		final double x10 = p1.x() - p0.x();
		final double y10 = p1.y() - p0.y();
		final double a = ((x21 * x21) / rrx) + ((y21 * y21) / rry);
		final double b = ((x21 * x10) / rrx) + ((y21 * y10) / rry);
		final double c = ((x10 * x10) / rrx) + ((y10 * y10) / rry);
		final double d = (b * b) - (a * (c - 1));
		
		if (d >= 0)
		{
			final double e = Math.sqrt(d);
			final double u1 = (-b - e) / a;
			final double u2 = (-b + e) / a;
			if (endlessLine || ((0 <= u1) && (u1 <= 1)))
			{
				final IVector2 tmpP = new Vector2(p1.x() + (x21 * u1), p1.y() + (y21 * u1));
				result.add(transformToTurned(tmpP));
			}
			if (endlessLine || ((0 <= u2) && (u2 <= 1)))
			{
				final IVector2 tmpP = new Vector2(p1.x() + (x21 * u2), p1.y() + (y21 * u2));
				result.add(transformToTurned(tmpP));
			}
		}
		
		return result;
	}
	
	
	@Override
	public double getCircumference()
	{
		double l = (getRadiusX() - getRadiusY()) / (getRadiusX() + getRadiusY());
		double e = 1 + ((3 * l * l) / (10.0 + Math.sqrt(4 - (3 * l * l))));
		return Math.PI * (getRadiusX() + getRadiusY()) * e;
	}
	
	
	@Override
	public double getArea()
	{
		return radiusX * radiusY * Math.PI;
	}
	
	
	@Override
	public double getDiameterMax()
	{
		return Math.max(radiusX, radiusY) * 2;
	}
	
	
	/**
	 * Transform a point that is not turned with the turnAngle of the ellipse
	 * to a turned point
	 * 
	 * @param point
	 * @return
	 */
	private IVector2 transformToTurned(final IVector2 point)
	{
		return point.subtractNew(getCenter()).turn(getTurnAngle()).add(getCenter());
	}
	
	
	/**
	 * Transform a turned point (normal incoming point actually) to a non turned
	 * point (needed by some calculations, that do not consider turnAngle
	 * 
	 * @param point
	 * @return
	 */
	private IVector2 transformToNotTurned(final IVector2 point)
	{
		return point.subtractNew(getCenter()).turn(-getTurnAngle()).add(getCenter());
	}
	
	
	@Override
	public String toString()
	{
		return "Ellipse [center=" + center + ", radiusX=" + radiusX + ", radiusY=" + radiusY + ", turnAngle=" + turnAngle
				+ "]";
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	public double getTurnAngle()
	{
		return turnAngle;
	}
	
	
	@Override
	public IVector2 getCenter()
	{
		return center;
	}
	
	
	@Override
	public double getRadiusX()
	{
		return radiusX;
	}
	
	
	@Override
	public double getRadiusY()
	{
		return radiusY;
	}
}
