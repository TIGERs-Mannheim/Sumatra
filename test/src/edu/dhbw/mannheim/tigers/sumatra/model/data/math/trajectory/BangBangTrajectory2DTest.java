/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 04.06.2015
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory;

import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;


/**
 * Test 2D bang bang trajectories.
 * 
 * @author AndreR
 */
public class BangBangTrajectory2DTest
{
	private static final int	NUMBER_OF_TESTS	= 1000000;
	private static final float	POS_LIMIT			= 10.0f;
	private static final float	POS_TOLERANCE		= 1e-5f;
	private static final float	VEL_TOLERANCE		= 1e-3f;
	private Random					rng					= new Random();
	
	
	private float getRandomFloat(final float minmax)
	{
		return (rng.nextFloat() - 0.5f) * minmax;
	}
	
	
	private IVector2 getRandomVector(final float minmax)
	{
		return new Vector2(getRandomFloat(minmax), getRandomFloat(minmax));
	}
	
	
	private void checkTimeOrder(final BangBangTrajectory1D traj)
	{
		Assert.assertTrue(traj.getPart(0).tEnd >= 0.0f);
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
			
			BangBangTrajectory2D traj = new BangBangTrajectory2D(initPos, finalPos, initVel, 3.0f, 5.0f, 2.0f);
			
			Assert.assertTrue(traj.getTotalTime() >= 0.0f);
			
			checkTimeOrder(traj.getX());
			checkTimeOrder(traj.getY());
			
			// check final velocity == 0
			Assert.assertEquals(0.0f, traj.getVelocity(traj.getTotalTime()).getLength2(), VEL_TOLERANCE);
			
			// check final position reached
			Assert.assertEquals(finalPos.x(), traj.getPosition(traj.getTotalTime()).x() * 1e-3f, POS_TOLERANCE);
			Assert.assertEquals(finalPos.y(), traj.getPosition(traj.getTotalTime()).y() * 1e-3f, POS_TOLERANCE);
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
			
			BangBangTrajectory2D traj = new BangBangTrajectory2D(initPos, finalPos, initVel, 3.0f, 5.0f, 2.0f);
			
			Assert.assertTrue(traj.getTotalTime() >= 0.0f);
			
			checkTimeOrder(traj.getX());
			checkTimeOrder(traj.getY());
			
			// check final velocity == 0
			Assert.assertEquals(0.0f, traj.getVelocity(traj.getTotalTime()).getLength2(), VEL_TOLERANCE);
			
			// check final position reached
			Assert.assertEquals(finalPos.x(), traj.getPosition(traj.getTotalTime()).x() * 1e-3f, POS_TOLERANCE);
			Assert.assertEquals(finalPos.y(), traj.getPosition(traj.getTotalTime()).y() * 1e-3f, POS_TOLERANCE);
		}
	}
}
