/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.wp.ball.trajectory.chipped;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import edu.tigers.sumatra.math.vector.IVector;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.math.vector.Vector3f;
import edu.tigers.sumatra.trajectory.BangBangTrajectory2D;
import edu.tigers.sumatra.wp.ball.prediction.IBallTrajectory;
import edu.tigers.sumatra.wp.ball.trajectory.chipped.FixedLossPlusRollingBallTrajectory.FixedLossPlusRollingParameters;


/**
 * @author AndreR <andre@ryll.cc>
 */
public class FixedLossPlusRollingBallTrajectoryTest
{
	private static IVector3 kickPos = Vector3.fromXYZ(0, 0, 0);
	private static IVector3 kickVel = Vector3.fromXYZ(3000, 0, 3000);
	
	private FixedLossPlusRollingParameters params;
	private IBallTrajectory trajectory;
	
	
	@Before
	public void setup()
	{
		params = new FixedLossPlusRollingParameters(0.75, 0.95, 0.6, -400.0, 10, 150);
		
		trajectory = FixedLossPlusRollingBallTrajectory.fromKick(kickPos.getXYVector(), kickVel, 0, params);
	}
	
	
	@Test
	public void testGetPos3ByTime()
	{
		IVector posLate = trajectory.getPosByTime(1e6);
		IVector3 posInf = trajectory.getPosByTime(Double.POSITIVE_INFINITY).getXYZVector();
		
		assertTrue(Double.isFinite(posInf.x()));
		assertTrue(Double.isFinite(posInf.y()));
		assertTrue(Double.isFinite(posInf.z()));
		
		assertTrue(posLate.isCloseTo(posInf, 1e-6));
	}
	
	
	@Test
	public void testGetVel3ByTime()
	{
		IVector velLate = trajectory.getVelByTime(1e6);
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
		IVector accLate = trajectory.getAccByTime(1e6);
		IVector3 accInf = trajectory.getAccByTime(Double.POSITIVE_INFINITY).getXYZVector();
		
		assertTrue(Double.isFinite(accInf.x()));
		assertTrue(Double.isFinite(accInf.y()));
		assertTrue(Double.isFinite(accInf.z()));
		
		assertTrue(accLate.isCloseTo(accInf, 1e-6));
		
		assertEquals(-0.4, accInf.x(), 1e-6);
		assertEquals(0, accInf.y(), 1e-6);
		assertEquals(0, accInf.z(), 1e-6);
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
	public void testGetPosByVel()
	{
		IVector finalPos = trajectory.getPosByVel(0);
		double tTotal = trajectory.getTimeByVel(0);
		double tStep = tTotal / 10;
		
		IBallTrajectory traj = trajectory;
		
		for (double t = 0; t <= tTotal; t += tStep)
		{
			IVector3 posNow = traj.getPosByTime(tStep).getXYZVector();
			IVector3 velNow = traj.getVelByTime(tStep).multiplyNew(1000.0).getXYZVector();
			double spin = traj.getSpinByTime(tStep);
			traj = FixedLossPlusRollingBallTrajectory.fromState(posNow, velNow, spin, params);
			
			IVector finalPosNew = traj.getPosByVel(0);
			
			assertTrue(finalPos.isCloseTo(finalPosNew, 1e-3));
		}
	}
	
	
	@Test
	public void testGetMinimumDistanceTrajBall()
	{
		IVector2 initialPos1 = Vector2.fromXY(2, -2);
		IVector2 finalPos1 = Vector2.fromXY(2, 4);
		IVector2 initialVel1 = Vector2f.ZERO_VECTOR;
		
		BangBangTrajectory2D traj1 = new BangBangTrajectory2D(initialPos1, finalPos1, initialVel1, 2, 3);
		
		double dist = traj1.getPlanarCurve().getMinimumDistanceToCurve(trajectory.getPlanarCurve());
		double sampleDist = sampleDistMin(traj1, trajectory, 1e-3);
		assertEquals(sampleDist, dist, 1);
	}
	
	
	private double sampleDistMin(final BangBangTrajectory2D traj, final IBallTrajectory ball, final double dt)
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
