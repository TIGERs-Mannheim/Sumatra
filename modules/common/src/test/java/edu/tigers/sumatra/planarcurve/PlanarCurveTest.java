/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.planarcurve;

import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.trajectory.BangBangTrajectoryFactory;
import edu.tigers.sumatra.trajectory.DestinationForTimedPositionCalc;
import edu.tigers.sumatra.trajectory.ITrajectory;
import edu.tigers.sumatra.trajectory.PlanarCurveFactory;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;


/**
 * Planar curve test
 */
public class PlanarCurveTest
{
	private static final int NUMBER_OF_TESTS = 1000;
	private static final double POS_LIMIT = 10.0;
	private final Random rng = new Random(0);
	private final PlanarCurveFactory planarCurveFactory = new PlanarCurveFactory();
	private final BangBangTrajectoryFactory trajectoryFactory = new BangBangTrajectoryFactory();

	private final DestinationForTimedPositionCalc timedPositionCalc = new DestinationForTimedPositionCalc();


	private double getRandomDouble(final double minmax)
	{
		return (rng.nextDouble() - 0.5) * minmax;
	}


	private IVector2 getRandomVector(final double minmax)
	{
		return Vector2.fromXY(getRandomDouble(minmax), getRandomDouble(minmax));
	}


	@Test
	public void testGetMinimumDistanceSimple()
	{
		IVector2 initialPos = Vector2.fromXY(0, 0);
		IVector2 finalPos = Vector2.fromXY(3, 0);
		IVector2 initialVel = Vector2f.ZERO_VECTOR;

		IVector2 testPoint = Vector2.fromXY(1.5, 1);

		ITrajectory<IVector2> traj = trajectoryFactory.sync(initialPos, finalPos, initialVel, 2, 3);
		double dist = planarCurveFactory.getPlanarCurve(traj).getMinimumDistanceToPoint(testPoint);
		assertThat(dist).isCloseTo(1.0, within(1e-6));
	}


	@Test
	public void testGetMinimumDistanceRandom()
	{
		for (int i = 0; i < NUMBER_OF_TESTS; i++)
		{
			IVector2 initPos = getRandomVector(POS_LIMIT);
			IVector2 finalPos = getRandomVector(POS_LIMIT);
			IVector2 initVel = getRandomVector(2.0);
			IVector2 testPoint = getRandomVector(5.0 * 1e3);

			ITrajectory<IVector2> traj = trajectoryFactory.sync(initPos, finalPos, initVel, 2.0, 3.0);

			double dist = planarCurveFactory.getPlanarCurve(traj).getMinimumDistanceToPoint(testPoint);
			double sampleDist = sampleDistMin(traj, testPoint, 1e-3);
			assertThat(dist).isCloseTo(sampleDist, within(2.0));
		}
	}


	@Test
	public void testGetMinimumDistanceTwoTraj()
	{
		IVector2 initialPos1 = Vector2.fromXY(0, 0);
		IVector2 finalPos1 = Vector2.fromXY(3, 0);
		IVector2 initialVel1 = Vector2f.ZERO_VECTOR;

		IVector2 initialPos2 = Vector2.fromXY(3, -2);
		IVector2 finalPos2 = Vector2.fromXY(3, 1);
		IVector2 initialVel2 = Vector2f.ZERO_VECTOR;

		ITrajectory<IVector2> traj1 = trajectoryFactory.sync(initialPos1, finalPos1, initialVel1, 2, 3);
		ITrajectory<IVector2> traj2 = trajectoryFactory.sync(initialPos2, finalPos2, initialVel2, 2, 3);

		double dist = planarCurveFactory.getPlanarCurve(traj1)
				.getMinimumDistanceToCurve(planarCurveFactory.getPlanarCurve(traj2));
		double sampleDist = sampleDistMin(traj1, traj2, 1e-3);
		assertThat(dist).isCloseTo(sampleDist, within(2.0));
	}


	@Test
	public void testGetMinimumDistanceTwoTrajRandom()
	{
		for (int i = 0; i < NUMBER_OF_TESTS; i++)
		{
			IVector2 initialPos1 = getRandomVector(POS_LIMIT);
			IVector2 finalPos1 = getRandomVector(POS_LIMIT);
			IVector2 initialVel1 = getRandomVector(2.0);

			IVector2 initialPos2 = getRandomVector(POS_LIMIT);
			IVector2 finalPos2 = getRandomVector(POS_LIMIT);
			IVector2 initialVel2 = getRandomVector(2.0);

			ITrajectory<IVector2> traj1 = trajectoryFactory.sync(initialPos1, finalPos1, initialVel1, 2, 3);
			ITrajectory<IVector2> traj2 = trajectoryFactory.sync(initialPos2, finalPos2, initialVel2, 2, 3);

			double dist = planarCurveFactory.getPlanarCurve(traj1)
					.getMinimumDistanceToCurve(planarCurveFactory.getPlanarCurve(traj2));
			double sampleDist = sampleDistMin(traj1, traj2, 1e-3);
			assertThat(dist).isCloseTo(sampleDist, within(2.0));
		}
	}


	@Test
	public void testLineTrajIntersectionSimple()
	{
		IVector2 initialPos1 = Vector2.fromXY(-3, 0);
		IVector2 finalPos1 = Vector2.fromXY(3, 0);
		IVector2 initialVel1 = Vector2.fromXY(0, 0);


		var lineSegment = Lines.segmentFromPoints(Vector2.fromY(2000), Vector2.fromY(-2000));
		var halfLine = Lines.halfLineFromDirection(Vector2.fromY(2000), Vector2.fromY(-1));
		var line = Lines.lineFromDirection(Vector2.fromY(2000), Vector2.fromY(1));

		ITrajectory<IVector2> traj1 = trajectoryFactory.sync(initialPos1, finalPos1, initialVel1, 2, 3);

		List<IVector2> intersections;
		intersections = planarCurveFactory.getPlanarCurve(traj1).getIntersectionsWithLine(lineSegment);
		assertThat(intersections).containsExactlyInAnyOrder(Vector2f.zero());
		intersections = planarCurveFactory.getPlanarCurve(traj1).getIntersectionsWithLine(halfLine);
		assertThat(intersections).containsExactlyInAnyOrder(Vector2f.zero());
		intersections = planarCurveFactory.getPlanarCurve(traj1).getIntersectionsWithLine(line);
		assertThat(intersections).containsExactlyInAnyOrder(Vector2f.zero());
	}


	@Test
	public void testLineTrajIntersectionQuadratic()
	{
		var intersectionPoint = Vector2.fromXY(1, 1);
		var p0 = Vector2.zero();
		var v0 = Vector2.fromX(1);
		// Use overshooting trajectories to the intersection distance. This will create a trajectory that passes the
		// intersection point but continues afterward with the overshoot.
		var destination = timedPositionCalc.destinationForBangBang2dSync(p0, intersectionPoint, v0, 2, 3, 0);

		var lineSegment = Lines.segmentFromPoints(Vector2.fromY(1000), Vector2.fromXY(2000, 1000));
		var halfLine = Lines.halfLineFromDirection(Vector2.fromY(1000), Vector2.fromX(1));
		var line = Lines.lineFromDirection(Vector2.fromY(1000), Vector2.fromX(-1));

		ITrajectory<IVector2> trajectory = trajectoryFactory.sync(p0, destination, v0, 2, 3);

		List<IVector2> intersections;
		var intersectionPointMM = intersectionPoint.multiplyNew(1e3);


		intersections = planarCurveFactory.getPlanarCurve(trajectory).getIntersectionsWithLine(lineSegment);
		assertThat(intersections).hasSize(1);
		assertThat(intersections.get(0).distanceTo(intersectionPointMM)).isLessThanOrEqualTo(1);

		intersections = planarCurveFactory.getPlanarCurve(trajectory).getIntersectionsWithLine(halfLine);
		assertThat(intersections).hasSize(1);
		assertThat(intersections.get(0).distanceTo(intersectionPointMM)).isLessThanOrEqualTo(1);

		intersections = planarCurveFactory.getPlanarCurve(trajectory).getIntersectionsWithLine(line);
		assertThat(intersections).hasSize(1);
		assertThat(intersections.get(0).distanceTo(intersectionPointMM)).isLessThanOrEqualTo(1);
	}


	@Test
	public void testLineTrajIntersectionRandom()
	{
		for (int i = 0; i < NUMBER_OF_TESTS; i++)
		{
			IVector2 initialPos1 = getRandomVector(POS_LIMIT);
			IVector2 finalPos1 = getRandomVector(POS_LIMIT);
			IVector2 initialVel1 = getRandomVector(2.0);

			IVector2 initialPos2 = getRandomVector(POS_LIMIT * 1e3);
			IVector2 finalPos2 = getRandomVector(POS_LIMIT * 1e3);

			var line = Lines.segmentFromPoints(initialPos2, finalPos2);

			ITrajectory<IVector2> traj1 = trajectoryFactory.sync(initialPos1, finalPos1, initialVel1, 2, 3);

			List<IVector2> intersections = planarCurveFactory.getPlanarCurve(traj1).getIntersectionsWithLine(line);

			for (IVector2 inter : intersections)
			{
				double minDistTraj = planarCurveFactory.getPlanarCurve(traj1).getMinimumDistanceToPoint(inter);
				assertThat(minDistTraj).isCloseTo(0.0, within(1e-3));
				assertThat(line.distanceTo(inter)).isLessThan(1e-3);
			}
		}
	}


	private double sampleDistMin(final ITrajectory<IVector2> traj, final IVector2 point, final double dt)
	{
		double min = Double.POSITIVE_INFINITY;
		for (double t = 0; t <= traj.getTotalTime(); t += dt)
		{
			double dist = traj.getPositionMM(t).distanceTo(point);
			if (dist < min)
			{
				min = dist;
			}
		}

		return min;
	}


	private double sampleDistMin(final ITrajectory<IVector2> traj1, final ITrajectory<IVector2> traj2, final double dt)
	{
		double min = Double.POSITIVE_INFINITY;
		double tMax = Math.max(traj1.getTotalTime(), traj2.getTotalTime());
		for (double t = 0; t <= tMax; t += dt)
		{
			double dist = traj1.getPositionMM(t).distanceTo(traj2.getPositionMM(t));
			if (dist < min)
			{
				min = dist;
			}
		}

		return min;
	}
}
