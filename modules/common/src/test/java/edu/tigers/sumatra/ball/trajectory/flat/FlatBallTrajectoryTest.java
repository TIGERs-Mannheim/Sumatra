/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ball.trajectory.flat;

import edu.tigers.sumatra.ball.BallParameters;
import edu.tigers.sumatra.ball.trajectory.IBallTrajectory;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.math.vector.Vector3f;
import edu.tigers.sumatra.trajectory.BangBangTrajectoryFactory;
import edu.tigers.sumatra.trajectory.ITrajectory;
import edu.tigers.sumatra.trajectory.PlanarCurveFactory;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * @author AndreR <andre@ryll.cc>
 */
public class FlatBallTrajectoryTest
{
	private final BangBangTrajectoryFactory trajectoryFactory = new BangBangTrajectoryFactory();
	private final PlanarCurveFactory planarCurveFactory = new PlanarCurveFactory();

	private static IVector2 kickPos = Vector2.fromXY(0, 0);
	private static IVector2 kickVel = Vector2.fromXY(8000, 0);

	private BallParameters params;
	private IBallTrajectory trajectory;


	@Before
	public void setup()
	{
		params = BallParameters.builder()
				.withBallRadius(21.5)
				.withAccSlide(-3600)
				.withAccRoll(-400)
				.withInertiaDistribution(0.61)
				.withChipDampingXYFirstHop(0.75)
				.withChipDampingXYOtherHops(0.95)
				.withChipDampingZ(0.5)
				.withMinHopHeight(10)
				.withMaxInterceptableHeight(150)
				.build();

		trajectory = FlatBallTrajectory.fromKick(params, kickPos, kickVel, Vector2f.ZERO_VECTOR);
	}


	@Test
	public void testGetTimeByPos()
	{
		// first test with a trajectory created from kick
		IVector2 kickPos = Vector2.fromXY(27, 6);
		IVector2 kickVel = Vector2.fromXY(1809.768, -1078.481);
		IBallTrajectory traj = FlatBallTrajectory.fromKick(params, kickPos, kickVel, Vector2f.ZERO_VECTOR);

		IVector2 closestDest = Vector2.fromXY(1678.56, -978.201);

		double time = traj.getTimeByPos(closestDest);
		assertTrue(time >= 0);

		// now test with a trajectory created from state
		double t = 1.52;
		IVector2 posNow = traj.getPosByTime(t).getXYVector();
		IVector2 velNow = traj.getVelByTime(t).multiplyNew(1e3).getXYVector();
		IVector2 spinNow = traj.getSpinByTime(t);

		traj = FlatBallTrajectory.fromState(params, posNow, velNow, spinNow);

		time = traj.getTimeByPos(closestDest);
		assertTrue(time >= 0);
	}


	@Test
	public void testGetPos3ByTime()
	{
		IVector3 pos = trajectory.getPosByTime(0.1).getXYZVector();

		assertEquals(782.0, pos.x(), 1e-6);
		assertEquals(0, pos.y(), 1e-6);
		assertEquals(0, pos.z(), 1e-6);

		IVector3 posLate = trajectory.getPosByTime(1e6).getXYZVector();
		IVector3 posInf = trajectory.getPosByTime(Double.POSITIVE_INFINITY).getXYZVector();

		assertTrue(Double.isFinite(posInf.x()));
		assertTrue(Double.isFinite(posInf.y()));
		assertTrue(Double.isFinite(posInf.z()));

		assertTrue(posLate.isCloseTo(posInf, 1e-6));
	}


	@Test
	public void testGetVel3ByTime()
	{
		IVector3 vel = trajectory.getVelByTime(0.2).getXYZVector();

		assertEquals(7.28, vel.x(), 1e-6);
		assertEquals(0, vel.y(), 1e-6);
		assertEquals(0, vel.z(), 1e-6);

		IVector3 velLate = trajectory.getVelByTime(1e6).getXYZVector();
		IVector3 velInf = trajectory.getVelByTime(Double.POSITIVE_INFINITY).getXYZVector();

		assertTrue(Double.isFinite(velInf.x()));
		assertTrue(Double.isFinite(velInf.y()));
		assertTrue(Double.isFinite(velInf.z()));

		assertTrue(velLate.isCloseTo(velInf, 1e-6));

		assertTrue(velInf.isCloseTo(Vector3f.ZERO_VECTOR, 1e-6));
	}

	@Test
	public void testGetAcc3ByTime()
	{
		IVector3 acc = trajectory.getAccByTime(0.1).getXYZVector();

		assertEquals(params.getAccSlide() * 1e-3, acc.x(), 1e-6);
		assertEquals(0, acc.y(), 1e-6);
		assertEquals(0, acc.z(), 1e-6);

		IVector3 accLate = trajectory.getAccByTime(1e6).getXYZVector();
		IVector3 accInf = trajectory.getAccByTime(Double.POSITIVE_INFINITY).getXYZVector();

		assertTrue(Double.isFinite(accInf.x()));
		assertTrue(Double.isFinite(accInf.y()));
		assertTrue(Double.isFinite(accInf.z()));

		assertTrue(accLate.isCloseTo(accInf, 1e-6));

		assertEquals(params.getAccRoll() * 1e-3, accInf.x(), 1e-6);
		assertEquals(0, accInf.y(), 1e-6);
		assertEquals(0, accInf.z(), 1e-6);
	}


	@Test
	public void testGetTimeByFixDist()
	{
		double time = trajectory.getTimeByDist(1000);

		assertTrue(time > 0);
	}


	@Test
	public void testGetTimeByDist()
	{
		double tTotal = trajectory.getTimeByVel(0);
		double dTotal = trajectory.getDistByTime(tTotal);

		double dStep = dTotal / 10;

		double tLast = -1;
		for (double d = 0; d <= dTotal; d += dStep)
		{
			double t = trajectory.getTimeByDist(d);
			assertTrue(t > tLast);
			tLast = t;
		}
	}


	@Test
	public void testGetMinimumDistanceTrajBall()
	{
		IVector2 initialPos1 = Vector2.fromXY(2, -2);
		IVector2 finalPos1 = Vector2.fromXY(2, 4);
		IVector2 initialVel1 = Vector2f.ZERO_VECTOR;

		ITrajectory<IVector2> traj1 = trajectoryFactory.sync(initialPos1, finalPos1, initialVel1, 2, 3);

		double dist = planarCurveFactory.getPlanarCurve(traj1).getMinimumDistanceToCurve(trajectory.getPlanarCurve());
		double sampleDist = sampleDistMin(traj1, trajectory, 1e-3);
		assertEquals(sampleDist, dist, 1);
	}


	private double sampleDistMin(final ITrajectory<IVector2> traj, final IBallTrajectory ball, final double dt)
	{
		double min = Double.POSITIVE_INFINITY;
		double tMax = Math.max(traj.getTotalTime(), ball.getTimeByVel(0));
		for (double t = 0; t <= tMax; t += dt)
		{
			double dist = traj.getPositionMM(t).distanceTo(ball.getPosByTime(t).getXYVector());
			if (dist < min)
			{
				min = dist;
			}
		}

		return min;
	}
}
