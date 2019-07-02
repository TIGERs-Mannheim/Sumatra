/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.planarcurve;

import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.trajectory.BangBangTrajectory2D;
import org.junit.Test;

import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;


/**
 * @author AndreR <andre@ryll.cc>
 */
public class PlanarCurveTest
{
	private static final int NUMBER_OF_TESTS = 10000;
	private static final double POS_LIMIT = 10.0;
	private final Random rng = new Random(0);
	
	
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
		
		BangBangTrajectory2D traj = new BangBangTrajectory2D(initialPos, finalPos, initialVel, 2, 3);
		double dist = traj.getPlanarCurve().getMinimumDistanceToPoint(testPoint);
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
			
			BangBangTrajectory2D traj = new BangBangTrajectory2D(initPos, finalPos, initVel, 2.0, 3.0);
			
			double dist = traj.getPlanarCurve().getMinimumDistanceToPoint(testPoint);
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
		
		BangBangTrajectory2D traj1 = new BangBangTrajectory2D(initialPos1, finalPos1, initialVel1, 2, 3);
		BangBangTrajectory2D traj2 = new BangBangTrajectory2D(initialPos2, finalPos2, initialVel2, 2, 3);
		
		double dist = traj1.getPlanarCurve().getMinimumDistanceToCurve(traj2.getPlanarCurve());
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
			
			BangBangTrajectory2D traj1 = new BangBangTrajectory2D(initialPos1, finalPos1, initialVel1, 2, 3);
			BangBangTrajectory2D traj2 = new BangBangTrajectory2D(initialPos2, finalPos2, initialVel2, 2, 3);
			
			double dist = traj1.getPlanarCurve().getMinimumDistanceToCurve(traj2.getPlanarCurve());
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
		
		IVector2 initialPos2 = Vector2.fromXY(0, -2000);
		IVector2 finalPos2 = Vector2.fromXY(0, 2000);
		
		ILine line = Line.fromPoints(initialPos2, finalPos2);
		
		BangBangTrajectory2D traj1 = new BangBangTrajectory2D(initialPos1, finalPos1, initialVel1, 2, 3);
		
		List<IVector2> intersections = traj1.getPlanarCurve().getIntersectionsWithLineSegment(line);
		assertThat(intersections).containsExactlyInAnyOrder(Vector2f.zero());
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
			
			ILine line = Line.fromPoints(initialPos2, finalPos2);
			
			BangBangTrajectory2D traj1 = new BangBangTrajectory2D(initialPos1, finalPos1, initialVel1, 2, 3);
			
			List<IVector2> intersections = traj1.getPlanarCurve().getIntersectionsWithLineSegment(line);
			
			for (IVector2 inter : intersections)
			{
				double minDistTraj = traj1.getPlanarCurve().getMinimumDistanceToPoint(inter);
				assertThat(minDistTraj).isCloseTo(0.0, within(1e-3));
				assertThat(line.isPointOnLineSegment(inter, 1e-3)).isTrue();
			}
		}
	}
	
	
	private double sampleDistMin(final BangBangTrajectory2D traj, final IVector2 point, final double dt)
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
	
	
	private double sampleDistMin(final BangBangTrajectory2D traj1, final BangBangTrajectory2D traj2, final double dt)
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
