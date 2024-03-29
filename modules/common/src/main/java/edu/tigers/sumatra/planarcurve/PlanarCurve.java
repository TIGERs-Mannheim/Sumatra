/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.planarcurve;

import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.line.ILineBase;
import edu.tigers.sumatra.math.rectangle.IRectangle;
import edu.tigers.sumatra.math.vector.IVector2;
import org.apache.commons.lang.Validate;
import org.apache.commons.math3.util.Pair;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * A planar curve is defined by multiple segments of 2D functions up to second order.
 *
 * @author AndreR <andre@ryll.cc>
 */
public class PlanarCurve
{
	private static final double TOLERANCE = 1e-3;
	private final List<PlanarCurveSegment> segments;


	/**
	 * Constructor from list of segments.
	 *
	 * @param segments
	 */
	public PlanarCurve(final List<PlanarCurveSegment> segments)
	{
		this.segments = segments;
	}


	public List<PlanarCurveSegment> getSegments()
	{
		return segments;
	}


	/**
	 * Get end time of this curve.
	 *
	 * @return
	 */
	public double getTEnd()
	{
		return segments.get(segments.size() - 1).getEndTime();
	}


	/**
	 * Get start time of this curve, usually zero.
	 *
	 * @return
	 */
	public double getTStart()
	{
		return segments.get(0).getStartTime();
	}


	/**
	 * Get state (pos, vel, acc) of a curve at a certain time.
	 *
	 * @param t
	 * @return
	 */
	public PlanarCurveState getState(double t)
	{
		for (var segment : segments)
		{
			if (t <= segment.getEndTime())
			{
				double tQuery = t - segment.getStartTime();
				return new PlanarCurveState(segment.getPosition(tQuery), segment.getVelocity(tQuery), segment.getAcc());
			}
		}

		// we came here because t is beyond tEnd, hence we use the last segment
		var lastSegment = segments.get(segments.size() - 1);
		double tQuery = lastSegment.getDuration();

		return new PlanarCurveState(lastSegment.getPosition(tQuery), lastSegment.getVelocity(tQuery),
				lastSegment.getAcc());
	}


	/**
	 * Create a curve from a simple point.
	 *
	 * @param point
	 * @return
	 */
	public static PlanarCurve fromPoint(final IVector2 point)
	{
		List<PlanarCurveSegment> pointList = new ArrayList<>();
		pointList.add(PlanarCurveSegment.fromPoint(point, 0, Double.POSITIVE_INFINITY));

		return new PlanarCurve(pointList);
	}


	/**
	 * Create a planar curve from a position and velocity. Curve is defined until tEnd.
	 *
	 * @param pos
	 * @param vel
	 * @param tEnd
	 * @return
	 */
	public static PlanarCurve fromPositionAndVelocity(final IVector2 pos, final IVector2 vel, final double tEnd)
	{
		List<PlanarCurveSegment> list = new ArrayList<>();
		list.add(PlanarCurveSegment.fromFirstOrder(pos, vel, 0, tEnd));

		return new PlanarCurve(list);
	}


	/**
	 * Create a planar curve from a position, velocity, and acceleration. Curve is defined until tEnd.
	 *
	 * @param pos
	 * @param vel
	 * @param acc
	 * @param tEnd
	 * @return
	 */
	public static PlanarCurve fromPositionVelocityAndAcceleration(final IVector2 pos, final IVector2 vel,
			final IVector2 acc, final double tEnd)
	{
		List<PlanarCurveSegment> list = new ArrayList<>();
		list.add(PlanarCurveSegment.fromSecondOrder(pos, vel, acc, 0, tEnd));

		return new PlanarCurve(list);
	}


	/**
	 * Get position of a curve at a certain time.
	 *
	 * @param t
	 * @return
	 */
	public IVector2 getPos(double t)
	{
		for (var segment : segments)
		{
			if (t <= segment.getEndTime())
			{
				double tQuery = t - segment.getStartTime();
				return segment.getPosition(tQuery);
			}
		}

		// we came here because t is beyond tEnd, hence we use the last segment
		var lastSegment = segments.get(segments.size() - 1);
		double tQuery = lastSegment.getDuration();

		return lastSegment.getPosition(tQuery);
	}


	/**
	 * Get velocity of a curve at a certain time.
	 *
	 * @param t
	 * @return
	 */
	public IVector2 getVel(double t)
	{
		for (var segment : segments)
		{
			if (t <= segment.getEndTime())
			{
				double tQuery = t - segment.getStartTime();
				return segment.getVelocity(tQuery);
			}
		}

		// we came here because t is beyond tEnd, hence we use the last segment
		var lastSegment = segments.get(segments.size() - 1);
		double tQuery = lastSegment.getDuration();

		return lastSegment.getVelocity(tQuery);
	}


	/**
	 * Get acceleration of a curve at a certain time.
	 *
	 * @param t
	 * @return
	 */
	public IVector2 getAcc(double t)
	{
		for (var segment : segments)
		{
			if (t <= segment.getEndTime())
			{
				return segment.getAcc();
			}
		}

		// we came here because t is beyond tEnd, hence we use the last segment
		return segments.get(segments.size() - 1).getAcc();
	}


	/**
	 * Restricts this planar curve from 0 to tEnd. All segments beyond tEnd are removed.
	 * The end time of the last segment is set to tEnd.
	 *
	 * @param tEnd
	 * @return this (for chaining operations)
	 */
	public PlanarCurve restrictToEnd(final double tEnd)
	{
		Validate.isTrue(tEnd > 0);

		segments.removeIf(s -> s.getStartTime() > tEnd);

		Validate.notEmpty(segments);

		segments.get(segments.size() - 1).setEndTime(tEnd);

		return this;
	}


	/**
	 * Restricts this planar curve from tStart to end time. All segments before tStart are removed.
	 * The start time of the first segment is set to tStart.
	 *
	 * @param tStart
	 * @return this (for chaining operations)
	 */
	public PlanarCurve restrictToStart(final double tStart)
	{
		Validate.isTrue(tStart <= segments.get(segments.size() - 1).getEndTime());

		segments.removeIf(s -> s.getEndTime() < tStart);

		Validate.notEmpty(segments);

		segments.set(0, segments.get(0).split(tStart).getSecond());

		return this;
	}


	/**
	 * Calculate minimum distance from this curve to given point.
	 *
	 * @param point [mm]
	 * @return
	 */
	public double getMinimumDistanceToPoint(final IVector2 point)
	{
		return getMinimumDistanceToCurve(PlanarCurve.fromPoint(point));
	}


	/**
	 * Calculate minimum distance of this curve to another curve.
	 *
	 * @param curve
	 * @return
	 */
	public double getMinimumDistanceToCurve(final PlanarCurve curve)
	{
		List<Pair<PlanarCurveSegment, PlanarCurveSegment>> combined = combineCurves(segments, curve.getSegments());

		double min = combined.get(0).getFirst().getPos().distanceTo(combined.get(0).getSecond().getPos());

		for (Pair<PlanarCurveSegment, PlanarCurveSegment> part : combined)
		{
			double tDuration = part.getFirst().getDuration();

			List<Double> roots = new ArrayList<>(minDistanceRoots(part.getFirst(), part.getSecond()));
			roots.add(tDuration);

			double dist = roots.stream().filter(r -> (r > 0) && (r <= (tDuration)))
					.mapToDouble(r -> part.getFirst().getPosition(r).distanceTo(part.getSecond().getPosition(r))).min()
					.orElse(Double.POSITIVE_INFINITY);

			if (dist < min)
			{
				min = dist;
			}
		}
		return min;
	}


	/**
	 * Calculate intersection points of this curve with a v2 line
	 *
	 * @param line
	 * @return
	 */
	public List<IVector2> getIntersectionsWithLine(final ILineBase line)
	{
		return getIntersectionTimesWithLine(line).stream().map(this::getPos).toList();
	}


	/**
	 * Calculate intersection times of this curve with a v2 line.
	 *
	 * @param line some v2 line implementation
	 * @return
	 */
	public List<Double> getIntersectionTimesWithLine(final ILineBase line)
	{
		List<Double> intersectionTimes = new ArrayList<>();

		PlanarCurveSegment lineSegment = PlanarCurveSegment.fromFirstOrder(line.supportVector(), line.directionVector(),
				0, 1.0);

		for (PlanarCurveSegment ourSegment : segments)
		{
			intersectionTimes.addAll(intersectionTimes(ourSegment, lineSegment));
		}
		List<Double> filtered = new ArrayList<>();
		for (var t : intersectionTimes)
		{
			if (filtered.stream().noneMatch(t2 -> SumatraMath.isEqual(t, t2, TOLERANCE))
					&& line.distanceTo(getPos(t)) < TOLERANCE)
			{
				filtered.add(t);
			}
		}
		return filtered;
	}


	/**
	 * Calculate intersection points of this curve with a rectangle.
	 *
	 * @param rect
	 * @return
	 */
	public List<IVector2> getIntersectionsWithRectangle(final IRectangle rect)
	{

		List<IVector2> intersections = new ArrayList<>();

		for (var line : rect.getEdges())
		{
			intersections.addAll(getIntersectionsWithLine(line));
		}

		return intersections;
	}


	/**
	 * Combine two curves and return a list with aligned segment pairs (same start/end time).<br>
	 * Suppress warnings about: high cyclomatic complexity, deeply nested if/else blocks, high cognitive complexity
	 *
	 * @param curveA
	 * @param curveB
	 * @return
	 */
	@SuppressWarnings({ "squid:S3776", "squid:MethodCyclomaticComplexity", "squid:S134" })
	private List<Pair<PlanarCurveSegment, PlanarCurveSegment>> combineCurves(final List<PlanarCurveSegment> curveA,
			final List<PlanarCurveSegment> curveB)
	{
		List<Pair<PlanarCurveSegment, PlanarCurveSegment>> combined = new ArrayList<>();

		Iterator<PlanarCurveSegment> iterA = curveA.iterator();
		Iterator<PlanarCurveSegment> iterB = curveB.iterator();

		PlanarCurveSegment segA = iterA.next();
		PlanarCurveSegment segB = iterB.next();

		boolean finishedA = false;
		boolean finishedB = false;

		while (!finishedA || !finishedB)
		{
			if (segA.getEndTime() <= segB.getEndTime())
			{
				if (finishedA)
				{
					// split A
					Pair<PlanarCurveSegment, PlanarCurveSegment> split = segA.split(segB.getEndTime());
					if (!SumatraMath.isZero(segB.getEndTime() - segB.getStartTime()))
					{
						combined.add(new Pair<>(split.getSecond(), segB));
					}
					segA = split.getSecond();
					if (iterB.hasNext())
					{
						segB = iterB.next();
					} else
					{
						finishedB = true;
					}
				} else
				{
					// split B
					Pair<PlanarCurveSegment, PlanarCurveSegment> split = segB.split(segA.getEndTime());
					if (!SumatraMath.isZero(segA.getEndTime() - segA.getStartTime()))
					{
						combined.add(new Pair<>(segA, split.getFirst()));
					}
					segB = split.getSecond();
					if (iterA.hasNext())
					{
						segA = iterA.next();
					} else
					{
						finishedA = true;
					}
				}
			} else
			{
				if (finishedB)
				{
					// split B
					Pair<PlanarCurveSegment, PlanarCurveSegment> split = segB.split(segA.getEndTime());
					if (!SumatraMath.isZero(segA.getEndTime() - segA.getStartTime()))
					{
						combined.add(new Pair<>(segA, split.getSecond()));
					}
					segB = split.getSecond();
					if (iterA.hasNext())
					{
						segA = iterA.next();
					} else
					{
						finishedA = true;
					}
				} else
				{
					// split A
					Pair<PlanarCurveSegment, PlanarCurveSegment> split = segA.split(segB.getEndTime());
					if (!SumatraMath.isZero(segB.getEndTime() - segB.getStartTime()))
					{
						combined.add(new Pair<>(segB, split.getFirst()));
					}
					segA = split.getSecond();
					if (iterB.hasNext())
					{
						segB = iterB.next();
					} else
					{
						finishedB = true;
					}
				}
			}
		}

		return combined;
	}


	/**
	 * Suppress warnings about: Number of lines in a case statement, cyclomatic complexity
	 *
	 * @param s1
	 * @param s2
	 * @return
	 */
	@SuppressWarnings({ "squid:S1151", "squid:MethodCyclomaticComplexity" })
	private List<Double> minDistanceRoots(final PlanarCurveSegment s1, final PlanarCurveSegment s2)
	{
		return switch (s1.getType())
		{
			case FIRST_ORDER -> switch (s2.getType())
			{
				case FIRST_ORDER -> rootsFirstOrderToFirstOrder(s1, s2);
				case SECOND_ORDER -> rootsFirstOrderToSecOrder(s1, s2);
				default -> rootsPointToFirstOrder(s2, s1);
			};
			case SECOND_ORDER -> switch (s2.getType())
			{
				case FIRST_ORDER -> rootsFirstOrderToSecOrder(s2, s1);
				case SECOND_ORDER -> rootsSecOrderToSecOrder(s1, s2);
				default -> rootsPointToSecOrder(s2, s1);
			};
			default -> switch (s2.getType())
			{
				case FIRST_ORDER -> rootsPointToFirstOrder(s1, s2);
				case SECOND_ORDER -> rootsPointToSecOrder(s1, s2);
				default -> rootsPointToPoint();
			};
		};
	}


	/**
	 * Suppress warnings about: Number of lines in a case statement
	 * Intersect between a segment from our curve with one from their curve
	 *
	 * @param our
	 * @param their
	 * @return
	 */
	@SuppressWarnings("squid:S1151")
	private List<Double> intersectionTimes(final PlanarCurveSegment our, final PlanarCurveSegment their)
	{
		return switch (our.getType())
		{
			case FIRST_ORDER -> switch (their.getType())
			{
				case FIRST_ORDER -> intersectFirstOrderToFirstOrder(our, their);
				case SECOND_ORDER -> intersectSecOrderToFirstOrder(our, their, false).getFirst();
				default -> new ArrayList<>();
			};
			case SECOND_ORDER -> switch (their.getType())
			{
				case FIRST_ORDER -> intersectSecOrderToFirstOrder(their, our, false).getSecond();
				case SECOND_ORDER ->
						throw new UnsupportedOperationException("SecondOrderToSecondOrder intersection not implemented");
				default -> new ArrayList<>();
			};
			default -> new ArrayList<>();
		};
	}


	private List<Double> rootsPointToPoint()
	{
		List<Double> roots = new ArrayList<>();
		roots.add(0.0);
		return roots;
	}


	private List<Double> rootsPointToFirstOrder(final PlanarCurveSegment point, final PlanarCurveSegment firstOrder)
	{
		List<Double> roots = new ArrayList<>();

		double px1 = firstOrder.getPos().x();
		double py1 = firstOrder.getPos().y();
		double vx1 = firstOrder.getVel().x();
		double vy1 = firstOrder.getVel().y();
		double px2 = point.getPos().x();
		double py2 = point.getPos().y();

		double denom = (2 * vx1 * vx1) + (2 * vy1 * vy1);
		if (SumatraMath.isZero(denom))
		{
			roots.add(0.0);
			return roots;
		}

		double tRoot = -((2 * vx1 * (px1 - px2)) + (2 * vy1 * (py1 - py2))) / denom;
		roots.add(tRoot);

		return roots;
	}


	private List<Double> rootsPointToSecOrder(final PlanarCurveSegment point, final PlanarCurveSegment secOrder)
	{
		double px1 = secOrder.getPos().x();
		double py1 = secOrder.getPos().y();
		double vx1 = secOrder.getVel().x();
		double vy1 = secOrder.getVel().y();
		double ax1 = secOrder.getAcc().x();
		double ay1 = secOrder.getAcc().y();
		double px2 = point.getPos().x();
		double py2 = point.getPos().y();

		double a = (ax1 * ax1) + (ay1 * ay1);
		double b = (3 * ax1 * vx1) + (3 * ay1 * vy1);
		double c =
				((((2 * vx1 * vx1) + (2 * vy1 * vy1) + (2 * ax1 * px1)) - (2 * ax1 * px2)) + (2 * ay1 * py1)) - (2 * ay1
						* py2);
		double d = (((2 * px1 * vx1) - (2 * px2 * vx1)) + (2 * py1 * vy1)) - (2 * py2 * vy1);

		return SumatraMath.cubicFunctionRoots(a, b, c, d);
	}


	private List<Double> rootsFirstOrderToFirstOrder(final PlanarCurveSegment c1, final PlanarCurveSegment c2)
	{
		List<Double> roots = new ArrayList<>();

		double px1 = c2.getPos().x();
		double py1 = c2.getPos().y();
		double vx1 = c2.getVel().x();
		double vy1 = c2.getVel().y();
		double px2 = c1.getPos().x();
		double py2 = c1.getPos().y();
		double vx2 = c1.getVel().x();
		double vy2 = c1.getVel().y();

		double denom = (2 * (vx1 - vx2) * (vx1 - vx2)) + (2 * (vy1 - vy2) * (vy1 - vy2));
		if (SumatraMath.isZero(denom))
		{
			roots.add(0.0);
			return roots;
		}

		double tRoot = -((2 * (px1 - px2) * (vx1 - vx2)) + (2 * (py1 - py2) * (vy1 - vy2))) / denom;
		roots.add(tRoot);

		return roots;
	}


	private List<Double> rootsFirstOrderToSecOrder(final PlanarCurveSegment c1, final PlanarCurveSegment c2)
	{
		double px1 = c2.getPos().x();
		double py1 = c2.getPos().y();
		double vx1 = c2.getVel().x();
		double vy1 = c2.getVel().y();
		double ax1 = c2.getAcc().x();
		double ay1 = c2.getAcc().y();
		double px2 = c1.getPos().x();
		double py2 = c1.getPos().y();
		double vx2 = c1.getVel().x();
		double vy2 = c1.getVel().y();

		double a = (ax1 * ax1) + (ay1 * ay1);
		double b = (((3 * ax1 * vx1) - (3 * ax1 * vx2)) + (3 * ay1 * vy1)) - (3 * ay1 * vy2);
		double c =
				(((((((2 * vx1 * vx1) - (4 * vx1 * vx2)) + (2 * vx2 * vx2) + (2 * vy1 * vy1)) - (4 * vy1 * vy2)) + (2 * vy2
						* vy2) + (2 * ax1 * px1)) - (2 * ax1 * px2)) + (2 * ay1 * py1)) - (2 * ay1 * py2);
		double d = ((((2 * px1 * vx1) - (2 * px1 * vx2) - (2 * px2 * vx1)) + (2 * px2 * vx2) + (2 * py1 * vy1)) - (2 * py1
				* vy2) - (2 * py2 * vy1)) + (2 * py2 * vy2);

		return SumatraMath.cubicFunctionRoots(a, b, c, d);
	}


	private List<Double> rootsSecOrderToSecOrder(final PlanarCurveSegment c1, final PlanarCurveSegment c2)
	{
		double px1 = c1.getPos().x();
		double py1 = c1.getPos().y();
		double vx1 = c1.getVel().x();
		double vy1 = c1.getVel().y();
		double ax1 = c1.getAcc().x();
		double ay1 = c1.getAcc().y();
		double px2 = c2.getPos().x();
		double py2 = c2.getPos().y();
		double vx2 = c2.getVel().x();
		double vy2 = c2.getVel().y();
		double ax2 = c2.getAcc().x();
		double ay2 = c2.getAcc().y();

		double a = ((((-ax1 * ax1) + (2 * ax1 * ax2)) - (ax2 * ax2) - (ay1 * ay1)) + (2 * ay1 * ay2)) - (ay2 * ay2);
		double b =
				(((((3 * ax1 * vx2) - (3 * ax1 * vx1)) + (3 * ax2 * vx1)) - (3 * ax2 * vx2) - (3 * ay1 * vy1)) + (3 * ay1
						* vy2) + (3 * ay2 * vy1)) - (3 * ay2 * vy2);
		double c = (
				((((((((-2 * vx1 * vx1) + (4 * vx1 * vx2)) - (2 * vx2 * vx2) - (2 * vy1 * vy1)) + (4 * vy1 * vy2)) - (2
						* vy2 * vy2) - (2 * ax1 * px1)) + (2 * ax1 * px2) + (2 * ax2 * px1)) - (2 * ax2 * px2) - (2 * ay1
						* py1)) + (2 * ay1 * py2) + (2 * ay2 * py1)) - (2 * ay2 * py2));
		double d =
				((((-2 * px1 * vx1) + (2 * px1 * vx2) + (2 * px2 * vx1)) - (2 * px2 * vx2) - (2 * py1 * vy1)) + (2 * py1
						* vy2) + (2 * py2 * vy1)) - (2 * py2 * vy2);

		return SumatraMath.cubicFunctionRoots(a, b, c, d);
	}


	private List<Double> intersectFirstOrderToFirstOrder(final PlanarCurveSegment our, final PlanarCurveSegment their)
	{
		List<Double> intersectionsTime = new ArrayList<>();

		double px1 = our.getPos().x();
		double py1 = our.getPos().y();
		double vx1 = our.getVel().x();
		double vy1 = our.getVel().y();
		double px2 = their.getPos().x();
		double py2 = their.getPos().y();
		double vx2 = their.getVel().x();
		double vy2 = their.getVel().y();

		double denom = (vx2 * vy1) - (vx1 * vy2);
		if (SumatraMath.isZero(denom))
		{
			return intersectionsTime;
		}

		double tOur = ((-px2 * vy2) + (px1 * vy2) + ((py2 - py1) * vx2)) / denom;
		double tOurTotal = our.getDuration();

		if (checkTimes(0, 0, tOur, tOurTotal, true))
		{
			intersectionsTime.add(tOur + our.getStartTime());
		}
		return intersectionsTime;
	}


	/**
	 * @param first First order degree curve
	 * @param sec   Second order degree curve
	 * @return
	 */
	@SuppressWarnings("squid:MethodCyclomaticComplexity")
	private Pair<List<Double>, List<Double>> intersectSecOrderToFirstOrder(final PlanarCurveSegment first,
			final PlanarCurveSegment sec, boolean unboundFirstOrder)
	{
		List<Double> intersectionsFirst = new ArrayList<>();
		List<Double> intersectionsSec = new ArrayList<>();

		double velFirst = first.getVel().getLength2();

		if (SumatraMath.isZero(velFirst))
		{
			return new Pair<>(intersectionsFirst, intersectionsSec);
		}

		double px1 = sec.getPos().x();
		double py1 = sec.getPos().y();
		double vx1 = sec.getVel().x();
		double vy1 = sec.getVel().y();
		double ax1 = sec.getAcc().x();
		double ay1 = sec.getAcc().y();
		double px2 = first.getPos().x();
		double py2 = first.getPos().y();
		double vx2 = first.getVel().x();
		double vy2 = first.getVel().y();

		double a = ((((vx1 * vx1) + (2 * ax1 * px2)) - (2 * ax1 * px1)) * vy2 * vy2) + (
				((((((-2 * ax1 * py2) + (2 * ax1 * py1)) - (2 * ay1 * px2)) + (2 * ay1 * px1)) * vx2) - (2 * vx1 * vx2
						* vy1)) * vy2) + (vx2 * vx2 * vy1 * vy1) + (((2 * ay1 * py2) - (2 * ay1 * py1)) * vx2 * vx2);

		if (a < 0.0)
		{
			return new Pair<>(intersectionsFirst, intersectionsSec);
		}

		double aSqrt = SumatraMath.sqrt(a);

		double denom = (ax1 * vy2) - (ay1 * vx2);
		if (SumatraMath.isZero(denom))
		{
			return new Pair<>(intersectionsFirst, intersectionsSec);
		}

		double tSecA = ((aSqrt - (vx1 * vy2)) + (vx2 * vy1)) / denom;
		double tSecB = -((aSqrt + (vx1 * vy2)) - (vx2 * vy1)) / denom;

		final double tFirstA;
		final double tFirstB;

		if (SumatraMath.isZero(vx2))
		{
			double pySecA = sec.getPosition(tSecA).y();
			double pySecB = sec.getPosition(tSecB).y();
			tFirstA = -(py2 - pySecA) / vy2;
			tFirstB = -(py2 - pySecB) / vy2;
		} else
		{
			double pxSecA = sec.getPosition(tSecA).x();
			double pxSecB = sec.getPosition(tSecB).x();
			tFirstA = -(px2 - pxSecA) / vx2;
			tFirstB = -(px2 - pxSecB) / vx2;
		}

		double tFirstTotal = first.getDuration();
		double tSecTotal = sec.getDuration();

		if (checkTimes(tFirstA, tFirstTotal, tSecA, tSecTotal, unboundFirstOrder))
		{
			intersectionsFirst.add(tFirstA + first.getStartTime());
			intersectionsSec.add(tSecA + sec.getStartTime());
		}

		if (checkTimes(tFirstB, tFirstTotal, tSecB, tSecTotal, unboundFirstOrder))
		{
			intersectionsFirst.add(tFirstB + first.getStartTime());
			intersectionsSec.add(tSecB + sec.getStartTime());
		}

		return new Pair<>(intersectionsFirst, intersectionsSec);
	}


	private boolean checkTimes(double timeA, double durationA, double timeB, double durationB, boolean unboundA)
	{
		boolean isCorrectA;
		boolean isCorrectB;
		if (SumatraMath.isEqual(timeA, durationA) || SumatraMath.isZero(timeA))
		{
			isCorrectA = true;
		} else
		{
			isCorrectA = timeA >= 0 && timeA <= durationA;
		}

		if (SumatraMath.isEqual(timeB, durationB) || SumatraMath.isZero(timeB))
		{
			isCorrectB = true;
		} else
		{
			isCorrectB = timeB >= 0 && timeB <= durationB;
		}

		return (unboundA || isCorrectA) && isCorrectB;
	}
}
