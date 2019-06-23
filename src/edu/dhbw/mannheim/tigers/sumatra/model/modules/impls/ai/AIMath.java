/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 03.08.2010
 * Author(s): Malte
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import Jama.Matrix;

import edu.dhbw.mannheim.tigers.sumatra.model.data.ATrackedObject;
import edu.dhbw.mannheim.tigers.sumatra.model.data.AVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Circle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.ILine;
import edu.dhbw.mannheim.tigers.sumatra.model.data.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Line;
import edu.dhbw.mannheim.tigers.sumatra.model.data.MathException;
import edu.dhbw.mannheim.tigers.sumatra.model.data.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;


/**
 * This class holds math-functions often used in the AI Module.<br>
 * 
 * If you change something here or have ideas for new implementations please contact the owner.
 * If you want to add more functionality, focus on clear documentation (like in the already existing methods) and leave
 * your name!
 * 
 * @author Malte
 */
public class AIMath
{
	private static final Logger	LOG				= Logger.getLogger(AIMath.class);
	// --------------------------------------------------------------------------
	// --- Basic math constants -------------------------------------------------
	// --------------------------------------------------------------------------
	public static final float		PI					= (float) Math.PI;
	public static final float		PI_TWO			= (float) (Math.PI * 2);
	public static final float		PI_HALF			= (float) (Math.PI / 2);
	public static final float		PI_QUART			= (float) (Math.PI / 4);
	public static final float		PI_SQR			= (float) (Math.PI * Math.PI);
	public static final float		PI_INV			= (float) (1 / Math.PI);
	public static final float		PI_TWO_INV		= (float) (1 / (Math.PI * 2));
	
	public static final float		DEG_TO_RAD		= PI / 180.0f;
	public static final float		RAD_TO_DEG		= 180.0f / PI;
	
	private static final float		EPS				= 0.00001f;
	
	// --------------------------------------------------------------------------
	// --- Factorial ------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * The maximum number for which the function {@link #faculty(int)} can return a result via lookup-array (
	 * {@link #FACTORIALS})!
	 */
	private static final int		FACTORIAL_MAX	= 10;
	private static final long[]	FACTORIALS		= new long[FACTORIAL_MAX + 1];
	private static final float[]	SINUS				= new float[360001];
	private static final float[]	COSINUS			= new float[360001];
	private static final float[]	TANGENS			= new float[360001];
	

	// Static initialization of the Lookup-array
	static
	{
		long n = 1;
		FACTORIALS[0] = n;
		for (int i = 1; i <= FACTORIAL_MAX; i++)
		{
			n *= i;
			FACTORIALS[i] = n;
		}
	}
	
	// Static initialization of the trigonometric functions
	static
	{
		for (int i = 0; i < 360001; i++)
		{
			SINUS[i] = (float) Math.sin(i / (1000 * 360 / (AIMath.PI_TWO)) - AIMath.PI);
			COSINUS[i] = (float) Math.cos(i / (1000 * 360 / (AIMath.PI_TWO)) - AIMath.PI);
			TANGENS[i] = (float) Math.tan(i / (1000 * 360 / (AIMath.PI_TWO)) - AIMath.PI);
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- Basic conversion functions -------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * This is not inaccurate or fast. It just converts the result to float!
	 * 
	 * @author Malte
	 */
	public static float sqrt(float number)
	{
		return (float) Math.sqrt(number);
	}
	

	/**
	 * SINUS!
	 * 
	 * @author Malte
	 */
	public static float sin(float number)
	{
		return SINUS[(int) ((normalizeAngle(number) + AIMath.PI) * (360f / PI_TWO) * 1000f)];
	}
	

	/**
	 * COSINUS!
	 * 
	 * @author Malte
	 */
	public static float cos(float number)
	{
		return COSINUS[(int) ((normalizeAngle(number) + AIMath.PI) * (360f / PI_TWO) * 1000f)];
	}
	

	/**
	 * TANGENS!
	 * 
	 * @author Malte
	 */
	public static float tan(float number)
	{
		return TANGENS[(int) ((normalizeAngle(number) + AIMath.PI) * (360f / PI_TWO) * 1000f)];
	}
	

	/**
	 * @param number
	 * @return (float) {@link Math#acos(double)}
	 * @author Gero
	 */
	public static float acos(float number)
	{
		return (float) Math.acos(number);
	}
	

	/**
	 * @param deg The angle in degree that should be converted to radiant
	 * @return The given angle in radiant
	 * 
	 * @author Gero
	 */
	public static float deg2rad(float deg)
	{
		return DEG_TO_RAD * deg;
	}
	

	/**
	 * @param rad The angle in radiant that should be converted to degree
	 * @return The given angle in degree
	 * 
	 * @author Gero
	 */
	public static float rad2deg(float rad)
	{
		return RAD_TO_DEG * rad;
	}
	

	/**
	 * @param exponent The exponent
	 * @return (float) {@link Math#exp(double)}
	 * 
	 * @author Gero
	 */
	public static float exp(float exponent)
	{
		return (float) Math.exp(exponent);
	}
	

	/**
	 * Checks, if x is almost 0 (within the epsilon environment).<br>
	 * -epsilon < x < epsilon
	 * @param x
	 * @param epsilon
	 * 
	 * @author GuntherB
	 */
	public static boolean isZero(float x, float epsilon)
	{
		if (x > -epsilon && x < epsilon)
		{
			return true;
		} else
		{
			return false;
		}
	}
	

	/**
	 * 
	 * Checks, if x is almost 0 (within the epsilon environment).<br>
	 * -EPSILON < x < EPSILON <br>
	 * EPSILON = 0.00001f;
	 * @author GuntherB
	 */
	public static boolean isZero(float x)
	{
		return isZero(x, EPS);
	}
	

	/**
	 * Returns distance between two points
	 * 
	 * @param a
	 * @param b
	 * @return euclidean distance
	 * 
	 * @author Oliver Steinbrecher <OST1988@aol.com>, Malte Mauelshagen <deineMutter@dlr.de>
	 */
	public static float distancePP(IVector2 a, IVector2 b)
	{
		return a.subtractNew(b).getLength2();
	}
	

	/**
	 * @param a
	 * @param b
	 * @return The squared distance between two points
	 *         TODO @author ???
	 */
	public static float distancePPSqr(IVector2 a, IVector2 b)
	{
		float abX = a.x() - b.x();
		float abY = a.y() - b.y();
		return abX * abX + abY * abY;
	}
	

	/**
	 * 
	 * Shortest distance between a tracked object and a point.
	 * @param object
	 * @param point
	 *           TODO @author ???
	 */
	public static float distancePP(ATrackedObject object, IVector2 point)
	{
		return distancePP(object.pos, point);
	}
	

	/**
	 * Calculates the distance between a point and a line.
	 * 
	 * @param point
	 * @param line1 , first point on the line
	 * @param line2 , second point on the line
	 * @return the distance between line and point
	 * 
	 * @author Oliver Steinbrecher <OST1988@aol.com>
	 */
	public static float distancePL(IVector2 point, IVector2 line1, IVector2 line2)
	{
		return distancePP(point, leadPointOnLine(point, line1, line2));
	}
	

	/**
	 * Create the lead point on a straight line (Lot fällen).
	 * 
	 * @param point which should be used to create lead
	 * @param line1 , first point on the line
	 * @param line2 , second point on the line
	 * @return the lead point on the line
	 * 
	 * @author Oliver Steinbrecher <OST1988@aol.com>
	 */
	public static Vector2 leadPointOnLine(IVector2 point, IVector2 line1, IVector2 line2)
	{
		Vector2 result = null;
		
		if (line1.x() == line2.x())
		{
			// special case 1. line is orthogonal to x-axis
			result = new Vector2(line1.x(), point.y());
		} else if (line1.y() == line2.y())
		{
			// special case 2. line is orthogonal to y-axis
			result = new Vector2(point.x(), line1.y());
		} else
		{
			// create straight line A from line1 to line2
			float mA = (line2.y() - line1.y()) / (line2.x() - line1.x());
			float nA = line2.y() - (mA * line2.x());
			
			// calculate straight line B
			float mB = -1 / mA;
			float nB = point.y() - mB * point.x();
			
			// cut straight lines A and B
			float xCut = (nB - nA) / (mA - mB);
			float yCut = mA * xCut + nA;
			
			result = new Vector2(xCut, yCut);
		}
		
		return result;
	}
	

	/**
	 * Calculates the distance between a point and a line.
	 */
	public static float distancePL(IVector2 point, Line line)
	{
		return distancePP(point, leadPointOnLine(point, line));
	}
	

	/**
	 * Create the lead point on a straight line (Lot fällen).
	 * 
	 */
	public static Vector2 leadPointOnLine(IVector2 point, ILine line)
	{
		return leadPointOnLine(point, line.supportVector(), line.supportVector().addNew(line.directionVector()));
	}
	

	/**
	 * this method returns the faculty of an int.
	 * @param n
	 * @author Malte, Gero
	 */
	public static long faculty(int n)
	{
		if (n > FACTORIAL_MAX)
		{
			LOG.error("AIMath.faculty is limited to FACTORIAL_MAX; if you need more, change it! ;-)");
			return -1;
		} else if (n < 0)
		{
			LOG.error("AIMath.faculty: Can't calculate faculty of a negative number!");
			return -1;
		} else
		{
			return FACTORIALS[n];
		}
	}
	

	/**
	 * Calculates the angle between x-Axis and a line, given by two points (p1, p2).<br>
	 * Further details {@link AIMath#angleBetweenXAxisAndVector(AVector2) here}<br>
	 * 
	 * 
	 * @param p1
	 * @param p2
	 * @author Malte
	 */
	public static float angleBetweenXAxisAndLine(IVector2 p1, IVector2 p2)
	{
		Line line = new Line();
		line.setPoints(p1, p2);
		return angleBetweenXAxisAndLine(line);
	}
	

	/**
	 * Calculates the angle between x-Axis and a line.<br>
	 * Further details here: {@link AVector2#getAngle()}
	 * 
	 * @param p1
	 * @param p2
	 * @author Malte
	 */
	public static float angleBetweenXAxisAndLine(Line l)
	{
		return l.directionVector().getAngle();
	}
	

	/**
	 * calculates a point on a circle defined by center and current vectors
	 * performs a projection (rotation) of {@link current}
	 * 
	 * @param current point on circle
	 * @param center of circle
	 * @param angle of rotation
	 * @return projected point
	 * @author DanielW
	 */
	public static Vector2 getNextPointOnCircle(IVector2 current, IVector2 center, float angle)
	{
		/*
		 * x' = (x-u) cos(beta) - (y-v) sin(beta) + u
		 * y' = (x-u) sin(beta) + (y-v) cos(beta) + v
		 */
		float x = (current.x() - center.x()) * cos(angle) - (current.y() - center.y()) * sin(angle) + center.x();
		float y = (current.x() - center.x()) * sin(angle) + (current.y() - center.y()) * cos(angle) + center.y();
		Vector2 result = new Vector2(x, y);
		return result;
	}
	

	/**
	 * 
	 * Normalize angle, to make sure angle is in (-pi/pi] interval.<br>
	 * New angle is returned, parameter stay unaffected.
	 * 
	 * @param angle
	 * @author Malte
	 */
	public static float normalizeAngle(float angle)
	{
		// Don't call this a hack! It's numeric!
		return (angle - Math.round((angle / (2 * AIMath.PI)) - 0.000001) * 2 * AIMath.PI);
	}
	

	/**
	 * A triangle is defined by three points(p1,p2,p3).
	 * This methods calculates the point(p4) where the bisector("Winkelhalbierende") of the angle(alpha) at p1 cuts the
	 * line p2-p3.
	 * 
	 * TODO: What will happen if some of the given vectors are equal or zero-vectors?
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
	public static Vector2 calculateBisector(IVector2 p1, IVector2 p2, IVector2 p3)
	{
		if (p1.equals(p2) || p1.equals(p3))
		{
			LOG.warn("AIMath#calculateBisector(): some vectors are equal!");
			return new Vector2(p1);
		}
		if (p2.equals(p3))
		{
			return new Vector2(p2);
		}
		Vector2 p1p2 = p2.subtractNew(p1);
		Vector2 p1p3 = p3.subtractNew(p1);
		Vector2 p3p2 = p2.subtractNew(p3);
		
		p3p2.scaleTo(p3p2.getLength2() / (p1p2.getLength2() / p1p3.getLength2() + 1));
		p3p2.add(p3);
		
		return p3p2;
	}
	

	/**
	 * 
	 * Use {@link #intersectionPoint(IVector2, IVector2, IVector2, IVector2)} instead! It does the same!
	 * 
	 * @param p1
	 * @param v1
	 * @param p2
	 * @param v2
	 * @throws MathException If lines are parallel or equal.
	 * @author Malte
	 * 
	 */
	@Deprecated
	public static Vector2 intersectPoint(IVector2 p1, IVector2 v1, IVector2 p2, IVector2 v2) throws MathException
	{
		
		// Slope ("Steigung")
		float m1 = v1.y() / v1.x();
		float m2 = v2.y() / v2.x();
		
		// Y-Intercept ("Y-Achsenabschnitt")
		float yIntercept1 = p1.y() - (p1.x() / v1.x()) * v1.y();
		float yIntercept2 = p2.y() - (p2.x() / v2.x()) * v2.y();
		

		// Direction vectors are equal
		if (m1 == m2) // To Malte: Really float-Equality? (Gero) Malte: Yes!
		{
			// Lines are equal
			if (yIntercept1 == yIntercept2)
			{
				String msg = "AIMath#intersectionPoint: two lines are equal. " + "There are infinite intersection points.";
				LOG.error(msg);
				throw new MathException(msg);
			}
			String msg = "AIMath#intersectionPoint: two lines are parallel. " + "There is no Intersection Point. ";
			LOG.error(msg);
			throw new MathException(msg);
		}
		
		// Direction vector v1 is parallel to y-Axis
		if (v1.x() == 0)
		{
			return new Vector2(p1.x(), yIntercept2 + m2 * p1.x());
		}
		
		// Direction vector v2 is parallel to y-Axis
		if (v2.x() == 0)
		{
			return new Vector2(p2.x(), yIntercept1 + m1 * p2.x());
		}
		

		float x = (yIntercept1 - yIntercept2) / (m2 - m1);
		float y = m1 * x + yIntercept1;
		

		return new Vector2(x, y);
	}
	

	/**
	 * Two lines are given by a support vector <b>v</b> ("Stützvektor") and a direction vector <b>p</b>
	 * ("Richtungsvektor").
	 * This methods calculate the point where these lines intersect.
	 * If lines are parallel or equal, Exeption is thrown!!
	 * 
	 * @param p1
	 * @param v1
	 * @param p2
	 * @param v2
	 * 
	 * @author Malte
	 */
	public static Vector2 intersectionPoint(IVector2 p1, IVector2 v1, IVector2 p2, IVector2 v2) throws MathException
	{
		// Create a matrix
		Matrix m = new Matrix(2, 2);
		m.set(0, 0, v1.x());
		m.set(0, 1, -v2.x());
		m.set(1, 0, v1.y());
		m.set(1, 1, -v2.y());
		
		double[] b = { p2.x() - p1.x(), p2.y() - p1.y() };
		if (m.rank() == 1)
		{
			throw new MathException("Given lines are parallel or equal!");
		}
		
		Matrix bM = new Matrix(2, 1);
		bM.set(0, 0, b[0]);
		bM.set(1, 0, b[1]);
		Matrix solved = m.solve(bM);
		
		float x = (float) (solved.get(0, 0) * v1.x() + p1.x());
		float y = (float) (solved.get(0, 0) * v1.y() + p1.y());
		
		return new Vector2(x, y);
		
	}
	

	/**
	 * This methods calculate the point where two lines (l1, l2) intersect.
	 * If lines are parallel or equal, Exception is thrown.
	 * 
	 * @param l1
	 * @param l2
	 * @throws MathException
	 * @author Malte
	 */
	public static Vector2 intersectionPoint(ILine l1, ILine l2) throws MathException
	{
		return intersectionPoint(l1.supportVector(), l1.directionVector(), l2.supportVector(), l2.directionVector());
		// return intersectPoint(l1.supportVector(), l1.directionVector(), l2.supportVector(), l2.directionVector());
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
	public static float yInterceptOfLine(IVector2 point, float slope)
	{
		return (point.y() - slope * point.x());
	}
	

	/**
	 * Indicates if line intercepts/touches circle
	 * 
	 * @param center of circle
	 * @param radius of circle
	 * @param slope of line
	 * @param offset of line
	 * 
	 * @return true if line intercepts circle
	 * @author ChristianK
	 */
	public static boolean isLineInterceptingCircle(IVector2 center, float radius, float slope, float yIntercept)
	{
		// based on equation of cirle and line
		// trying to intercept leads to a quadratic-equation
		// p-q-equation is used
		// point of interception doesn't matter --> checks only if value in sqrt is >= 0 (i.e. equation is solvable, i.e.
		// is intercepting
		
		float p = ((-2 * center.x()) + (2 * slope * yIntercept) - (2 * center.y() * slope)) / (1 + (slope * slope));
		float q = ((center.x() * center.x()) + (yIntercept * yIntercept) - (2 * center.y() * yIntercept)
				+ (center.y() * center.y()) - (radius * radius))
				/ (1 + (slope * slope));
		
		if (((p * p / 4) - q) >= 0)
		{
			return true; // yepp, is intercepting
		} else
		{
			return false; // nope, not intercepting
		}
	}
	

	/**
	 * gets the sign of a float
	 * @param f
	 * @return -1 if f is negative; 1 else
	 * @author DanielW
	 */
	public static float sign(float f)
	{
		return f < 0 ? -1 : 1;
	}
	

	/**
	 * Returns true if the given number is positive or zero. Else: false
	 * @param f
	 * @author Malte
	 */
	public static boolean isPositive(float f)
	{
		return f >= 0 ? true : false;
	}
	

	/**
	 * Checks if all given values are the same.
	 * If list is empty, it's true!
	 * 
	 * @param values
	 * @author Malte
	 */
	public static boolean allTheSame(boolean... values)
	{
		if (values.length == 0)
		{
			return true;
		}
		boolean ref = values[0];
		for (boolean b : values)
		{
			if (b != ref)
			{
				return false;
			}
		}
		return true;
	}
	

	/**
	 * calculates a point on a line between start and end, that is stepSize away from start
	 * calculation is based on Intercept theorem (Strahlensatz)
	 * 
	 * @param start
	 * @param end
	 * @param stepSize
	 * @author ChristianK
	 */
	public static Vector2 stepAlongLine(IVector2 start, IVector2 end, float stepSize)
	{
		float distance = distancePP(start, end);
		float coefficient = stepSize / distance;
		
		float xDistance = end.x() - start.x();
		float yDistance = end.y() - start.y();
		
		Vector2 result = new Vector2();
		result.x = xDistance * coefficient + start.x();
		result.y = yDistance * coefficient + start.y();
		
		return result;
	}
	

	/**
	 * 
	 * Test if the point is in or on the given circle.
	 * Please use: {@link Circle#isPointInShape(AVector2)} instead.
	 * 
	 * @author Steffen
	 * @author Dion
	 * 
	 */
	@Deprecated
	public static boolean isPointInCircle(Circle circle, Vector2 point)
	{
		point.subtract(circle.center());
		
		if (point.getLength2() <= circle.radius())
		{
			return true;
		}
		return false;
	}
	

	/**
	 * Finds the nearest bot to a given position (p).
	 * 
	 * @param worldframe
	 * @param position
	 * @return
	 *         TODO @author ???
	 */
	public static TrackedBot getNearestBot(List<TrackedBot> bots, Vector2 p)
	{
		float distance = 10000000;
		TrackedBot result = null;
		if (bots.size() < 1)
		{
			LOG.warn("Input list in #getNearestBot has no elements!");
			return null;
		}
		for (TrackedBot bot : bots)
		{
			if (distancePP(bot.pos, p) < distance)
			{
				distance = distancePP(bot.pos, p);
				result = bot;
			}
		}
		return result;
	}
	

	/**
	 * {@link AIMath#p2pVisibility(WorldFrame, IVector2, IVector2, List)}
	 * 
	 * @param wf
	 * @param start
	 * @param end
	 * @param ignoreBotId
	 * @return
	 */
	public static boolean p2pVisibility(WorldFrame wf, IVector2 start, IVector2 end, Integer... ignoreBotId)
	{
		return p2pVisibility(wf, start, end, Arrays.asList(ignoreBotId));
	}
	

	/**
	 * {@link AIMath#p2pVisibility(WorldFrame, IVector2, IVector2, List)}
	 * 
	 * @param wf
	 * @param start
	 * @param end
	 * @param ignoreBotId
	 * @return
	 */
	public static boolean p2pVisibility(WorldFrame wf, IVector2 start, IVector2 end, Float raySize,
			Integer... ignoreBotId)
	{
		return p2pVisibility(wf, start, end, raySize, Arrays.asList(ignoreBotId));
	}
	

	/**
	 * {@link AIMath#p2pVisibility(WorldFrame, IVector2, IVector2, List)}
	 * 
	 * @param wf
	 * @param start
	 * @param end
	 * @param ignoreBotId
	 * @return
	 */
	public static boolean p2pVisibility(WorldFrame wf, IVector2 start, IVector2 end, List<Integer> ignoreIds)
	{
		return p2pVisibility(wf, start, end, 0f, ignoreIds);
	}
	

	/**
	 * Checks if the beam between two points is blocked or not.
	 * 
	 * TODO This methods is one of the most time-consuming methods in the AI. It is really worth spending some time
	 * optimizing it! (Gero)
	 * 
	 * @param wf
	 * @param start
	 * @param end
	 * @param ignoreIds
	 * @return
	 * @author GuntherB
	 */
	public static boolean p2pVisibility(WorldFrame wf, IVector2 start, IVector2 end, Float raySize,
			List<Integer> ignoreIds)
	{
		final float MIN_DIST = AIConfig.getGeometry().getBallRadius() + AIConfig.getGeometry().getBotRadius() + raySize;
		
		// building list of bots to control
		final Map<Integer, TrackedBot> botsToCheck = new HashMap<Integer, TrackedBot>(10);
		botsToCheck.putAll(wf.tigerBots);
		botsToCheck.putAll(wf.foeBots);
		
		for (Integer ignoreId : ignoreIds)
		{
			botsToCheck.remove(ignoreId);
		}
		
		// checking free line
		final float distanceStartEndSquared = AIMath.distancePPSqr(start, end);
		for (TrackedBot bot : botsToCheck.values())
		{
			final float distanceBotStartSquared = AIMath.distancePPSqr(bot.pos, start);
			final float distanceBotEndSquared = AIMath.distancePPSqr(bot.pos, end);
			if (distanceStartEndSquared > distanceBotStartSquared && distanceStartEndSquared > distanceBotEndSquared)
			{
				// only check those bots that possibly can be in between start and end
				float distanceBotLine = AIMath.distancePL(bot.pos, start, end);
				if (distanceBotLine < MIN_DIST)
				{
					return false;
				}
			}
		}
		
		return true;
	}
	

	/**
	 * @param x
	 * @return x^2 (square of x)
	 * @author DanielW
	 */
	public static float square(float x)
	{
		return x * x;
	}
	

	/**
	 * @param x
	 * @return x^3 (cubic of x)
	 * @author DanielW
	 */
	public static float cubic(float x)
	{
		return x * x * x;
	}
	

	/**
	 * Returns the minimum float-value
	 * @param 1 to n float values
	 * @return minimum value
	 * @author DionH
	 */
	public static float min(float... values)
	{
		if (values.length == 0)
			throw new IllegalArgumentException("No values");
		
		float minimum = values[0];
		
		for (float f : values)
		{
			if (f < minimum)
				minimum = f;
		}
		
		return minimum;
	}
	

	/**
	 * Check is number has digits after decimal point.
	 * 
	 * @param number to check
	 * @return true when number has digits after decimal point
	 */
	public static boolean hasDigitsAfterDecimalPoint(float number)
	{
		float numberInt = (float) Math.ceil(number);
		
		if (number == numberInt)
		{
			return false;
		} else
		{
			return true;
		}
	}
	

	/**
	 * Returns the position of the Kicker of a given bot, so it can be aimed at.
	 * Says nothing about the orientation of that kicker!
	 * @author GuntherB
	 */
	public static IVector2 getKickerPosFromBot(WorldFrame wf, int botID)
	{
		final float MID_POINT_TO_KICKER = 59.5f * 1.0f; // nominal distance * factor (from sim testing)
		TrackedBot bot = wf.tigerBots.get(botID);
		
		Vector2f botPos = bot.pos;
		float botRot = bot.angle;
		
		// scaling
		Vector2 kickerPos = new Vector2(MID_POINT_TO_KICKER, 0);
		
		// rotation
		kickerPos.turnTo(botRot);
		
		// reference Position
		
		kickerPos.add(botPos);
		
		return kickerPos;
	}
	
	/**
	 * Convert a bot-local vector to the equivalent global one.
	 * 
	 * @param local Bot-local vector
	 * @param wpAngle Orientation of the bot
	 * @return Properly turned global vector
	 * 
	 * @author AndreR
	 */
	public static Vector2 convertLocalBotVector2Global(IVector2 local, float wpAngle)
	{
		return local.turnNew(-AIMath.PI_HALF + wpAngle);
	}
	
	/**
	 * Convert a global vector to a bot-local one
	 * 
	 * @param global Global vector
	 * @param wpAngle Orientation of the bot
	 * @return Properly turned local vector
	 * 
	 * @author AndreR
	 */
	public static Vector2 convertGlobalBotVector2Local(IVector2 global, float wpAngle)
	{
		return global.turnNew(AIMath.PI_HALF - wpAngle);
	}
}