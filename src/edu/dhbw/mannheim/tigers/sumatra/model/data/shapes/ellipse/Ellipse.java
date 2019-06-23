/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 17, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.ellipse;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Embeddable;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.ILine;


/**
 * Default implementation of an ellipse
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
@Embeddable
public class Ellipse implements IEllipse
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final float	MAX_STEP_CURVE			= 5f;
	private static final float	POINT_ON_CURVE_TOL	= 5f;
	private static final float	STEP_TOLERANCE			= 0.01f;
	private final IVector2		center;
	private final float			radiusX;
	private final float			radiusY;
	private final float			turnAngle;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @param center
	 * @param radiusX
	 * @param radiusY
	 * @param turnAngle
	 */
	public Ellipse(final IVector2 center, final float radiusX, final float radiusY, final float turnAngle)
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
	public Ellipse(final IVector2 center, final float radiusX, final float radiusY)
	{
		this(center, radiusX, radiusY, 0);
	}
	
	
	/**
	 * Copy constructor
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
	public IVector2 getApex(EApexType apexType)
	{
		final IVector2 res;
		final float bigRad = getDiameterMax();
		
		switch (apexType)
		{
			case MAIN_POS:
				res = center.addNew(getFocusFromCenter().scaleToNew(Math.max(radiusX, radiusY)));
				break;
			case MAIN_NEG:
				res = center.addNew(getFocusFromCenter().scaleToNew(-Math.max(radiusX, radiusY)));
				break;
			case SEC_POS:
				res = center.addNew(getFocusFromCenter().turnNew((float) Math.PI / 2)
						.scaleToNew(Math.min(radiusX, radiusY)));
				break;
			case SEC_NEG:
				res = center.addNew(getFocusFromCenter().turnNew((float) Math.PI / 2).scaleToNew(
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
		final float x;
		final float y;
		if (getRadiusX() > getRadiusY())
		{
			x = (float) Math.sqrt((radiusX * radiusX) - (radiusY * radiusY));
			y = 0;
		} else
		{
			x = 0;
			y = (float) Math.sqrt((radiusY * radiusY) - (radiusX * radiusX));
		}
		return new Vector2(x, y).turn(turnAngle);
	}
	
	
	@Override
	public boolean isPointInShape(IVector2 point)
	{
		final float lenPos = getFocusPositive().subtractNew(point).getLength2();
		final float lenNeg = getFocusNegative().subtractNew(point).getLength2();
		if ((lenPos + lenNeg) <= getDiameterMax())
		{
			return true;
		}
		return false;
	}
	
	
	@Override
	public boolean isLineIntersectingShape(ILine line)
	{
		return !getIntersectingPoints(line).isEmpty();
	}
	
	
	@Override
	public IVector2 nearestPointOutside(IVector2 point)
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
	public IVector2 stepOnCurve(IVector2 start, float step)
	{
		if (GeoMath.distancePP(start, nearestPointOutside(start)) > POINT_ON_CURVE_TOL)
		{
			throw new IllegalArgumentException(
					"The start point is not on the ellipse border. Use nearestPointOutside to get an appropriate point");
		}
		
		IVector2 curPt = transformToNotTurned(start);
		float nextStep = step;
		final float maxStep = MAX_STEP_CURVE;
		do
		{
			float curStep;
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
			float actStep = GeoMath.distancePP(curPt, newP);
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
	private boolean isNegPos(final float a, final float b)
	{
		return ((a > 0) && (b < 0)) || ((a < 0) && (b > 0));
	}
	
	
	@Override
	public List<IVector2> getIntersectingPoints(ILine line)
	{
		final IVector2 pt1 = line.supportVector();
		final IVector2 pt2 = line.supportVector().addNew(line.directionVector());
		
		return getIntersectingPoints(pt1, pt2, true);
	}
	
	
	@Override
	public List<IVector2> getIntersectingPoints(IVector2 p1, IVector2 p2)
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
	public float getCircumference()
	{
		double l = (getRadiusX() - getRadiusY()) / (getRadiusX() + getRadiusY());
		double e = 1 + ((3 * l * l) / (10 + Math.sqrt(4 - (3 * l * l))));
		return (float) (Math.PI * (getRadiusX() + getRadiusY()) * e);
	}
	
	
	@Override
	public float getArea()
	{
		return (float) (radiusX * radiusY * Math.PI);
	}
	
	
	@Override
	public float getDiameterMax()
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
	public float getTurnAngle()
	{
		return turnAngle;
	}
	
	
	@Override
	public IVector2 getCenter()
	{
		return center;
	}
	
	
	@Override
	public float getRadiusX()
	{
		return radiusX;
	}
	
	
	@Override
	public float getRadiusY()
	{
		return radiusY;
	}
}
