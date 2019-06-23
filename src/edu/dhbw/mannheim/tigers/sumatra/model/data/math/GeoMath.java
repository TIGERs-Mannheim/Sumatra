/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 14.09.2011
 * Author(s): stei_ol
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.math;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

import Jama.Matrix;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.SimpleWorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.exceptions.MathException;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.Circle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.ICircle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.ellipse.IEllipse;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector3f;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.ILine;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.Line;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ATrackedObject;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;


/**
 * Helper class for Geometry math problems.
 * 
 * @author osteinbrecher
 */
public final class GeoMath
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger	log				= Logger.getLogger(GeoMath.class.getName());
	
	/** Matrix X index */
	private static final int		X					= 0;
	/** Matrix X index */
	private static final int		Y					= 1;
	
	/** Senseless Vector. Vector2f(42000,42000). Use it to initialize your vector. */
	public static final IVector2	INIT_VECTOR		= new Vector2f(42000, 42000);
	
	/** Senseless Vector. Vector3f(42000,42000). Use it to initialize your vector. */
	public static final IVector3	INIT_VECTOR3	= new Vector3f(42000, 42000, 42000);
	
	/**  */
	public static final float		ACCURACY			= 0.001f;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * not instantiable
	 */
	private GeoMath()
	{
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Returns distance between two points
	 * 
	 * @param a
	 * @param b
	 * @return euclidean distance
	 * @author Oliver Steinbrecher <OST1988@aol.com>, Malte Mauelshagen <deineMutter@dlr.de>
	 */
	public static float distancePP(final IVector2 a, final IVector2 b)
	{
		return a.subtractNew(b).getLength2();
	}
	
	
	/**
	 * Squared distance between too points
	 * 
	 * @param a
	 * @param b
	 * @return The squared distance between two points
	 */
	public static float distancePPSqr(final IVector2 a, final IVector2 b)
	{
		final float abX = a.x() - b.x();
		final float abY = a.y() - b.y();
		return (abX * abX) + (abY * abY);
	}
	
	
	/**
	 * Shortest distance between a tracked object and a point.
	 * 
	 * @param object
	 * @param point
	 * @return
	 */
	public static float distancePP(final ATrackedObject object, final IVector2 point)
	{
		return distancePP(object.getPos(), point);
	}
	
	
	/**
	 * Calculates the distance between a point and a line.
	 * 
	 * @param point
	 * @param line1 , first point on the line
	 * @param line2 , second point on the line
	 * @return the distance between line and point
	 * @author Oliver Steinbrecher <OST1988@aol.com>
	 */
	public static float distancePL(final IVector2 point, final IVector2 line1, final IVector2 line2)
	{
		return distancePP(point, leadPointOnLine(point, line1, line2));
	}
	
	
	/**
	 * Create the lead point on a straight line (Lot f�llen).
	 * 
	 * @param point which should be used to create lead
	 * @param line1 , first point on the line
	 * @param line2 , second point on the line
	 * @return the lead point on the line
	 * @author Oliver Steinbrecher <OST1988@aol.com>
	 */
	public static Vector2 leadPointOnLine(final IVector2 point, final IVector2 line1, final IVector2 line2)
	{
		if (SumatraMath.isEqual(line1.x(), line2.x()))
		{
			// special case 1. line is orthogonal to x-axis
			return new Vector2(line1.x(), point.y());
			
		} else if (SumatraMath.isEqual(line1.y(), line2.y()))
		{
			// special case 2. line is orthogonal to y-axis
			return new Vector2(point.x(), line1.y());
			
		} else
		{
			// create straight line A from line1 to line2
			final float mA = (line2.y() - line1.y()) / (line2.x() - line1.x());
			final float nA = line2.y() - (mA * line2.x());
			
			// calculate straight line B
			final float mB = -1 / mA;
			final float nB = point.y() - (mB * point.x());
			
			// cut straight lines A and B
			final float xCut = (nB - nA) / (mA - mB);
			final float yCut = (mA * xCut) + nA;
			
			return new Vector2(xCut, yCut);
		}
	}
	
	
	/**
	 * Calculates the distance between a point and a line.
	 * 
	 * @param point
	 * @param line
	 * @return
	 */
	public static float distancePL(final IVector2 point, final ILine line)
	{
		return distancePP(point, leadPointOnLine(point, line));
	}
	
	
	/**
	 * Create the lead point on a straight line (Lot f�llen).
	 * 
	 * @param point
	 * @param line
	 * @return
	 */
	public static Vector2 leadPointOnLine(final IVector2 point, final ILine line)
	{
		return leadPointOnLine(point, line.supportVector(), line.supportVector().addNew(line.directionVector()));
	}
	
	
	/**
	 * Calculates the angle between x-Axis and a line, given by two points (p1, p2).<br>
	 * Further details {@link GeoMath#angleBetweenVectorAndVector(IVector2, IVector2) here}<br>
	 * 
	 * @param p1
	 * @param p2
	 * @author Malte
	 * @return
	 */
	public static float angleBetweenXAxisAndLine(final IVector2 p1, final IVector2 p2)
	{
		return angleBetweenXAxisAndLine(Line.newLine(p1, p2));
	}
	
	
	/**
	 * Calculates the angle between x-Axis and a line.<br>
	 * Further details here: {@link edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector2#getAngle()}
	 * 
	 * @author Malte
	 * @param l
	 * @return
	 */
	public static float angleBetweenXAxisAndLine(final ILine l)
	{
		return l.directionVector().getAngle();
	}
	
	
	/**
	 * Calculates the angle between two vectors (in rad).
	 * 
	 * @param v1
	 * @param v2
	 * @author AndreR
	 * @return angle in rad [0,PI]
	 */
	public static float angleBetweenVectorAndVector(final IVector2 v1, final IVector2 v2)
	{
		// The old version was numerically unstable, this one works better
		return Math.abs(angleBetweenVectorAndVectorWithNegative(v1, v2));
	}
	
	
	/**
	 * Calculates the angle between two vectors with respect to the rotation direction.
	 * 
	 * @see <a href=
	 *      "http://stackoverflow.com/questions/2663570/how-to-calculate-both-positive-and-negative-angle-between-two-lines"
	 *      >how-to-calculate-both-positive-and-negative-angle-between-two-lines</a>
	 * @see <a href= "http://en.wikipedia.org/wiki/Atan2" >Atan2 (wikipedia)</a>
	 * @param v1
	 * @param v2
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 * @return angle in rad [-PI,PI]
	 */
	public static float angleBetweenVectorAndVectorWithNegative(final IVector2 v1, final IVector2 v2)
	{
		// angle between positive x-axis and first vector
		final double angleA = Math.atan2(v1.x(), v1.y());
		// angle between positive x-axis and second vector
		final double angleB = Math.atan2(v2.x(), v2.y());
		// rotation
		float rotation = (float) (angleB - angleA);
		// fix overflows
		if (rotation < (-Math.PI - ACCURACY))
		{
			rotation += 2 * Math.PI;
		} else if (rotation > (Math.PI + ACCURACY))
		{
			rotation -= 2 * Math.PI;
		}
		return rotation;
	}
	
	
	/**
	 * A triangle is defined by three points(p1,p2,p3).
	 * This methods calculates the point(p4) where the bisector("Winkelhalbierende") of the angle(alpha) at p1 cuts the
	 * line p2-p3.
	 * 
	 * <pre>
	 *        p4
	 *  p2----x----p3
	 *    \   |   /
	 *     \  |  /
	 *      \^|^/
	 *       \|/<--alpha
	 *       p1
	 * </pre>
	 * 
	 * @param p1
	 * @param p2
	 * @param p3
	 * @return p4
	 * @author Malte
	 */
	public static Vector2 calculateBisector(final IVector2 p1, final IVector2 p2, final IVector2 p3)
	{
		if (p1.equals(p2) || p1.equals(p3))
		{
			log.warn("AIMath#calculateBisector(): some vectors are equal!");
			return new Vector2(p1);
		}
		if (p2.equals(p3))
		{
			return new Vector2(p2);
		}
		final Vector2 p1p2 = p2.subtractNew(p1);
		final Vector2 p1p3 = p3.subtractNew(p1);
		final Vector2 p3p2 = p2.subtractNew(p3);
		
		p3p2.scaleTo(p3p2.getLength2() / ((p1p2.getLength2() / p1p3.getLength2()) + 1));
		p3p2.add(p3);
		
		return p3p2;
	}
	
	
	/**
	 * Two line segments (Strecke) are given by two vectors each.
	 * This method calculates the distance between the line segments.
	 * If one or both of the lines are points (both vectors are the same) the distance form the line segment to the point
	 * is calculated
	 * THIS FUNCTION IS NOT CORRECT, IT IS JUST AN APPROXIMATION
	 * 
	 * @param l1p1
	 * @param l1p2
	 * @param l2p1
	 * @param l2p2
	 * @author Dirk
	 * @return
	 * @throws MathException if lines are parallel or equal or one of the vectors is zero
	 */
	public static float distanceBetweenLineSegments(final IVector2 l1p1, final IVector2 l1p2, final IVector2 l2p1,
			final IVector2 l2p2)
			throws MathException
	{
		// line crossing
		IVector2 lc = null;
		// special cases: one or both lines are points
		if (l1p1.equals(l1p2) && l2p1.equals(l2p2))
		{
			return distancePP(l1p1, l2p1);
		}
		else if (l1p1.equals(l1p2))
		{
			lc = leadPointOnLine(l1p1, new Line(l2p1, l2p2.subtractNew(l2p1)));
		}
		else if (l2p1.equals(l2p2))
		{
			lc = leadPointOnLine(l2p1, new Line(l1p1, l1p2.subtractNew(l1p1)));
		} else
		{
			// the normal case: both lines are real lines
			lc = GeoMath.intersectionPoint(l1p1, l1p2.subtractNew(l1p1), l2p1,
					l2p2.subtractNew(l2p1));
		}
		
		// limit to line segments
		IVector2 nearestPointToCrossingForLineSegement1 = new Vector2(lc);
		if (ratio(l1p1, lc, l1p2) > 1)
		{
			nearestPointToCrossingForLineSegement1 = new Vector2(l1p2);
		}
		if ((ratio(l1p2, lc, l1p1) > 1)
				&& ((ratio(l1p1, lc, l1p2) < 1) || (ratio(l1p2, lc, l1p1) < ratio(l1p1, lc, l1p2))))
		{
			nearestPointToCrossingForLineSegement1 = new Vector2(l1p1);
		}
		
		IVector2 nearestPointToCrossingForLineSegement2 = new Vector2(lc);
		if (ratio(l2p1, lc, l2p2) > 1)
		{
			nearestPointToCrossingForLineSegement2 = new Vector2(l2p2);
		}
		if ((ratio(l2p2, lc, l2p1) > 1)
				&& ((ratio(l2p1, lc, l2p2) < 1) || (ratio(l2p2, lc, l2p1) < ratio(l2p1, lc, l2p2))))
		{
			nearestPointToCrossingForLineSegement2 = new Vector2(l2p1);
		}
		return nearestPointToCrossingForLineSegement2.subtractNew(nearestPointToCrossingForLineSegement1).getLength2();
	}
	
	
	/**
	 * returns the factor the distance between root and point 1 is longer than the distance between root and point 2
	 * e.g. root = (0,0), point1 = (100,0), point2 = (200,0) -> ratio = 1/2
	 * 
	 * @param root
	 * @param point1
	 * @param point2
	 * @return
	 */
	public static float ratio(final IVector2 root, final IVector2 point1, final IVector2 point2)
	{
		if (point2.equals(root))
		{
			// ratio is inifinite
			return Float.MAX_VALUE;
		}
		return (point1.subtractNew(root).getLength2() / point2.subtractNew(root).getLength2());
	}
	
	
	/**
	 * Two lines are given by a support vector <b>p</b> ("Stuetzvektor") and a direction vector <b>v</b>
	 * ("Richtungsvektor").
	 * This methods calculate the point where these lines intersect.
	 * If lines are parallel or equal or one of the vectors is zero Exeption is thrown!!
	 * 
	 * @param p1
	 * @param v1
	 * @param p2
	 * @param v2
	 * @author Malte
	 * @return
	 * @throws MathException if lines are parallel or equal or one of the vectors is zero
	 */
	public static Vector2 intersectionPoint(final IVector2 p1, final IVector2 v1, final IVector2 p2, final IVector2 v2)
			throws MathException
	{
		if (v1.equals(AVector2.ZERO_VECTOR))
		{
			throw new MathException("v1 is the zero vector!");
		}
		if (v2.equals(AVector2.ZERO_VECTOR))
		{
			throw new MathException("v2 is the zero vector!");
		}
		// Create a matrix
		final Matrix m = new Matrix(2, 2);
		m.set(0, 0, v1.x());
		m.set(0, 1, -v2.x());
		m.set(1, 0, v1.y());
		m.set(1, 1, -v2.y());
		
		final double[] b = { p2.x() - p1.x(), p2.y() - p1.y() };
		if (m.rank() == 1)
		{
			throw new MathException("Given lines are parallel or equal!");
		}
		
		final Matrix bM = new Matrix(2, 1);
		bM.set(0, 0, b[X]);
		bM.set(1, 0, b[Y]);
		final Matrix solved = m.solve(bM);
		
		final float x = (float) ((solved.get(0, 0) * v1.x()) + p1.x());
		final float y = (float) ((solved.get(0, 0) * v1.y()) + p1.y());
		
		return new Vector2(x, y);
		
	}
	
	
	/**
	 * This methods calculate the point where two lines (l1, l2) intersect.
	 * If lines are parallel or equal, Exception is thrown.
	 * 
	 * @param l1
	 * @param l2
	 * @throws MathException if lines are parallel or equal
	 * @author Malte
	 * @return
	 */
	public static Vector2 intersectionPoint(final ILine l1, final ILine l2) throws MathException
	{
		return intersectionPoint(l1.supportVector(), l1.directionVector(), l2.supportVector(), l2.directionVector());
	}
	
	
	/**
	 * Calculates the intersection point of two lines.
	 * Throws MathException if lines are parallel, equal or intersection is off line boundaries.
	 * Will not work with horizontal or vertical lines.
	 * 
	 * @param l1
	 * @param l2
	 * @return Intersection point
	 * @throws MathException
	 * @author JulianT
	 */
	public static Vector2 intersectionPointOnLine(final ILine l1, final ILine l2) throws MathException
	{
		IVector2 intersect = intersectionPoint(l1, l2);
		
		if (isPointOnLine(l1, intersect) && isPointOnLine(l2, intersect))
		{
			return (Vector2) intersect;
		}
		
		throw new MathException("No intersection on line");
		
	}
	
	
	/**
	 * Calculates if a Point is on a Line.
	 * 
	 * @param line
	 * @param point
	 * @return True, if Point on Line
	 * @author SimonS
	 */
	public static boolean isPointOnLine(final ILine line, final IVector2 point)
	{
		boolean pointOnLine = false;
		
		IVector2 supportV = line.supportVector();
		IVector2 directionV = line.directionVector();
		
		IVector2 startLine1 = supportV;
		IVector2 endLine1 = supportV.addNew(directionV);
		
		float faktorX = (point.x() - supportV.x()) / directionV.x();
		float faktorY = (point.y() - supportV.y()) / directionV.y();
		
		if (Float.isNaN(faktorX))
		{
			faktorX = faktorY;
		}
		
		if (Float.isNaN(faktorY))
		{
			faktorY = faktorX;
		}
		
		
		if (isVectorBetween(point, startLine1, endLine1) && SumatraMath.isEqual(faktorX, faktorY))
		{
			pointOnLine = true;
		} else
		{
			pointOnLine = false;
		}
		
		return pointOnLine;
		
	}
	
	
	/**
	 * Check if is a Vektor bewtween min and max.
	 * Only look at x and y
	 * 
	 * @param point
	 * @param min
	 * @param max
	 * @return True
	 * @author SimonS
	 */
	public static boolean isVectorBetween(final IVector2 point, final IVector2 min, final IVector2 max)
	{
		return (SumatraMath.isBetween(point.x(), min.x(), max.x()) && SumatraMath.isBetween(point.y(), min.y(), max.y()));
	}
	
	
	/**
	 * A line is given by its slope and a point on it.
	 * This method calculates the y-Intercept.
	 * 
	 * @param point
	 * @param slope
	 * @return yIntercept
	 * @author ChristianK
	 */
	public static float yInterceptOfLine(final IVector2 point, final float slope)
	{
		return (point.y() - (slope * point.x()));
	}
	
	
	/**
	 * Indicates if line intercepts/touches circle
	 * 
	 * @param center of circle
	 * @param radius of circle
	 * @param slope of line
	 * @param yIntercept
	 * @return true if line intercepts circle
	 * @author ChristianK
	 */
	public static boolean isLineInterceptingCircle(final IVector2 center, final float radius, final float slope,
			final float yIntercept)
	{
		// based on equation of cirle and line
		// trying to intercept leads to a quadratic-equation
		// p-q-equation is used
		// point of interception doesn't matter --> checks only if value in sqrt is >= 0 (i.e. equation is solvable, i.e.
		// is intercepting
		
		final float p = (((-2 * center.x()) + (2 * slope * yIntercept)) - (2 * center.y() * slope))
				/ (1 + (slope * slope));
		final float q = (((((center.x() * center.x()) + (yIntercept * yIntercept)) - (2 * center.y() * yIntercept)) + (center
				.y() * center.y())) - (radius * radius))
				/ (1 + (slope * slope));
		
		if ((((p * p) / 4) - q) >= 0)
		{
			// yepp, is intercepting
			return true;
		}
		// nope, not intercepting
		return false;
	}
	
	
	/**
	 * calculates a point on a circle defined by center and current vectors
	 * performs a projection (rotation) of {@link IVector2}
	 * 
	 * @param current point on circle
	 * @param center of circle
	 * @param angle of rotation
	 * @return projected point
	 * @author DanielW
	 */
	public static Vector2 stepAlongCircle(final IVector2 current, final IVector2 center, final float angle)
	{
		/*
		 * x' = (x-u) cos(beta) - (y-v) sin(beta) + u
		 * y' = (x-u) sin(beta) + (y-v) cos(beta) + v
		 */
		final float x = (((current.x() - center.x()) * AngleMath.cos(angle)) - ((current.y() - center.y()) * AngleMath
				.sin(angle))) + center.x();
		final float y = ((current.x() - center.x()) * AngleMath.sin(angle))
				+ ((current.y() - center.y()) * AngleMath.cos(angle)) + center.y();
		
		return new Vector2(x, y);
	}
	
	
	/**
	 * Distance between two points on a circle
	 * 
	 * @param center
	 * @param p1
	 * @param p2
	 * @return
	 */
	public static float distancePPCircle(final IVector2 center, final IVector2 p1, final IVector2 p2)
	{
		IVector2 c2p1 = p1.subtractNew(center);
		IVector2 c2p2 = p2.subtractNew(center);
		float angle = angleBetweenVectorAndVector(c2p1, c2p2);
		float radius = GeoMath.distancePP(p1, center);
		float u = 2 * radius * AngleMath.PI;
		return (angle / AngleMath.PI_TWO) * u;
	}
	
	
	/**
	 * calculates a point on a line between start and end, that is stepSize away from start
	 * calculation is based on Intercept theorem (Strahlensatz)
	 * 
	 * @param start
	 * @param end
	 * @param stepSize
	 * @author ChristianK
	 * @return
	 */
	public static Vector2 stepAlongLine(final IVector2 start, final IVector2 end, final float stepSize)
	{
		final Vector2 result = new Vector2();
		
		final float distance = distancePP(start, end);
		if (distance == 0)
		{
			result.x = end.x();
			result.y = end.y();
			return result;
		}
		
		final float coefficient = stepSize / distance;
		
		final float xDistance = end.x() - start.x();
		final float yDistance = end.y() - start.y();
		
		
		result.x = (xDistance * coefficient) + start.x();
		result.y = (yDistance * coefficient) + start.y();
		if (Float.isNaN(result.x()) || Float.isNaN(result.y()))
		{
			log.fatal("stepAlongLine: result contains NaNs. Very dangerous!!");
			final String seperator = " / ";
			log.fatal(start.toString() + seperator + end.toString() + seperator + distance + seperator + coefficient
					+ seperator + xDistance + seperator + yDistance + seperator + result.toString());
		}
		return result;
	}
	
	
	/**
	 * Calculates the next point on the ellipse from start, step wide<br>
	 * This is just a wrapper to {@link IEllipse#stepOnCurve(IVector2, float)}
	 * 
	 * @param ellipse ellipse can also be used for elliptic curve
	 * @param start start point on ellipse, this must be on the ellipse border!
	 * @param step how many steps to go, may be negative for clockwise direction
	 * @return
	 */
	public static IVector2 stepAlongEllipse(final IEllipse ellipse, final IVector2 start, final float step)
	{
		return ellipse.stepOnCurve(start, step);
	}
	
	
	/**
	 * Calculates the intersection points of the given line and the circle.
	 * If they do not intersect, the list is empty.
	 * 
	 * @param l
	 * @param c
	 * @return
	 */
	public static List<IVector2> lineCircleIntersections(final ILine l, final ICircle c)
	{
		return c.lineIntersections(l);
	}
	
	
	/**
	 * Checks if the beam between two points is blocked or not.
	 * ray looks like this:
	 * 
	 * <pre>
	 * | * |
	 * |   |
	 * |   |
	 * | * |
	 * </pre>
	 * 
	 * @param wf
	 * @param start
	 * @param end
	 * @param raySize
	 * @param ignoreIds
	 * @return
	 * @author GuntherB
	 */
	public static boolean p2pVisibility(final SimpleWorldFrame wf, final IVector2 start, final IVector2 end,
			final float raySize,
			final Collection<BotID> ignoreIds)
	{
		final float minDistance = AIConfig.getGeometry().getBallRadius() + AIConfig.getGeometry().getBotRadius()
				+ raySize;
		
		// checking free line
		final float distanceStartEndSquared = distancePPSqr(start, end);
		for (final TrackedBot bot : wf.getBots().values())
		{
			if (ignoreIds.contains(bot.getId()))
			{
				continue;
			}
			final float distanceBotStartSquared = distancePPSqr(bot.getPos(), start);
			final float distanceBotEndSquared = distancePPSqr(bot.getPos(), end);
			if ((distanceStartEndSquared > distanceBotStartSquared) && (distanceStartEndSquared > distanceBotEndSquared))
			{
				// only check those bots that possibly can be in between start and end
				final float distanceBotLine = distancePL(bot.getPos(), start, end);
				if (distanceBotLine < minDistance)
				{
					return false;
				}
			}
		}
		
		return true;
	}
	
	
	/**
	 * Checks if the beam between two points is blocked by ball or not.
	 * ray looks like this:
	 * 
	 * <pre>
	 * | * |
	 * |   |
	 * |   |
	 * | * |
	 * </pre>
	 * 
	 * @param wf
	 * @param start
	 * @param end
	 * @param raySize
	 * @return
	 * @author GuntherB
	 */
	public static boolean p2pVisibilityBall(final SimpleWorldFrame wf, final IVector2 start, final IVector2 end,
			final float raySize)
	{
		final float minDistance = AIConfig.getGeometry().getBallRadius() + AIConfig.getGeometry().getBotRadius()
				+ raySize;
		
		// checking free line
		final float distanceStartEndSquared = distancePPSqr(start, end);
		IVector2 ballPos = wf.getBall().getPos();
		final float distanceBotStartSquared = distancePPSqr(ballPos, start);
		final float distanceBotEndSquared = distancePPSqr(ballPos, end);
		if ((distanceStartEndSquared > distanceBotStartSquared) && (distanceStartEndSquared > distanceBotEndSquared))
		{
			// only check those bots that possibly can be in between start and end
			final float distanceBotLine = distancePL(ballPos, start, end);
			if (distanceBotLine < minDistance)
			{
				return false;
			}
		}
		
		return true;
	}
	
	
	/**
	 * Check p2pVisibility for ball and all bots expect those given
	 * 
	 * @param wf
	 * @param start
	 * @param end
	 * @param raySize
	 * @param ignoreBotId
	 * @return
	 */
	public static boolean p2pVisibilityBotBall(final SimpleWorldFrame wf, final IVector2 start, final IVector2 end,
			final float raySize, final BotID... ignoreBotId)
	{
		if (!p2pVisibilityBall(wf, start, end, raySize))
		{
			return false;
		}
		return p2pVisibility(wf, start, end, raySize, Arrays.asList(ignoreBotId));
	}
	
	
	/**
	 * {@link GeoMath#p2pVisibility(WorldFrame, IVector2, IVector2, List)}
	 * 
	 * @param wf
	 * @param start
	 * @param end
	 * @param ignoreBotId
	 * @return
	 */
	public static boolean p2pVisibility(final WorldFrame wf, final IVector2 start, final IVector2 end,
			final BotID... ignoreBotId)
	{
		return p2pVisibility(wf, start, end, Arrays.asList(ignoreBotId));
	}
	
	
	/**
	 * {@link GeoMath#p2pVisibility(WorldFrame, IVector2, IVector2, List)}
	 * 
	 * @param wf
	 * @param start
	 * @param end
	 * @param raySize
	 * @param ignoreBotId
	 * @return
	 */
	public static boolean p2pVisibility(final WorldFrame wf, final IVector2 start, final IVector2 end,
			final Float raySize, final BotID... ignoreBotId)
	{
		return p2pVisibility(wf, start, end, raySize, Arrays.asList(ignoreBotId));
	}
	
	
	/**
	 * {@link GeoMath#p2pVisibility(WorldFrame, IVector2, IVector2, List)}
	 * 
	 * @param wf
	 * @param start
	 * @param end
	 * @param ignoreIds
	 * @return
	 */
	public static boolean p2pVisibility(final WorldFrame wf, final IVector2 start, final IVector2 end,
			final List<BotID> ignoreIds)
	{
		return p2pVisibility(wf, start, end, 0f, ignoreIds);
	}
	
	
	/**
	 * Check if one of the end points is visible from start
	 * 
	 * @param wf
	 * @param start
	 * @param ends
	 * @param ignoreIds
	 * @return
	 */
	public static boolean p2pVisibility(final WorldFrame wf, final IVector2 start, final List<IVector2> ends,
			final List<BotID> ignoreIds)
	{
		for (IVector2 end : ends)
		{
			if (p2pVisibility(wf, start, end, ignoreIds))
			{
				return true;
			}
		}
		return false;
	}
	
	
	/**
	 * Check if the position in the First, Second, Third, or Fourth Quadrant.
	 * Note: <strong> We are every time in quadrant 2,3 and the foe in 1,4</strong>
	 * 
	 * @param position to check
	 * @return 1,2,3,4 for the number of the quadrant
	 * @author PhilippP (Ph.Posovszky@gmail.com)
	 */
	public static int checkQuadrant(final IVector2 position)
	{
		if ((position.x() >= 0) && (position.y() >= 0))
		{
			return 1;
		} else if ((position.x() < 0) && (position.y() > 0))
		{
			return 2;
		} else if ((position.x() <= 0) && (position.y() <= 0))
		{
			return 3;
		} else
		{
			return 4;
		}
		
	}
	
	
	/**
	 * Determines if a position is inside or outside the field
	 * 
	 * @param pos The questionable position as IVector2
	 * @return true if pos is inside, false otherwise
	 */
	public static boolean isInsideField(final IVector2 pos)
	{
		return AIConfig.getGeometry().getFieldWBorders().isPointInShape(pos);
	}
	
	
	/**
	 * Computes the next position inside the field given any coordinates
	 * 
	 * @param pos IVector2 that may be outside the field
	 * @return The IVector2 inside the field next to pos
	 */
	public static IVector2 nextInsideField(final IVector2 pos)
	{
		Vector2 rtrnPos = new Vector2();
		
		if ((2 * Math.abs(pos.x())) >= AIConfig.getGeometry().getFieldLength())
		{
			rtrnPos.setX(Math.signum(pos.x())
					* ((0.5f * AIConfig.getGeometry().getFieldLength()) - AIConfig.getGeometry().getBotRadius()));
		} else
		{
			rtrnPos.setX(pos.x());
		}
		
		if ((2 * Math.abs(pos.y())) >= AIConfig.getGeometry().getFieldWidth())
		{
			rtrnPos.setY(Math.signum(pos.y())
					* ((0.5f * AIConfig.getGeometry().getFieldWidth()) - AIConfig.getGeometry().getBotRadius()));
		} else
		{
			rtrnPos.setY(pos.y());
		}
		
		return rtrnPos;
	}
	
	
	/**
	 * Get the intersection points of the two tangential lines that cross the external points.
	 * 
	 * @see <a href="https://de.wikipedia.org/wiki/Kreistangente">Kreistangente</a>
	 * @param circle
	 * @param externalPoint
	 * @return
	 */
	public static List<IVector2> tangentialIntersections(final Circle circle, final IVector2 externalPoint)
	{
		IVector2 dir = externalPoint.subtractNew(circle.center());
		float d = dir.getLength2();
		float alpha = (float) Math.acos(circle.radius() / d);
		float beta = dir.getAngle();
		
		List<IVector2> points = new ArrayList<IVector2>(2);
		points.add(circle.center().addNew(new Vector2(beta + alpha).scaleTo(circle.radius())));
		points.add(circle.center().addNew(new Vector2(beta - alpha).scaleTo(circle.radius())));
		return points;
	}
	
	
	/**
	 * Get the nearest point to p from the list
	 * 
	 * @param list
	 * @param p
	 * @return
	 */
	public static IVector2 nearestPointInList(final List<IVector2> list, final IVector2 p)
	{
		if (list.isEmpty())
		{
			return p;
		}
		IVector2 closest = null;
		float closestDist = Float.MAX_VALUE;
		for (IVector2 vec : list)
		{
			float dist = GeoMath.distancePP(vec, p);
			if (closestDist > dist)
			{
				closestDist = dist;
				closest = vec;
			}
		}
		return closest;
	}
}
