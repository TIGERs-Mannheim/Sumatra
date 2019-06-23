/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - Tigers Mannheim
 */
package edu.tigers.sumatra.wp.ball.trajectory.flat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import edu.tigers.sumatra.math.vector.AVector3;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.wp.ball.prediction.IBallTrajectory;
import edu.tigers.sumatra.wp.ball.trajectory.flat.TwoPhaseFixedVelBallTrajectory.TwoPhaseFixedVelParameters;


/**
 * @author AndreR <andre@ryll.cc>
 */
public class TwoPhaseFixedVelBallTrajectoryTest
{
	private static IVector3					kickPos	= Vector3.fromXYZ(0, 0, 0);
	private static IVector3					kickVel	= Vector3.fromXYZ(8000, 0, 0);
	
	private TwoPhaseFixedVelParameters	params;
	private IBallTrajectory					trajectory;
	
	
	@Before
	public void setup()
	{
		params = new TwoPhaseFixedVelParameters(-2800, -800, 2000);
		
		trajectory = TwoPhaseFixedVelBallTrajectory.fromKick(kickPos.getXYVector(), kickVel, params);
	}
	
	
	@Test
	public void testGetPos3ByTime()
	{
		IVector3 pos = trajectory.getPos3ByTime(1.0);
		
		assertEquals(6600, pos.x(), 1e-6);
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
		IVector3 vel = trajectory.getVel3ByTime(1.0);
		
		assertEquals(5.2, vel.x(), 1e-6);
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
		IVector3 acc = trajectory.getAcc3ByTime(1.0);
		
		assertEquals(-2.8, acc.x(), 1e-6);
		assertEquals(0, acc.y(), 1e-6);
		assertEquals(0, acc.z(), 1e-6);
		
		IVector3 accLate = trajectory.getAcc3ByTime(1e6);
		IVector3 accInf = trajectory.getAcc3ByTime(Double.POSITIVE_INFINITY);
		
		assertTrue(Double.isFinite(accInf.x()));
		assertTrue(Double.isFinite(accInf.y()));
		assertTrue(Double.isFinite(accInf.z()));
		
		assertTrue(accLate.isCloseTo(accInf, 1e-6));
		
		assertEquals(-0.8, accInf.x(), 1e-6);
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
			traj = TwoPhaseFixedVelBallTrajectory.fromState(posNow, velNow, params);
			
			double tStop = t + traj.getTimeByVel(0) + tStep;
			
			assertEquals(tTotal, tStop, 1e-3);
			
			IVector2 finalPosNew = traj.getPosByVel(0);
			
			assertTrue(finalPos.isCloseTo(finalPosNew, 1e-3));
		}
	}
}
