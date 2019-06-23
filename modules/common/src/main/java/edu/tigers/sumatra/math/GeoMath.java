/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 14.09.2011
 * Author(s): stei_ol
 * *********************************************************
 */
package edu.tigers.sumatra.math;

import java.util.List;

import org.apache.log4j.Logger;


/**
 * Helper class for Geometry math problems.
 * 
 * @author osteinbrecher
 */
public final class GeoMath
{
	private static final Logger	log		= Logger.getLogger(GeoMath.class.getName());
														
	private static final double	ACCURACY	= 1e-3;
														
														
	/**
	 * Some Low-Level methods
	 * 
	 * @author KaiE
	 */
	private static class LowLevel
	{
		
		private LowLevel()
		{
			throw new RuntimeException("instance is forbidden");
		}
		
		
		/**
		 * calculates the intersection-coefficient of the first line given as supp1 and dir1 and the second line
		 * build from supp2 and dir2.
		 * 
		 * <pre>
		 * :: Let the following variables be defined as:
		 * s1 = supp1.x
		 * s2 = supp1.y
		 * d1 = dir1.x
		 * d2 = dir1.y
		 * x1 = supp2.x
		 * x2 = supp2.y
		 * r1 = dir2.x
		 * r2 = dir2.y
		 * ::
		 * Basic equations: s1 + lambda*d1 = x1 + gamma*r1
		 *                  s2 + lambda*d2 = x2 + gamma*r2
		 * ==============================================
		 * s1 + lambda*d1 = x1 + gamma*r1
		 * 
		 * s1 - x1 + lambda*d1 = gamma*r1
		 * 
		 * s1 - x1 + lambda*d1
		 * ------------------- = gamma
		 *          r1
		 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ Insert into 2nd dim:
		 * 
		 *                            s1 - x1 + lambda*d1
		 * s2 + lambda * d2 = x2 + (----------------------)*r2
		 *                                     r1
		 * 
		 * 
		 * (s2*r1) + (lambda*d2*r1) = (x2*r1) + (s1*r2) - (x1*r2) + (lambda*d1*r2)
		 * 
		 * with a sharp eye one can notice some determinants of 2d-matrices...
		 * 
		 *  ((r1*s2)-(r2*s1)) - ((r1*x2)-(r2*x1)) = lambda *((d1*r2)-(d2*r1))
		 * 
		 *  ^^^^^^^^^^^^^^^^^    ^^^^^^^^^^^^^^^^           ^^^^^^^^^^^^^^^^^
		 *       detRS               detRX                        detDR
		 * 
		 *  ==> if detDR==0 -> parallel
		 * 
		 *                detRS - detRX
		 *  ==> lambda = ---------------
		 *                    detDR
		 * </pre>
		 * 
		 * @param supp1
		 * @param dir1
		 * @param supp2
		 * @param dir2
		 * @throws RuntimeException if the Lines are parallel or no true lines...
		 * @return the lambda for the first line
		 */
		public static double getLineIntersectionLambda(final IVector2 supp1, final IVector2 dir1, final IVector2 supp2,
				final IVector2 dir2)
		{
			final double s1 = supp1.x();
			final double s2 = supp1.y();
			final double d1 = dir1.x();
			final double d2 = dir1.y();
			
			final double x1 = supp2.x();
			final double x2 = supp2.y();
			final double r1 = dir2.x();
			final double r2 = dir2.y();
			
			
			final double detRS = (r1 * s2) - (r2 * s1);
			final double detRX = (r1 * x2) - (r2 * x1);
			final double detDR = (d1 * r2) - (d2 * r1);
			
			if (Math.abs(detDR) < (ACCURACY * ACCURACY))
			{
				throw new RuntimeException(
						"the two lines are parallel! Should not happen but when it does tell KaiE as this means there might be a bug");
			}
			return (detRS - detRX) / detDR;
		}
		
		
		/**
		 * returns point on line with support-vector s and direction vector d with the given lambda.
		 * solves axpy of vector line function
		 * 
		 * @param s
		 * @param d
		 * @param lambda
		 * @return
		 */
		public static Vector2 getPointOnLineForLambda(final IVector2 s, final IVector2 d, final double lambda)
		{
			
			final double xcut = s.x() + (d.x() * lambda);
			final double ycut = s.y() + (d.y() * lambda);
			return new Vector2(xcut, ycut);
		}
		
		
		/**
		 * checks if the lines are parallel
		 * 
		 * @param s1 support-vector 1
		 * @param d1 direction-vector 1
		 * @param s2 support-vector 2
		 * @param d2 direction-vector 2
		 * @return
		 */
		public static boolean isLineParallel(final IVector2 s1, final IVector2 d1, final IVector2 s2, final IVector2 d2)
		{
			return (Math.abs((d1.x() * d2.y()) - (d2.x() * d1.y())) < (ACCURACY * ACCURACY));
		}
		
		
		/**
		 * checks if the given lambda is within the interval [min,max] with the predefined epsilon.
		 * 
		 * @param lambda
		 * @param min
		 * @param max
		 * @return
		 */
		public static boolean isLambdaInRange(final double lambda, final double min, final double max)
		{
			return (((min - ACCURACY) < lambda) && (lambda < (max + ACCURACY)));
		}
		
		
		/**
		 * calculates the lambda for a point if on the line. Returns NaN when the point was not
		 * part of the line
		 * 
		 * @param point
		 * @param supp
		 * @param dir
		 * @return
		 */
		public static double getLeadPointLambda(final IVector2 point, final IVector2 supp, final IVector2 dir)
		{
			
			final IVector2 ortho = new Vector2(dir.y(), -dir.x());
			if (LowLevel.isLineParallel(supp, dir, point, ortho))
			{
				return 0;
			}
			
			return getLineIntersectionLambda(supp, dir, point, ortho);
		}
		
	}
	
	
	/**
	 * not instantiable
	 */
	private GeoMath()
	{
	}
	
	
	/**
	 * Returns distance between two points
	 * 
	 * @param a
	 * @param b
	 * @return euclidean distance
	 * @author Oliver Steinbrecher <OST1988@aol.com>, Malte Mauelshagen <deineMutter@dlr.de>
	 */
	public static double distancePP(final IVector2 a, final IVector2 b)
	{
		return Math.sqrt(distancePPSqr(a, b));
	}
	
	
	/**
	 * Squared distance between too points
	 * 
	 * @param a
	 * @param b
	 * @return The squared distance between two points
	 */
	public static double distancePPSqr(final IVector2 a, final IVector2 b)
	{
		final double abX = a.x() - b.x();
		final double abY = a.y() - b.y();
		return (abX * abX) + (abY * abY);
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
	public static double distancePL(final IVector2 point, final IVector2 line1, final IVector2 line2)
	{
		return distancePP(point, leadPointOnLine(point, line1, line2));
	}
	
	
	/**
	 * Calculates the distance between a point and a line.
	 * 
	 * @param point
	 * @param line
	 * @return
	 */
	public static double distancePL(final IVector2 point, final ILine line)
	{
		return distancePP(point, leadPointOnLine(point, line));
	}
	
	
	/**
	 * Create the lead point on a straight line (Lot faellen).
	 * 
	 * @param point which should be used to create lead
	 * @param line1 , first point on the line
	 * @param line2 , second point on the line
	 * @return the lead point on the line
	 * @author Oliver Steinbrecher <OST1988@aol.com>
	 */
	public static Vector2 leadPointOnLine(final IVector2 point, final IVector2 line1, final IVector2 line2)
	{
		return leadPointOnLine(point, Line.newLine(line1, line2));
	}
	
	
	/**
	 * Create the lead point on a straight line (Lot faellen).
	 * 
	 * @param point
	 * @param line
	 * @return
	 */
	public static Vector2 leadPointOnLine(final IVector2 point, final ILine line)
	{
		final IVector2 sline = line.supportVector();
		final IVector2 dline = line.directionVector();
		
		
		return LowLevel.getPointOnLineForLambda(sline, dline, LowLevel.getLeadPointLambda(point, sline, dline));
		
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
	public static double angleBetweenXAxisAndLine(final IVector2 p1, final IVector2 p2)
	{
		return angleBetweenXAxisAndLine(Line.newLine(p1, p2));
	}
	
	
	/**
	 * Calculates the angle between x-Axis and a line.<br>
	 * Further details here: {@link edu.tigers.sumatra.math.AVector2#getAngle()}
	 * 
	 * @author Malte
	 * @param l
	 * @return
	 */
	public static double angleBetweenXAxisAndLine(final ILine l)
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
	public static double angleBetweenVectorAndVector(final IVector2 v1, final IVector2 v2)
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
	public static double angleBetweenVectorAndVectorWithNegative(final IVector2 v1, final IVector2 v2)
	{
		// angle between positive x-axis and first vector
		final double angleA = Math.atan2(v1.x(), v1.y());
		// angle between positive x-axis and second vector
		final double angleB = Math.atan2(v2.x(), v2.y());
		// rotation
		double rotation = angleB - angleA;
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
	 * returns the dotted line (see image)
	 * 
	 * <pre>
	 * p2            p3
	 *  \      p4   /
	 *   \----x----/
	 *    \   |   /
	 *     \  |  /
	 *      \^|^/
	 *       \|/<--alpha
	 *       p1
	 * </pre>
	 * 
	 * @param p1 triangle edge
	 * @param p2 triangle edge
	 * @param p3 triangle edge
	 * @param x position in triangle
	 * @return distance
	 * @author DirkK
	 */
	
	public static double triangleDistance(final IVector2 p1, final IVector2 p2, final IVector2 p3, final IVector2 x)
	{
		ILine ball2LeftPost = Line.newLine(p1, p2);
		ILine ball2RightPost = Line.newLine(p1, p3);
		ILine defenseLine = new Line(x, x.subtractNew(p1).turnNew(AngleMath.PI_HALF));
		
		IVector2 defenseLineLeft = x;
		IVector2 defenseLineRight = x;
		try
		{
			defenseLineLeft = GeoMath.intersectionPoint(ball2LeftPost, defenseLine);
			defenseLineRight = GeoMath.intersectionPoint(ball2RightPost, defenseLine);
		} catch (MathException err)
		{
			log.warn("This should not happen!", err);
		}
		
		return GeoMath.distancePP(defenseLineLeft, defenseLineRight);
	}
	
	
	/**
	 * Checks if the given lines are parallel or degenerated
	 * 
	 * @param l1
	 * @param l2
	 * @return
	 */
	public static boolean isLineParallel(final ILine l1, final ILine l2)
	{
		return LowLevel.isLineParallel(l1.supportVector(), l1.directionVector(), l2.supportVector(),
				l2.directionVector());
	}
	
	
	/**
	 * Two line segments (Strecke) are given by two vectors each.
	 * This method calculates the distance between the line segments.
	 * If one or both of the lines are points (both vectors are the same) the distance from the line segment to the point
	 * is calculated
	 * 
	 * @param l1p1
	 * @param l1p2
	 * @param l2p1
	 * @param l2p2
	 * @author Dirk,KaiE
	 * @return
	 * @throws MathException if lines are parallel or equal or one of the vectors is zero
	 */
	public static double incompleteDistanceBetweenLineSegments(final IVector2 l1p1, final IVector2 l1p2,
			final IVector2 l2p1,
			final IVector2 l2p2)
					throws MathException
	{
		
		
		final IVector2 dir1 = l1p2.subtractNew(l1p1);
		final IVector2 dir2 = l2p2.subtractNew(l2p1);
		
		if ((dir1.getLength2() + dir2.getLength2()) < (ACCURACY))
		{
			return distancePP(l1p1, l2p1);
		}
		try
		{
			final double lambda = LowLevel.getLineIntersectionLambda(l1p1, dir1, l2p1, dir2);
			final double delta = LowLevel.getLineIntersectionLambda(l2p1, dir2, l1p1, dir1);
			/**
			 * Segments are intersecting - so the distance is 0
			 */
			if (LowLevel.isLambdaInRange(lambda, 0, 1) && LowLevel.isLambdaInRange(delta, 0, 1))
			{
				return 0;
			}
			final IVector2 closestEndPointToIntersectionA = lambda < (0.5 + ACCURACY) ? l1p1 : l1p2;
			final IVector2 closestEndPointToIntersectionB = delta < (0.5 + ACCURACY) ? l2p1 : l2p2;
			final double lambdaLead = LowLevel.getLeadPointLambda(closestEndPointToIntersectionB, l1p1, dir1);
			final double deltaLead = LowLevel.getLeadPointLambda(closestEndPointToIntersectionA, l2p1, dir2);
			double distanceLineAFromB = 0;
			if (LowLevel.isLambdaInRange(deltaLead, 0, 1))
			{
				distanceLineAFromB = distancePP(closestEndPointToIntersectionA,
						LowLevel.getPointOnLineForLambda(l2p1, dir2, deltaLead));
			} else
			{
				final IVector2 closestEndPointToLead = deltaLead <= (0.5 + ACCURACY) ? l2p1 : l2p2;
				distanceLineAFromB = distancePP(closestEndPointToIntersectionA, closestEndPointToLead);
			}
			double distanceLineBFromA = 0;
			
			if (LowLevel.isLambdaInRange(lambdaLead, 0, 1))
			{
				distanceLineBFromA = distancePP(closestEndPointToIntersectionB,
						LowLevel.getPointOnLineForLambda(l1p1, dir1, lambdaLead));
			} else
			{
				final IVector2 closestEndPointToLead = lambdaLead <= (0.5 + ACCURACY) ? l1p1 : l1p2;
				distanceLineBFromA = distancePP(closestEndPointToIntersectionB, closestEndPointToLead);
			}
			return Math.min(distanceLineAFromB, distanceLineBFromA);
			
		} catch (RuntimeException ex)
		{
			throw new MathException("lines were parallel with direction " + dir1 + " and " + dir2);
		}
		
		
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
	public static double distanceBetweenLineSegments(final IVector2 l1p1, final IVector2 l1p2, final IVector2 l2p1,
			final IVector2 l2p2)
					throws MathException
	{
		// line crossing
		IVector2 lc = null;
		// special cases: one or both lines are points
		if (l1p1.equals(l1p2) && l2p1.equals(l2p2))
		{
			return distancePP(l1p1, l2p1);
		} else if (l1p1.equals(l1p2))
		{
			lc = leadPointOnLine(l1p1, new Line(l2p1, l2p2.subtractNew(l2p1)));
		} else if (l2p1.equals(l2p2))
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
	 * This Method returns the nearest point on the line-segment to a given point. When the line is degenerated
	 * (l1p1=l1p2) the first point is returned. If the lead point of the argument is not on the segment the
	 * nearest edge-point (l1p1,p1p2) is returned.
	 * 
	 * @param l1p1
	 * @param l1p2
	 * @param point
	 * @author Dirk, Felix, KaiE
	 * @return
	 */
	public static IVector2 nearestPointOnLineSegment(final IVector2 l1p1, final IVector2 l1p2, final IVector2 point)
	{
		final IVector2 dir = l1p2.subtractNew(l1p1);
		final double lambda = LowLevel.getLeadPointLambda(point, l1p1, dir);
		if (LowLevel.isLambdaInRange(lambda, 0, 1))
		{
			return LowLevel.getPointOnLineForLambda(l1p1, dir, lambda);
		}
		final double dist1 = distancePPSqr(l1p1, point);
		final double dist2 = distancePPSqr(l1p2, point);
		return dist1 < dist2 ? l1p1 : l1p2;
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
	public static double ratio(final IVector2 root, final IVector2 point1, final IVector2 point2)
	{
		if (point2.equals(root))
		{
			// ratio is inifinite
			return Double.MAX_VALUE;
		}
		return (point1.subtractNew(root).getLength2() / point2.subtractNew(root).getLength2());
	}
	
	
	/**
	 * Two lines are given by a support vector <b>p</b> ("Stuetzvektor") and a direction vector <b>v</b>
	 * ("Richtungsvektor").
	 * This methods calculate the point where these lines intersect.
	 * If lines are parallel or equal or one of the vectors is zero MathException is thrown!!
	 * 
	 * @param p1
	 * @param v1
	 * @param p2
	 * @param v2
	 * @author KaiE
	 * @return
	 * @throws MathException
	 */
	public static Vector2 intersectionPoint(final IVector2 p1, final IVector2 v1, final IVector2 p2, final IVector2 v2)
			throws MathException
	{
		try
		{
			final double lambda = LowLevel.getLineIntersectionLambda(p1, v1, p2, v2);
			return LowLevel.getPointOnLineForLambda(p1, v1, lambda);
		} catch (RuntimeException ex)
		{
			throw new MathException("lines were parallel!");
		}
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
	 * Calculates the intersection point of two paths. Returns null if there is no intersection point.
	 * 
	 * @param p1 vector to the first point of the first path
	 * @param v1 vector from the first point of the first path to the second point of the first path
	 * @param p2 vector to the first point of the second path
	 * @param v2 vector from the first point of the second path to the second point of the second path
	 * @return the intersection point of the two paths if possible, else null
	 */
	public static Vector2 intersectionPointPath(final IVector2 p1, final IVector2 v1, final IVector2 p2,
			final IVector2 v2)
	{
		
		if (LowLevel.isLineParallel(p1, v1, p2, v2))
		{
			return null;
		}
		try
		{
			final double lambda = LowLevel.getLineIntersectionLambda(p1, v1, p2, v2);
			final double delta = LowLevel.getLineIntersectionLambda(p2, v2, p1, v1);
			if (LowLevel.isLambdaInRange(lambda, 0, 1) && LowLevel.isLambdaInRange(delta, 0, 1))
			{
				return LowLevel.getPointOnLineForLambda(p1, v1, lambda);
			}
		} catch (RuntimeException e)
		{
			log.error("Exception with parameter: p1=" + p1 + "v1=" + v1 + "p2=" + p2 + "v2=" + v2, e);
			// no operation
		}
		return null;
		
		
	}
	
	
	/**
	 * Proxy to {@link GeoMath#intersectionPointPath}
	 * 
	 * @param p1p1 first point of the first line
	 * @param p1p2 second point of the first line
	 * @param p2p1 first point of the second line
	 * @param p2p2 second point of the second line
	 * @return intersection of the two paths if exists, null else
	 */
	public static IVector2 intersectionBetweenPaths(final IVector2 p1p1, final IVector2 p1p2, final IVector2 p2p1,
			final IVector2 p2p2)
	{
		IVector2 intersectionPoint = intersectionPointPath(p1p1, p1p2.subtractNew(p1p1), p2p1, p2p2.subtractNew(p2p1));
		return intersectionPoint;
	}
	
	
	/**
	 * calculates the intersection point of a line with a line-segment (path).
	 * 
	 * @param line
	 * @param pPath
	 * @param vPath
	 * @return the intersection point on the path or null if not intersecting
	 */
	public static IVector2 intersectionPointLinePath(final ILine line, final IVector2 pPath,
			final IVector2 vPath)
	{
		final IVector2 pLine = line.supportVector();
		final IVector2 vLine = line.directionVector();
		if (LowLevel.isLineParallel(pLine, vLine, pPath, vPath))
		{
			return null;
		}
		try
		{
			final double lambda = LowLevel.getLineIntersectionLambda(pPath, vPath, pLine, vLine);
			if (LowLevel.isLambdaInRange(lambda, 0, 1))
			{
				return LowLevel.getPointOnLineForLambda(pPath, vPath, lambda);
			}
		} catch (RuntimeException e)
		{
			log.error("Exception with parameter: pLine=" + pLine + "vLine=" + vLine + "pPath=" + pPath + "vPath=" + vPath,
					e);
			// no operation
		}
		return null;
		
	}
	
	
	/**
	 * calculates the intersection of a line with a given half line. The half line starts at the support-vector
	 * and reaches to infinity in direction of the direction-vector.
	 * 
	 * @param line
	 * @param halfLine
	 * @return null if the lines are not intersecting else the point
	 */
	public static IVector2 intersectionPointLineHalfLine(final ILine line, final ILine halfLine)
	{
		if (isLineParallel(line, halfLine))
		{
			return null;
		}
		try
		{
			final IVector2 hLS = halfLine.supportVector();
			final IVector2 hLD = halfLine.directionVector();
			final IVector2 lS = line.supportVector();
			final IVector2 lD = line.directionVector();
			final double lambda = LowLevel.getLineIntersectionLambda(hLS, hLD, lS, lD);
			if (lambda > -(ACCURACY * ACCURACY))
			{
				return LowLevel.getPointOnLineForLambda(hLS, hLD, lambda);
			}
		} catch (RuntimeException ex)
		{
			// catch to avoid runtime exception
			log.error("Exception with parameter: line=" + line + "halfLine=" + halfLine, ex);
		}
		return null;
	}
	
	
	/**
	 * calculates the intersection point of an half line with a given path. If no intersection is found null
	 * is returned
	 * 
	 * @param halfLine
	 * @param pP support point
	 * @param pD direction vector
	 * @return
	 */
	public static IVector2 intersectionPointHalfLinePath(final ILine halfLine, final IVector2 pP, final IVector2 pD)
	{
		if (isLineParallel(new Line(pP, pD), halfLine))
		{
			return null;
		}
		try
		{
			final IVector2 hLS = halfLine.supportVector();
			final IVector2 hLD = halfLine.directionVector();
			final double lambda = LowLevel.getLineIntersectionLambda(hLS, hLD, pP, pD);
			final double delta = LowLevel.getLineIntersectionLambda(pP, pD, hLS, hLD);
			if ((lambda > -(ACCURACY * ACCURACY)) && LowLevel.isLambdaInRange(delta, 0, 1))
			{
				return LowLevel.getPointOnLineForLambda(hLS, hLD, lambda);
			}
		} catch (RuntimeException ex)
		{
			// catch to avoid runtime exception
			log.error("Exception with parameter: path=" + new Line(pP, pD) + "halfLine=" + halfLine, ex);
		}
		return null;
	}
	
	
	/**
	 * Calculates if a Point is on a path specified by the length of the direction vector.
	 * 
	 * @param line
	 * @param point
	 * @return True, if Point on Line
	 * @author SimonS, KaiE
	 */
	public static boolean isPointOnPath(final ILine line, final IVector2 point)
	{
		IVector2 lp = GeoMath.leadPointOnLine(point, line);
		if (GeoMath.distancePP(point, lp) < ACCURACY)
		{
			return isVectorBetween(point, line.supportVector(), line.supportVector().addNew(line.directionVector()));
		}
		return false;
	}
	
	
	/**
	 * Check if is a Vector between min and max.
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
	public static double yInterceptOfLine(final IVector2 point, final double slope)
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
	public static boolean isLineInterceptingCircle(final IVector2 center, final double radius, final double slope,
			final double yIntercept)
	{
		// based on equation of cirle and line
		// trying to intercept leads to a quadratic-equation
		// p-q-equation is used
		// point of interception doesn't matter --> checks only if value in sqrt is >= 0 (i.e. equation is solvable, i.e.
		// is intercepting
		
		final double p = (((-2 * center.x()) + (2 * slope * yIntercept)) - (2 * center.y() * slope))
				/ (1.0 + (slope * slope));
		final double q = (((((center.x() * center.x()) + (yIntercept * yIntercept)) - (2 * center.y() * yIntercept))
				+ (center
						.y() * center.y()))
				- (radius * radius))
				/ (1.0 + (slope * slope));
				
		if ((((p * p) / 4.0) - q) >= 0)
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
	 * @param angle of rotation in radians
	 * @return projected point
	 * @author DanielW
	 */
	public static Vector2 stepAlongCircle(final IVector2 current, final IVector2 center, final double angle)
	{
		/*
		 * x' = (x-u) cos(beta) - (y-v) sin(beta) + u
		 * y' = (x-u) sin(beta) + (y-v) cos(beta) + v
		 */
		final double x = (((current.x() - center.x()) * Math.cos(angle)) - ((current.y() - center.y()) * Math
				.sin(angle))) + center.x();
		final double y = ((current.x() - center.x()) * Math.sin(angle))
				+ ((current.y() - center.y()) * Math.cos(angle)) + center.y();
				
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
	public static double distancePPCircle(final IVector2 center, final IVector2 p1, final IVector2 p2)
	{
		IVector2 c2p1 = p1.subtractNew(center);
		IVector2 c2p2 = p2.subtractNew(center);
		double angle = angleBetweenVectorAndVector(c2p1, c2p2);
		double radius = GeoMath.distancePP(p1, center);
		double u = 2 * radius * AngleMath.PI;
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
	public static Vector2 stepAlongLine(final IVector2 start, final IVector2 end, final double stepSize)
	{
		final Vector2 result = new Vector2();
		
		final double distanceSqr = distancePPSqr(start, end);
		if (distanceSqr == 0)
		{
			result.setX(end.x());
			result.setY(end.y());
			return result;
		}
		
		final double distance = Math.sqrt(distanceSqr);
		final double coefficient = stepSize / distance;
		
		final double xDistance = end.x() - start.x();
		final double yDistance = end.y() - start.y();
		
		
		result.setX((xDistance * coefficient) + start.x());
		result.setY((yDistance * coefficient) + start.y());
		if (Double.isNaN(result.x()) || Double.isNaN(result.y()))
		{
			log.fatal("stepAlongLine: result contains NaNs. Very dangerous!!");
			final String seperator = " / ";
			log.fatal(start.toString() + seperator + end.toString() + seperator + distance + seperator + coefficient
					+ seperator + xDistance + seperator + yDistance + seperator + result.toString());
		}
		return result;
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
		double closestDist = Double.MAX_VALUE;
		for (IVector2 vec : list)
		{
			double dist = GeoMath.distancePPSqr(vec, p);
			if (closestDist > dist)
			{
				closestDist = dist;
				closest = vec;
			}
		}
		return closest;
	}
	
	
	/**
	 * Convert a bot-local vector to the equivalent global one.
	 * 
	 * @param local Bot-local vector
	 * @param wpAngle Orientation of the bot
	 * @return Properly turned global vector
	 * @author AndreR
	 */
	public static Vector2 convertLocalBotVector2Global(final IVector2 local, final double wpAngle)
	{
		return local.turnNew(-AngleMath.PI_HALF + wpAngle);
	}
	
	
	/**
	 * Convert a global vector to a bot-local one
	 * 
	 * @param global Global vector
	 * @param wpAngle Orientation of the bot
	 * @return Properly turned local vector
	 * @author AndreR
	 */
	public static Vector2 convertGlobalBotVector2Local(final IVector2 global, final double wpAngle)
	{
		return global.turnNew(AngleMath.PI_HALF - wpAngle);
	}
	
	
	/**
	 * Convert a global bot angle to a bot-local one
	 * 
	 * @param angle global angle
	 * @return local angle
	 * @author AndreR
	 */
	public static double convertGlobalBotAngle2Local(final double angle)
	{
		return AngleMath.PI_HALF - angle;
	}
	
	
	/**
	 * Convert a local bot angle to a global one
	 * 
	 * @param angle local angle
	 * @return global angle
	 * @author AndreR
	 */
	public static double convertLocalBotAngle2Global(final double angle)
	{
		return -AngleMath.PI_HALF + angle;
	}
	
	
	/**
	 * Calculates the position of the dribbler/kicker depending on bot position and orientation (angle)
	 * 
	 * @param botPos
	 * @param orientation
	 * @param center2Dribbler
	 * @return
	 */
	public static IVector2 getBotKickerPos(final IVector2 botPos, final double orientation, final double center2Dribbler)
	{
		
		return botPos.addNew(new Vector2(orientation).scaleTo(center2Dribbler));
	}
}
