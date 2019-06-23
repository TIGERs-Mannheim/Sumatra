/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.wp.ball.trajectory.chipped;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import edu.tigers.sumatra.math.vector.AVector3;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.wp.ball.trajectory.chipped.FixedLossPlusRollingBallTrajectory.FixedLossPlusRollingParameters;


/**
 * @author AndreR <andre@ryll.cc>
 */
public class FixedLossPlusRollingConsultantTest
{
	private FixedLossPlusRollingParameters	params;
	private FixedLossPlusRollingConsultant	consultant;
	
	
	@Before
	public void setup()
	{
		params = new FixedLossPlusRollingParameters(0.75, 0.6, -400.0, 10, 150);
		consultant = new FixedLossPlusRollingConsultant(params);
	}
	
	
	@Test
	public void testGetInitVelForDistTouchdown()
	{
		for (double distance = 0; distance < 3000; distance += 100)
		{
			for (int numTouchdown = 0; numTouchdown <= 2; numTouchdown++)
			{
				double kickVel = consultant.getInitVelForDistAtTouchdown(distance, numTouchdown) * 1000;
				IVector2 kickVector = consultant.absoluteKickVelToVector(kickVel);
				
				FixedLossPlusRollingBallTrajectory traj = new FixedLossPlusRollingBallTrajectory(AVector3.ZERO_VECTOR,
						Vector3.fromXYZ(kickVector.x(), 0, kickVector.y()), 0, params);
				
				if (traj.getTouchdownLocations().size() > numTouchdown)
				{
					double distToTouchdown = traj.getTouchdownLocations().get(numTouchdown).getLength2();
					
					assertEquals(distance, distToTouchdown, 1e-6);
				}
			}
		}
	}
	
	
	@Test
	public void testGetMinimumDistanceToOverChip()
	{
		// test specific values
		double dist = consultant.getMinimumDistanceToOverChip(3.0, 150);
		assertEquals(dist, 188.89098770912403, 1e-6);
		
		// test unreachable height
		dist = consultant.getMinimumDistanceToOverChip(1.0, 10000);
		assertTrue(Double.isInfinite(dist));
	}
	
	
	@Test
	public void testGetMaximumDistanceToOverChip()
	{
		// test specific values
		double dist = consultant.getMaximumDistanceToOverChip(3.0, 150);
		assertEquals(dist, 728.5402049514264, 1e-6);
		
		// test unreachable height
		dist = consultant.getMaximumDistanceToOverChip(1.0, 10000);
		assertEquals(0, dist, 1e-9);
	}
	
	
	@Test
	public void testGetInitVelForPeakHeight()
	{
		double kickVel = consultant.getInitVelForPeakHeight(200);
		double distMin = consultant.getMinimumDistanceToOverChip(kickVel, 200);
		double distMax = consultant.getMaximumDistanceToOverChip(kickVel, 200);
		assertEquals(0, distMin - distMax, 1e-4);
	}
	
}
