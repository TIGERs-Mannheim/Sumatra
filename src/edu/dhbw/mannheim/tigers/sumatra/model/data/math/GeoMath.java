/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 14.09.2011
 * Author(s): stei_ol
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.math;

import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import Jama.Matrix;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.exceptions.MathException;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.ICircle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.ellipse.IEllipse;
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
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotIDMap;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EBotType;


/**
 * Helper class for Geometry math problems.
 * 
 * @author osteinbrecher
 * 
 */
public final class GeoMath
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger	log										= Logger.getLogger(GeoMath.class.getName());
	
	/** Matrix X index */
	private static final int		X											= 0;
	/** Matrix X index */
	private static final int		Y											= 1;
	
	/** Senseless Vector. Vector2f(42000,42000). Use it to initialize your vector. */
	public static final IVector2	INIT_VECTOR								= new Vector2f(42000, 42000);
	
	/** Senseless Vector. Vector3f(42000,42000). Use it to initialize your vector. */
	public static final IVector3	INIT_VECTOR3							= new Vector3f(42000, 42000, 42000);
	
	/**  */
	public static final float		ACCURACY									= 0.001f;
	private static final float		APPROX_ORIENT_BALL_DAMP_ACCURACY	= 0.005f;
	
	private static final int		APPROX_ORIENT_BALL_DAMP_MAX_ITER	= 100;
	
	
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
	 * 
	 * @author Oliver Steinbrecher <OST1988@aol.com>, Malte Mauelshagen <deineMutter@dlr.de>
	 */
	public static float distancePP(IVector2 a, IVector2 b)
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
	public static float distancePPSqr(IVector2 a, IVector2 b)
	{
		final float abX = a.x() - b.x();
		final float abY = a.y() - b.y();
		return (abX * abX) + (abY * abY);
	}
	
	
	/**
	 * 
	 * Shortest distance between a tracked object and a point.
	 * @param object
	 * @param point
	 * @return
	 * 
	 */
	public static float distancePP(ATrackedObject object, IVector2 point)
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
	 * 
	 * @author Oliver Steinbrecher <OST1988@aol.com>
	 */
	public static float distancePL(IVector2 point, IVector2 line1, IVector2 line2)
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
	 * 
	 * @author Oliver Steinbrecher <OST1988@aol.com>
	 */
	public static Vector2 leadPointOnLine(IVector2 point, IVector2 line1, IVector2 line2)
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
	 * @param point
	 * @param line
	 * @return
	 */
	public static float distancePL(IVector2 point, ILine line)
	{
		return distancePP(point, leadPointOnLine(point, line));
	}
	
	
	/**
	 * Create the lead point on a straight line (Lot f�llen).
	 * @param point
	 * @param line
	 * @return
	 * 
	 */
	public static Vector2 leadPointOnLine(IVector2 point, ILine line)
	{
		return leadPointOnLine(point, line.supportVector(), line.supportVector().addNew(line.directionVector()));
	}
	
	
	/**
	 * Calculates the angle between x-Axis and a line, given by two points (p1, p2).<br>
	 * Further details {@link GeoMath#angleBetweenVectorAndVector(IVector2, IVector2) here}<br>
	 * 
	 * 
	 * @param p1
	 * @param p2
	 * @author Malte
	 * @return
	 */
	public static float angleBetweenXAxisAndLine(IVector2 p1, IVector2 p2)
	{
		final Line line = new Line();
		line.setPoints(p1, p2);
		return angleBetweenXAxisAndLine(line);
	}
	
	
	/**
	 * Calculates the angle between x-Axis and a line.<br>
	 * Further details here: {@link edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector2#getAngle()}
	 * 
	 * @author Malte
	 * @param l
	 * @return
	 */
	public static float angleBetweenXAxisAndLine(Line l)
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
	public static float angleBetweenVectorAndVector(IVector2 v1, IVector2 v2)
	{
		// The old version was numerically unstable, this one works better
		return Math.abs(angleBetweenVectorAndVectorWithNegative(v1, v2));
	}
	
	
	/**
	 * Calculates the angle between two vectors with respect to the rotation direction.
	 * @see <a href=
	 *      "http://stackoverflow.com/questions/2663570/how-to-calculate-both-positive-and-negative-angle-between-two-lines"
	 *      >how-to-calculate-both-positive-and-negative-angle-between-two-lines</a>
	 * @see <a href= "http://en.wikipedia.org/wiki/Atan2" >Atan2 (wikipedia)</a>
	 * 
	 * @param v1
	 * @param v2
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 * @return angle in rad [-PI,PI]
	 */
	public static float angleBetweenVectorAndVectorWithNegative(IVector2 v1, IVector2 v2)
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
	public static Vector2 calculateBisector(IVector2 p1, IVector2 p2, IVector2 p3)
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
	 * Two lines are given by a support vector <b>p</b> ("Stuetzvektor") and a direction vector <b>v</b>
	 * ("Richtungsvektor").
	 * This methods calculate the point where these lines intersect.
	 * If lines are parallel or equal or one of the vectors is zero Exeption is thrown!!
	 * 
	 * @param p1
	 * @param v1
	 * @param p2
	 * @param v2
	 * 
	 * @author Malte
	 * @return
	 * @throws MathException if lines are parallel or equal or one of the vectors is zero
	 */
	public static Vector2 intersectionPoint(IVector2 p1, IVector2 v1, IVector2 p2, IVector2 v2) throws MathException
	{
		if (v1.equals(Vector2.ZERO_VECTOR))
		{
			throw new MathException("v1 is the zero vector!");
		}
		if (v2.equals(Vector2.ZERO_VECTOR))
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
	public static Vector2 intersectionPoint(ILine l1, ILine l2) throws MathException
	{
		return intersectionPoint(l1.supportVector(), l1.directionVector(), l2.supportVector(), l2.directionVector());
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
		return (point.y() - (slope * point.x()));
	}
	
	
	/**
	 * Indicates if line intercepts/touches circle
	 * 
	 * @param center of circle
	 * @param radius of circle
	 * @param slope of line
	 * @param yIntercept
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
		
		final float p = (((-2 * center.x()) + (2 * slope * yIntercept)) - (2 * center.y() * slope))
				/ (1 + (slope * slope));
		final float q = (((((center.x() * center.x()) + (yIntercept * yIntercept)) - (2 * center.y() * yIntercept)) + (center
				.y() * center.y())) - (radius * radius)) / (1 + (slope * slope));
		
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
	public static Vector2 stepAlongCircle(IVector2 current, IVector2 center, float angle)
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
	 * calculates a point on a line between start and end, that is stepSize away from start
	 * calculation is based on Intercept theorem (Strahlensatz)
	 * 
	 * @param start
	 * @param end
	 * @param stepSize
	 * @author ChristianK
	 * @return
	 */
	public static Vector2 stepAlongLine(IVector2 start, IVector2 end, float stepSize)
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
	public static IVector2 stepAlongEllipse(final IEllipse ellipse, IVector2 start, final float step)
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
	public static List<IVector2> lineCircleIntersections(ILine l, ICircle c)
	{
		return c.lineIntersections(l);
	}
	
	
	/**
	 * Checks if the beam between two points is blocked or not.
	 * 
	 * TODO unassigned This methods is one of the most time-consuming methods in the AI. It is really worth spending some
	 * time optimizing it! (Gero) <br>
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
	public static boolean p2pVisibility(WorldFrame wf, IVector2 start, IVector2 end, Float raySize, List<BotID> ignoreIds)
	
	{
		final float minDistance = AIConfig.getGeometry().getBallRadius() + AIConfig.getGeometry().getBotRadius()
				+ raySize;
		
		// building list of bots to control
		final BotIDMap<TrackedBot> botsToCheck = new BotIDMap<TrackedBot>(12);
		botsToCheck.putAll(wf.tigerBotsVisible);
		botsToCheck.putAll(wf.foeBots);
		
		for (final BotID ignoreId : ignoreIds)
		{
			botsToCheck.remove(ignoreId);
		}
		
		// checking free line
		final float distanceStartEndSquared = distancePPSqr(start, end);
		for (final TrackedBot bot : botsToCheck.values())
		{
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
	 * {@link GeoMath#p2pVisibility(WorldFrame, IVector2, IVector2, List)}
	 * 
	 * @param wf
	 * @param start
	 * @param end
	 * @param ignoreBotId
	 * @return
	 */
	public static boolean p2pVisibility(WorldFrame wf, IVector2 start, IVector2 end, BotID... ignoreBotId)
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
	public static boolean p2pVisibility(WorldFrame wf, IVector2 start, IVector2 end, Float raySize, BotID... ignoreBotId)
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
	public static boolean p2pVisibility(WorldFrame wf, IVector2 start, IVector2 end, List<BotID> ignoreIds)
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
	public static boolean p2pVisibility(WorldFrame wf, IVector2 start, List<IVector2> ends, List<BotID> ignoreIds)
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
	 * calculate a speed vector that results from damping the ball against the
	 * kicker of a bot.
	 * 
	 * @param shootSpeed should be combination of direction and speed (length of vector)
	 * @param incomingSpeedVec vector representing direction and speed (length) of ball
	 * @param botType type of the bot. This is for using the correct config params
	 * @return direction vector the ball will go to; length of vector = speed
	 */
	public static IVector2 ballDamp(IVector2 shootSpeed, IVector2 incomingSpeedVec, EBotType botType)
	{
		float ballDampFactor = AIConfig.getGeneral(botType).getBallDampFactor();
		
		incomingSpeedVec = incomingSpeedVec.turnNew(AngleMath.PI);
		
		float angle = AngleMath
				.normalizeAngle(AngleMath.difference(shootSpeed.getAngle(), incomingSpeedVec.getAngle()) * 2);
		Vector2 outVec = incomingSpeedVec.turnNew(angle).multiplyNew(1 - ballDampFactor);
		IVector2 dampVec = new Vector2(shootSpeed.getAngle()).scaleTo(shootSpeed.getLength2());
		outVec.add(dampVec);
		
		return outVec;
	}
	
	
	/**
	 * Approximate the orientation of the bot that is needed to kick a ball that comes
	 * with incomingSpeedVec in targetAngle direction when kicking with shootSpeed.
	 * 
	 * @param shootSpeed velocity of the kicker
	 * @param incomingSpeedVec vector with direction and speed of the incoming ball
	 * @param botType for parameter selection
	 * @param initialOrientation where to start with approximation, e.g. current bot (target) orientation
	 * @param targetAngle angle of the vector from position of bot to shoot target (should be normalized!)
	 * @return
	 */
	public static float approxOrientationBallDamp(float shootSpeed, IVector2 incomingSpeedVec, EBotType botType,
			float initialOrientation, float targetAngle)
	{
		float destAngle = initialOrientation;
		for (int i = 0; i < APPROX_ORIENT_BALL_DAMP_MAX_ITER; i++)
		{
			IVector2 vShootSpeed = new Vector2(destAngle).scaleTo(shootSpeed);
			IVector2 outVec = ballDamp(vShootSpeed, incomingSpeedVec, botType);
			float diff = targetAngle - outVec.getAngle();
			if (Math.abs(diff) < APPROX_ORIENT_BALL_DAMP_ACCURACY)
			{
				break;
			}
			destAngle = AngleMath.normalizeAngle(destAngle + diff);
		}
		return destAngle;
	}
	
	
	/**
	 * Check if the position in the First, Second, Third, or Fourth Quadrant.
	 * Note: <strong> We are every time in quadrant 2,3 and the foe in 1,4</strong>
	 * @param position to check
	 * @return 1,2,3,4 for the number of the quadrant
	 * 
	 * @author PhilippP (Ph.Posovszky@gmail.com)
	 */
	public static int checkQuadrant(IVector2 position)
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
}
