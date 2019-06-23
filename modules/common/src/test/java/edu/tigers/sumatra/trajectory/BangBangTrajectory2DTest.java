/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.trajectory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.trajectory.BangBangTrajectory1D.BBTrajectoryPart;


/**
 * Test 2D bang bang trajectories.
 * 
 * @author AndreR
 */
public class BangBangTrajectory2DTest
{
	private static final int		NUMBER_OF_TESTS	= 1000000;
	private static final double	POS_LIMIT			= 10.0;
	private static final double	POS_TOLERANCE		= 1e-5f;
	private static final double	VEL_TOLERANCE		= 1e-3f;
	private final Random rng = new Random(0);
	
	
	private double getRandomDouble(final double minmax)
	{
		return (rng.nextDouble() - 0.5) * minmax;
	}
	
	
	private IVector2 getRandomVector(final double minmax)
	{
		return Vector2.fromXY(getRandomDouble(minmax), getRandomDouble(minmax));
	}
	
	
	private void checkTimeOrder(final BangBangTrajectory1D traj)
	{
		double tLast = 0.0;
		for (BBTrajectoryPart part : traj.getParts())
		{
			Assert.assertTrue(part.tEnd >= tLast);
			tLast = part.tEnd;
		}
	}
	
	
	/** */
	@Test
	public void testMonteCarloWithinVMax()
	{
		for (int i = 0; i < NUMBER_OF_TESTS; i++)
		{
			IVector2 initPos = getRandomVector(POS_LIMIT);
			IVector2 finalPos = getRandomVector(POS_LIMIT);
			IVector2 initVel = getRandomVector(2.0f);
			
			BangBangTrajectory2D traj = new BangBangTrajectory2D(initPos, finalPos, initVel, 2.0, 3.0);
			
			Assert.assertTrue(traj.getTotalTime() >= 0.0);
			
			checkTimeOrder(traj.getX());
			checkTimeOrder(traj.getY());
			
			// check final velocity == 0
			Assert.assertEquals(0.0f, traj.getVelocity(traj.getTotalTime()).getLength2(), VEL_TOLERANCE);
			
			// check final position reached
			Assert.assertEquals(finalPos.x(), traj.getPositionMM(traj.getTotalTime()).x() * 1e-3f, POS_TOLERANCE);
			Assert.assertEquals(finalPos.y(), traj.getPositionMM(traj.getTotalTime()).y() * 1e-3f, POS_TOLERANCE);
		}
	}
	
	
	/** */
	@Test
	public void testMonteCarloAboveVMax()
	{
		for (int i = 0; i < NUMBER_OF_TESTS; i++)
		{
			IVector2 initPos = getRandomVector(POS_LIMIT);
			IVector2 finalPos = getRandomVector(POS_LIMIT);
			IVector2 initVel = getRandomVector(4.0f);
			
			BangBangTrajectory2D traj = new BangBangTrajectory2D(initPos, finalPos, initVel, 2.0, 3.0);
			
			Assert.assertTrue(traj.getTotalTime() >= 0.0);
			
			checkTimeOrder(traj.getX());
			checkTimeOrder(traj.getY());
			
			// check final velocity == 0
			Assert.assertEquals(0.0f, traj.getVelocity(traj.getTotalTime()).getLength2(), VEL_TOLERANCE);
			
			// check final position reached
			Assert.assertEquals(finalPos.x(), traj.getPositionMM(traj.getTotalTime()).x() * 1e-3f, POS_TOLERANCE);
			Assert.assertEquals(finalPos.y(), traj.getPositionMM(traj.getTotalTime()).y() * 1e-3f, POS_TOLERANCE);
		}
	}
	
	
	@Test
	public void testAccuracy()
	{
		IVector2 pos = Vector2.fromXY(-677.6483764648438, 0.011869861744344234);
		IVector2 dest = Vector2.fromXY(-186.5, -0.0);
		IVector2 vel1 = Vector2.fromXY(0.09700202941894531, 4.835854306293186E-4);
		IVector2 vel2 = Vector2.fromXY(0.09700202941894531, 0);
		
		BangBangTrajectory2D traj1 = new BangBangTrajectory2D(
				pos.multiplyNew(1e-3f),
				dest.multiplyNew(1e-3f),
				vel1,
				2.0,
				2.0);
		
		BangBangTrajectory2D traj2 = new BangBangTrajectory2D(
				pos.multiplyNew(1e-3f),
				dest.multiplyNew(1e-3f),
				vel2,
				2.0,
				2.0);
		
		assertThat(traj1.getTotalTime()).isCloseTo(traj2.getTotalTime(), within(1e-4));
	}
}
