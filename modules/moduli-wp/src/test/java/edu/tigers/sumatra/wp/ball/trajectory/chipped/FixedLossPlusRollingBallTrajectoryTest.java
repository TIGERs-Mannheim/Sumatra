/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - Tigers Mannheim
 */
package edu.tigers.sumatra.wp.ball.trajectory.chipped;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import edu.tigers.sumatra.math.vector.AVector2;
import edu.tigers.sumatra.math.vector.AVector3;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.trajectory.BangBangTrajectory2D;
import edu.tigers.sumatra.wp.ball.prediction.IBallTrajectory;
import edu.tigers.sumatra.wp.ball.trajectory.chipped.FixedLossPlusRollingBallTrajectory.FixedLossPlusRollingParameters;
import junit.framework.Assert;


/**
 * @author AndreR <andre@ryll.cc>
 */
public class FixedLossPlusRollingBallTrajectoryTest
{
	private static IVector3						kickPos	= Vector3.fromXYZ(0, 0, 0);
	private static IVector3						kickVel	= Vector3.fromXYZ(3000, 0, 3000);
	
	private FixedLossPlusRollingParameters	params;
	private IBallTrajectory						trajectory;
	
	
	@Before
	public void setup()
	{
		params = new FixedLossPlusRollingParameters(0.75, 0.6, -400.0, 10, 150);
		
		trajectory = FixedLossPlusRollingBallTrajectory.fromKick(kickPos.getXYVector(), kickVel, params);
	}
	
	
	@Test
	public void testGetPos3ByTime()
	{
		IVector3 posLate = trajectory.getPos3ByTime(1e6);
		IVector3 posInf = trajectory.getPos3ByTime(Double.POSITIVE_INFINITY);
		
		assertTrue(Double.isFinite(posInf.x()));
		assertTrue(Double.isFinite(posInf.y()));
		assertTrue(Double.isFinite(posInf.z()));
		
		assertTrue(posLate.isCloseTo(posInf, 1e-6));
	}
	
	
	@Test
	public void testGetVel3ByTime()
	{
		IVector3 velLate = trajectory.getVel3ByTime(1e6);
		IVector3 velInf = trajectory.getVel3ByTime(Double.POSITIVE_INFINITY);
		
		assertTrue(Double.isFinite(velInf.x()));
		assertTrue(Double.isFinite(velInf.y()));
		assertTrue(Double.isFinite(velInf.z()));
		
		assertTrue(velLate.isCloseTo(velInf, 1e-6));
		
		assertTrue(velInf.isCloseTo(AVector3.ZERO_VECTOR, 1e-6));
	}
	
	
	@Test
	public void testGetAcc3ByTime()
	{
		IVector3 accLate = trajectory.getAcc3ByTime(1e6);
		IVector3 accInf = trajectory.getAcc3ByTime(Double.POSITIVE_INFINITY);
		
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
		IVector2 finalPos = trajectory.getPosByVel(0);
		double tTotal = trajectory.getTimeByVel(0);
		double tStep = tTotal / 10;
		
		IBallTrajectory traj = trajectory;
		
		for (double t = 0; t <= tTotal; t += tStep)
		{
			IVector3 posNow = traj.getPos3ByTime(tStep);
			IVector3 velNow = traj.getVel3ByTime(tStep).multiplyNew(1000.0);
			traj = FixedLossPlusRollingBallTrajectory.fromState(posNow, velNow, params);
			
			IVector2 finalPosNew = traj.getPosByVel(0);
			
			assertTrue(finalPos.isCloseTo(finalPosNew, 1e-3));
		}
	}
	
	
	@Test
	public void testGetMinimumDistanceTrajBall()
	{
		IVector2 initialPos1 = Vector2.fromXY(2, -2);
		IVector2 finalPos1 = Vector2.fromXY(2, 4);
		IVector2 initialVel1 = AVector2.ZERO_VECTOR;
		
		BangBangTrajectory2D traj1 = new BangBangTrajectory2D(initialPos1, finalPos1, initialVel1, 2, 3);
		
		double dist = traj1.getPlanarCurve().getMinimumDistanceToCurve(trajectory.getPlanarCurve());
		double sampleDist = sampleDistMin(traj1, trajectory, 1e-3);
		Assert.assertEquals(sampleDist, dist, 1);
	}
	
	
	private double sampleDistMin(final BangBangTrajectory2D traj, final IBallTrajectory ball, final double dt)
	{
		double min = Double.POSITIVE_INFINITY;
		double tMax = Math.max(traj.getTotalTime(), ball.getTimeByVel(0));
		for (double t = 0; t <= tMax; t += dt)
		{
			double dist = traj.getPositionMM(t).distanceTo(ball.getPosByTime(t));
			if (dist < min)
			{
				min = dist;
			}
		}
		
		return min;
	}
}
