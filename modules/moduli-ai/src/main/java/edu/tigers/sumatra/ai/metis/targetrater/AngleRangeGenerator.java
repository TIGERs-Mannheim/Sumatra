/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.targetrater;

import edu.tigers.sumatra.geometry.Goal;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.intersections.IIntersections;
import edu.tigers.sumatra.math.line.ILineSegment;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.triangle.TriangleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;


/**
 * Generate covered angles from an arbitrary triangular range
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Value
public class AngleRangeGenerator
{
	ILineSegment lineSegment;


	/**
	 * Create the angle rater based on a goal
	 *
	 * @param goal
	 * @return
	 */
	public static AngleRangeGenerator forGoal(Goal goal)
	{
		return new AngleRangeGenerator(goal.getLineSegment());
	}


	/**
	 * Create the angle rater based on an line segment
	 *
	 * @param lineSegment
	 * @return
	 */
	public static AngleRangeGenerator forLineSegment(ILineSegment lineSegment)
	{
		return new AngleRangeGenerator(lineSegment);
	}


	/**
	 * Generate the covered angles
	 *
	 * @return a list of all ranges covered by obstacles (in the 180° range around the range center)
	 */
	public List<AngleRange> findCoveredAngleRanges(IVector2 origin, List<ICircle> obstacles)
	{
		IVector2 endCenter = TriangleMath.bisector(origin, lineSegment.getPathStart(), lineSegment.getPathEnd());
		IVector2 originToEndCenter = endCenter.subtractNew(origin);

		double maxDistanceToLine = origin.farthestTo(List.of(lineSegment.getPathStart(), lineSegment.getPathEnd()))
				.distanceTo(origin);

		if (obstacles.stream().anyMatch(c -> c.isPointInShape(origin)))
		{
			return List.of(AngleRange.width(AngleMath.PI_TWO));
		}

		return obstacles.stream()
				.map(circle -> createAngleRange(origin, originToEndCenter, circle, maxDistanceToLine))
				.flatMap(Optional::stream)
				.toList();
	}


	private Optional<AngleRange> createAngleRange(IVector2 origin, IVector2 originToEndCenter, ICircle obstacle,
			double maxDistanceToLine)
	{
		if (origin.distanceTo(obstacle.center()) > maxDistanceToLine + obstacle.radius())
		{
			return Optional.empty();
		}

		var intersections = obstacle.intersect(lineSegment);
		var tangentialIntersections = obstacle.tangentialIntersections(origin);
		if (intersections.isEmpty())
		{
			return createRange(origin, originToEndCenter, tangentialIntersections);
		}
		var filteredTangentialIntersections = tangentialIntersections.stream()
				.map(tangentialIntersection -> filterTangentialIntersection(origin, tangentialIntersection, intersections))
				.toList();
		return createRange(origin, originToEndCenter, filteredTangentialIntersections);
	}


	private IVector2 filterTangentialIntersection(IVector2 origin, IVector2 tangentialIntersection,
			IIntersections obstacleLineIntersections)
	{
		if (Lines.segmentFromPoints(origin, tangentialIntersection).intersect(lineSegment).isEmpty())
		{
			return tangentialIntersection;
		}
		return tangentialIntersection.nearestTo(obstacleLineIntersections.asList());
	}


	private Optional<AngleRange> createRange(IVector2 origin, IVector2 originToEndCenter, List<IVector2> intersections)
	{
		return Optional.of(AngleRange.fromAngles(
				relativeAngle(origin, originToEndCenter, intersections.get(0)),
				relativeAngle(origin, originToEndCenter, intersections.get(1))
		)).filter(r -> r.getWidth() > 0 && r.getWidth() < AngleMath.DEG_180_IN_RAD);
	}


	private double relativeAngle(IVector2 origin, IVector2 startToEndCenter, IVector2 point)
	{
		IVector2 originToPoint = point.subtractNew(origin);
		return startToEndCenter.angleTo(originToPoint).orElse(AngleMath.DEG_180_IN_RAD);
	}


	/**
	 * Generate all uncovered angle ranges
	 *
	 * @return all uncovered angle ranges
	 */
	public List<AngleRange> findUncoveredAngleRanges(IVector2 origin, List<ICircle> obstacles)
	{
		return findUncoveredAngleRanges(findCoveredAngleRanges(origin, obstacles), getAngleRange(origin));
	}


	/**
	 * Generate all uncovered angle ranges
	 *
	 * @param coveredAngles
	 * @param fullRange
	 * @return
	 */
	public List<AngleRange> findUncoveredAngleRanges(final List<AngleRange> coveredAngles, final AngleRange fullRange)
	{
		var sortedCoveredAngles = coveredAngles.stream()
				.sorted(Comparator.comparingDouble(AngleRange::getRight))
				.toList();

		List<AngleRange> uncoveredAngles = new ArrayList<>();
		uncoveredAngles.add(fullRange);

		for (AngleRange r : sortedCoveredAngles)
		{
			List<AngleRange> newUncoveredAngles = new ArrayList<>();
			for (AngleRange c : uncoveredAngles)
			{
				newUncoveredAngles.addAll(c.cutOutRange(r));
			}
			uncoveredAngles = newUncoveredAngles;
		}

		return uncoveredAngles;
	}


	/**
	 * @return the full angle range to be considered
	 */
	public AngleRange getAngleRange(IVector2 origin)
	{
		IVector2 origin2Start = lineSegment.getPathStart().subtractNew(origin);
		IVector2 origin2End = lineSegment.getPathEnd().subtractNew(origin);
		double width = origin2End.angleToAbs(origin2Start).orElse(0.0);
		return AngleRange.width(width);
	}


	public Optional<IVector2> getPoint(IVector2 origin, double angle)
	{
		IVector2 endCenter = TriangleMath.bisector(origin, lineSegment.getPathStart(), lineSegment.getPathEnd());
		double baseAngle = endCenter.subtractNew(origin).getAngle();
		return lineSegment.intersect(Lines.lineFromDirection(origin, Vector2.fromAngle(baseAngle + angle))).asOptional();
	}
}
