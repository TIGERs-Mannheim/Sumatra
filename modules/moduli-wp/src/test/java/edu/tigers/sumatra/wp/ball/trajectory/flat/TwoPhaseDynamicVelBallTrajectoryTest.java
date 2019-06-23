/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - Tigers Mannheim
 */
package edu.tigers.sumatra.wp.ball.trajectory.flat;

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
import edu.tigers.sumatra.wp.ball.trajectory.flat.TwoPhaseDynamicVelBallTrajectory.TwoPhaseDynamicVelParameters;
import junit.framework.Assert;


/**
 * @author AndreR <andre@ryll.cc>
 */
public class TwoPhaseDynamicVelBallTrajectoryTest
{
	private static IVector3						kickPos	= Vector3.fromXYZ(0, 0, 0);
	private static IVector3						kickVel	= Vector3.fromXYZ(8000, 0, 0);
	
	private TwoPhaseDynamicVelParameters	params;
	private IBallTrajectory						trajectory;
	
	
	@Before
	public void setup()
	{
		params = new TwoPhaseDynamicVelParameters(-3600, -400, 0.62);
		
		trajectory = TwoPhaseDynamicVelBallTrajectory.fromKick(kickPos.getXYVector(), kickVel, params);
		// trajectory = TwoPhaseDynamicVelBallTrajectory.fromState(posNow, velNow, vSwitch, params);
	}
	
	
	@Test
	public void testGetTimeByPos()
	{
		// first test with a trajectory created from kick
		IVector3 kickPos = Vector3.fromXYZ(27, 6, 0);
		IVector3 kickVel = Vector3.fromXYZ(1809.768, -1078.481, 0);
		IBallTrajectory traj = TwoPhaseDynamicVelBallTrajectory.fromKick(kickPos.getXYVector(), kickVel, params);
		
		IVector2 closestDest = Vector2.fromXY(1678.56, -978.201);
		
		double time = traj.getTimeByPos(closestDest);
		assertTrue(time >= 0);
		
		// now test with a trajectory created from state
		double t = 1.52;
		IVector3 posNow = traj.getPos3ByTime(t);
		IVector3 velNow = traj.getVel3ByTime(t).multiplyNew(1e3);
		
		traj = TwoPhaseDynamicVelBallTrajectory.fromState(posNow, velNow, 1306.183, params);
		
		time = traj.getTimeByPos(closestDest);
		assertTrue(time >= 0);
	}
	
	
	@Test
	public void testGetPos3ByTime()
	{
		IVector3 pos = trajectory.getPos3ByTime(0.1);
		
		assertEquals(782.0, pos.x(), 1e-6);
		assertEquals(0, pos.y(), 1e-6);
		assertEquals(0, pos.z(), 1e-6);
		
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
		IVector3 vel = trajectory.getVel3ByTime(0.2);
		
		assertEquals(7.28, vel.x(), 1e-6);
		assertEquals(0, vel.y(), 1e-6);
		assertEquals(0, vel.z(), 1e-6);
		
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
		IVector3 acc = trajectory.getAcc3ByTime(0.1);
		
		assertEquals(params.getAccSlide() * 1e-3, acc.x(), 1e-6);
		assertEquals(0, acc.y(), 1e-6);
		assertEquals(0, acc.z(), 1e-6);
		
		IVector3 accLate = trajectory.getAcc3ByTime(1e6);
		IVector3 accInf = trajectory.getAcc3ByTime(Double.POSITIVE_INFINITY);
		
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
