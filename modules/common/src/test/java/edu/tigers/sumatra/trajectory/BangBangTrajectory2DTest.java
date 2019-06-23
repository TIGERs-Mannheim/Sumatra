/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 04.06.2015
 * Author(s): AndreR
 * *********************************************************
 */
package edu.tigers.sumatra.trajectory;

import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Vector2;


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
	private final Random				rng					= new Random();
	
	
	private double getRandomDouble(final double minmax)
	{
		return (rng.nextDouble() - 0.5) * minmax;
	}
	
	
	private IVector2 getRandomVector(final double minmax)
	{
		return new Vector2(getRandomDouble(minmax), getRandomDouble(minmax));
	}
	
	
	private void checkTimeOrder(final BangBangTrajectory1D traj)
	{
		Assert.assertTrue(traj.getPart(0).tEnd >= 0.0);
		Assert.assertTrue(traj.getPart(1).tEnd >= traj.getPart(0).tEnd);
		Assert.assertTrue(traj.getPart(2).tEnd >= traj.getPart(1).tEnd);
		Assert.assertTrue(traj.getPart(3).tEnd >= traj.getPart(2).tEnd);
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
			
			BangBangTrajectory2D traj = new BangBangTrajectory2D(initPos, finalPos, initVel, 3.0, 5.0, 2.0);
			
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
			
			BangBangTrajectory2D traj = new BangBangTrajectory2D(initPos, finalPos, initVel, 3.0, 5.0, 2.0);
			
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
}
