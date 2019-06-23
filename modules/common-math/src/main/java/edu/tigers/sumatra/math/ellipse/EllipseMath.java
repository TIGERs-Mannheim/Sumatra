/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.ellipse;

import java.util.ArrayList;
import java.util.List;

import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.math.vector.VectorMath;


/**
 * Ellipse related calculations.
 * Please consider using the methods from {@link IEllipse} instead of these static methods!
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public final class EllipseMath
{
	private static final double	MAX_STEP_CURVE			= 5;
	private static final double	POINT_ON_CURVE_TOL	= 5;
	private static final double	STEP_TOLERANCE			= 0.01;
	
	
	@SuppressWarnings("unused")
	private EllipseMath()
	{
	}
	
	
	/**
	 * @param ellipse
	 * @param start
	 * @param step
	 * @return point after step
	 */
	public static IVector2 stepOnCurve(final IEllipse ellipse, final IVector2 start, final double step)
	{
		if (VectorMath.distancePP(start, ellipse.nearestPointOutside(start)) > POINT_ON_CURVE_TOL)
		{
			throw new IllegalArgumentException(
					"The start point is not on the ellipse border. Use nearestPointOutsideCircle to get an appropriate point");
		}
		
		IVector2 curPt = transformToNotTurned(ellipse, start);
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
			
			final IVector2 relStart = curPt.subtractNew(ellipse.center());
			// tangent formula, see Wikipedia, not sure if it is correct for a turned ellipse
			final IVector2 dir = Vector2f.fromXY((-ellipse.getRadiusX() * relStart.y()) / ellipse.getRadiusY(),
					(ellipse.getRadiusY() * relStart.x())
							/ ellipse.getRadiusX());
			final IVector2 tmpP1 = curPt.addNew(dir.scaleToNew(curStep)).add(relStart);
			// ensure that we cross the ellipse border
			final IVector2 tmpP2 = LineMath.stepAlongLine(tmpP1, ellipse.center(), -ellipse.getDiameterMax());
			
			final List<IVector2> intsPts = lineSegmentIntersections(ellipse, Line.fromPoints(ellipse.center(),
					transformToTurned(ellipse, tmpP2)));
			if (intsPts.size() != 1)
			{
				throw new IllegalStateException("Only one intersection point expected, but " + intsPts.size() + " found");
			}
			final IVector2 newP = transformToNotTurned(ellipse, intsPts.get(0));
			
			// actual step
			double actStep = VectorMath.distancePP(curPt, newP);
			nextStep = nextStep - ((curStep > 0) ? actStep : -actStep);
			// if we passed zero
			if (isNegPos(nextStep, curStep) || (actStep < 0.001))
			{
				// exit
				nextStep = 0;
			}
			
			curPt = newP;
			
		} while (Math.abs(nextStep) > STEP_TOLERANCE);
		return transformToTurned(ellipse, curPt);
	}
	
	
	/**
	 * Checks if a is neg and b is pos or wise versa
	 *
	 * @param a
	 * @param b
	 * @return
	 */
	private static boolean isNegPos(final double a, final double b)
	{
		return ((a > 0) && (b < 0)) || ((a < 0) && (b > 0));
	}
	
	
	/**
	 * Main method for getting intersecting points.
	 * Will calc the intersecting points between the two points
	 *
	 * @param ellipse an ellipse
	 * @param line a line
	 * @return intersection points
	 */
	public static List<IVector2> lineIntersections(final IEllipse ellipse, final ILine line)
	{
		return lineIntersectionsGeneric(ellipse, line, true);
	}
	
	
	/**
	 * Main method for getting intersecting points.
	 * Will calc the intersecting points between the two points
	 *
	 * @param ellipse an ellipse
	 * @param line a line
	 * @return intersection points
	 */
	public static List<IVector2> lineSegmentIntersections(final IEllipse ellipse, final ILine line)
	{
		return lineIntersectionsGeneric(ellipse, line, false);
	}
	
	
	private static List<IVector2> lineIntersectionsGeneric(final IEllipse ellipse, final ILine line,
			final boolean endlessLine)
	{
		final List<IVector2> result = new ArrayList<>(2);
		
		final IVector2 p0 = ellipse.center();
		final IVector2 p1 = transformToNotTurned(ellipse, line.getStart());
		final IVector2 p2 = transformToNotTurned(ellipse, line.getEnd());
		
		// using double to avoid inaccurate results. (its fast enough)
		final double rrx = ellipse.getRadiusX() * ellipse.getRadiusX();
		final double rry = ellipse.getRadiusY() * ellipse.getRadiusY();
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
			final double e = SumatraMath.sqrt(d);
			final double u1 = (-b - e) / a;
			final double u2 = (-b + e) / a;
			if (endlessLine || ((0 <= u1) && (u1 <= 1)))
			{
				final IVector2 tmpP = Vector2f.fromXY(p1.x() + (x21 * u1), p1.y() + (y21 * u1));
				result.add(transformToTurned(ellipse, tmpP));
			}
			if (endlessLine || ((0 <= u2) && (u2 <= 1)))
			{
				final IVector2 tmpP = Vector2f.fromXY(p1.x() + (x21 * u2), p1.y() + (y21 * u2));
				result.add(transformToTurned(ellipse, tmpP));
			}
		}
		
		return result;
	}
	
	
	/**
	 * Transform a point that is not turned with the turnAngle of the ellipse
	 * to a turned point
	 *
	 * @param point
	 * @return
	 */
	private static IVector2 transformToTurned(final IEllipse ellipse, final IVector2 point)
	{
		return point.subtractNew(ellipse.center()).turn(ellipse.getTurnAngle()).add(ellipse.center());
	}
	
	
	/**
	 * Transform a turned point (normal incoming point actually) to a non turned
	 * point (needed by some calculations, that do not consider turnAngle
	 *
	 * @param point
	 * @return
	 */
	private static IVector2 transformToNotTurned(final IEllipse ellipse, final IVector2 point)
	{
		return point.subtractNew(ellipse.center()).turn(-ellipse.getTurnAngle()).add(ellipse.center());
	}
}
