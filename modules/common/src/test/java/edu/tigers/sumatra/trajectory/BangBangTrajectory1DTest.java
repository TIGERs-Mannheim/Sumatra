/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.trajectory;

import org.junit.Assert;
import org.junit.Test;


/**
 * Test 1D bang bang trajectories.
 */
public class BangBangTrajectory1DTest
{
	private final BangBangTrajectoryFactory trajectoryFactory = new BangBangTrajectoryFactory();


	private void checkTimeOrder(final BangBangTrajectory1D traj)
	{
		double tLast = 0.0;
		for (int i = 0; i < traj.numParts; i++)
		{
			Assert.assertTrue(traj.parts[i].tEnd >= tLast);
			tLast = traj.parts[i].tEnd;
		}
	}


	private void checkVelocity(final BangBangTrajectory1D traj, final double vMax)
	{
		for (BBTrajectoryPart part : traj.parts)
		{
			Assert.assertTrue(Math.abs(part.v0) <= vMax);
		}
	}


	@Test
	public void testCaseA()
	{
		var traj = trajectoryFactory.singleDim(0f, 0.5f, 0.5f, 2, 3);

		checkTimeOrder(traj);

		// check number of used elements is 2
		Assert.assertEquals(2, traj.numParts);

		// check final position is reached
		Assert.assertEquals(traj.getPosition(traj.getTotalTime()), 0.5, 1e-6f);

		// check final velocity is zero
		Assert.assertEquals(traj.getVelocity(traj.getTotalTime()), 0.0, 1e-6f);

		// check accelerations for this case
		Assert.assertEquals(3.0, traj.parts[0].acc, 0.0);
		Assert.assertEquals(traj.parts[1].acc, -3.0f, 0.0);

		// check if max velocity is exceeded
		checkVelocity(traj, 2.0);
	}


	@Test
	public void testCaseB()
	{
		var traj = trajectoryFactory.singleDim(0f, 2.0f, 0.5f, 2, 3);

		checkTimeOrder(traj);

		// check number of used elements is 3
		Assert.assertEquals(3, traj.numParts);

		// check final position is reached
		Assert.assertEquals(traj.getPosition(traj.getTotalTime()), 2.0, 1e-6f);

		// check final velocity is zero
		Assert.assertEquals(traj.getVelocity(traj.getTotalTime()), 0.0, 1e-6f);

		// check accelerations for this case
		Assert.assertEquals(3.0, traj.parts[0].acc, 0.0);
		Assert.assertEquals(0.0, traj.parts[1].acc, 0.0);
		Assert.assertEquals(traj.parts[2].acc, -3.0, 0.0);

		// check if max velocity is exceeded
		checkVelocity(traj, 2.0);
	}


	@Test
	public void testCaseC()
	{
		var traj = trajectoryFactory.singleDim(0f, 2.0f, 2.5f, 2, 3);

		checkTimeOrder(traj);

		// check number of used elements is 3
		Assert.assertEquals(3, traj.numParts);

		// check final position is reached
		Assert.assertEquals(traj.getPosition(traj.getTotalTime()), 2.0, 1e-6f);

		// check final velocity is zero
		Assert.assertEquals(traj.getVelocity(traj.getTotalTime()), 0.0, 1e-6f);

		// check accelerations for this case
		Assert.assertEquals(traj.parts[0].acc, -3.0f, 0.0);
		Assert.assertEquals(0.0, traj.parts[1].acc, 0.0);
		Assert.assertEquals(traj.parts[2].acc, -3.0f, 0.0);
	}


	@Test
	public void testCaseD()
	{
		var traj = trajectoryFactory.singleDim(0f, 0.5f, -1.0f, 2, 3);

		checkTimeOrder(traj);

		// check number of used elements is 2
		Assert.assertEquals(2, traj.numParts);

		// check final position is reached
		Assert.assertEquals(traj.getPosition(traj.getTotalTime()), 0.5, 1e-6f);

		// check final velocity is zero
		Assert.assertEquals(traj.getVelocity(traj.getTotalTime()), 0.0, 1e-6f);

		// check accelerations for this case
		Assert.assertEquals(3.0, traj.parts[0].acc, 0.0);
		Assert.assertEquals(traj.parts[1].acc, -3.0, 0.0);

		// check if max velocity is exceeded
		checkVelocity(traj, 2.0);
	}


	@Test
	public void testCaseE()
	{
		var traj = trajectoryFactory.singleDim(0f, 2.0f, -1.0f, 2, 3);

		checkTimeOrder(traj);

		// check final position is reached
		Assert.assertEquals(traj.getPosition(traj.getTotalTime()), 2.0, 1e-6f);

		// check final velocity is zero
		Assert.assertEquals(traj.getVelocity(traj.getTotalTime()), 0.0, 1e-6f);

		// check accelerations for this case
		Assert.assertEquals(3.0, traj.parts[0].acc, 0.0);
		Assert.assertEquals(0.0, traj.parts[1].acc, 0.0);
		Assert.assertEquals(traj.parts[2].acc, -3.0f, 0.0);

		// check if max velocity is exceeded
		checkVelocity(traj, 2.0);
	}


	@Test
	public void testCaseF()
	{
		var traj = trajectoryFactory.singleDim(0f, 0.5f, 3.0f, 2, 3);

		checkTimeOrder(traj);

		// check number of used elements is 2
		Assert.assertEquals(2, traj.numParts);

		// check final position is reached
		Assert.assertEquals(traj.getPosition(traj.getTotalTime()), 0.5, 1e-6f);

		// check final velocity is zero
		Assert.assertEquals(traj.getVelocity(traj.getTotalTime()), 0.0, 1e-6f);

		// check accelerations for this case
		Assert.assertEquals(traj.parts[0].acc, -3.0f, 0.0);
		Assert.assertEquals(3.0f, traj.parts[1].acc, 0.0);
	}


	@Test
	public void testCaseG()
	{
		var traj = trajectoryFactory.singleDim(0f, 0.5f, 5.0f, 2, 3);

		checkTimeOrder(traj);

		// check final position is reached
		Assert.assertEquals(traj.getPosition(traj.getTotalTime()), 0.5, 1e-6f);

		// check final velocity is zero
		Assert.assertEquals(traj.getVelocity(traj.getTotalTime()), 0.0, 1e-6f);

		// check accelerations for this case
		Assert.assertEquals(traj.parts[0].acc, -3.0f, 0.0);
		Assert.assertEquals(0.0f, traj.parts[1].acc, 0.0);
		Assert.assertEquals(3.0, traj.parts[2].acc, 0.0);
	}
}
