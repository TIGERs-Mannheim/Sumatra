/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.trajectory;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Test 1D bang bang trajectories.
 */
class BangBangTrajectory1DTest
{
	private final BangBangTrajectoryFactory trajectoryFactory = new BangBangTrajectoryFactory();


	private void checkTimeOrder(final BangBangTrajectory1D traj)
	{
		double tLast = 0.0;
		for (int i = 0; i < traj.numParts; i++)
		{
			assertTrue(traj.parts[i].tEnd >= tLast);
			tLast = traj.parts[i].tEnd;
		}
	}


	private void checkVelocity(final BangBangTrajectory1D traj, final double vMax)
	{
		for (BBTrajectoryPart part : traj.parts)
		{
			assertTrue(Math.abs(part.v0) <= vMax);
		}
	}


	@Test
	void testCaseA()
	{
		var traj = trajectoryFactory.singleDim(0f, 0.5f, 0.5f, 2, 3);

		checkTimeOrder(traj);

		// check number of used elements is 2
		assertEquals(2, traj.numParts);

		// check final position is reached
		assertEquals(0.5, traj.getPosition(traj.getTotalTime()), 1e-6f);

		// check final velocity is zero
		assertEquals(0.0, traj.getVelocity(traj.getTotalTime()), 1e-6f);

		// check accelerations for this case
		assertEquals(3.0, traj.parts[0].acc, 0.0);
		assertEquals(-3.0f, traj.parts[1].acc, 0.0);

		// check if max velocity is exceeded
		checkVelocity(traj, 2.0);
	}


	@Test
	void testCaseB()
	{
		var traj = trajectoryFactory.singleDim(0f, 2.0f, 0.5f, 2, 3);

		checkTimeOrder(traj);

		// check number of used elements is 3
		assertEquals(3, traj.numParts);

		// check final position is reached
		assertEquals(2.0, traj.getPosition(traj.getTotalTime()), 1e-6f);

		// check final velocity is zero
		assertEquals(0.0, traj.getVelocity(traj.getTotalTime()), 1e-6f);

		// check accelerations for this case
		assertEquals(3.0, traj.parts[0].acc, 0.0);
		assertEquals(0.0, traj.parts[1].acc, 0.0);
		assertEquals(-3.0, traj.parts[2].acc, 0.0);

		// check if max velocity is exceeded
		checkVelocity(traj, 2.0);
	}


	@Test
	void testCaseC()
	{
		var traj = trajectoryFactory.singleDim(0f, 2.0f, 2.5f, 2, 3);

		checkTimeOrder(traj);

		// check number of used elements is 3
		assertEquals(3, traj.numParts);

		// check final position is reached
		assertEquals(2.0, traj.getPosition(traj.getTotalTime()), 1e-6f);

		// check final velocity is zero
		assertEquals(0.0, traj.getVelocity(traj.getTotalTime()), 1e-6f);

		// check accelerations for this case
		assertEquals(-3.0f, traj.parts[0].acc, 0.0);
		assertEquals(0.0, traj.parts[1].acc, 0.0);
		assertEquals(-3.0f, traj.parts[2].acc, 0.0);
	}


	@Test
	void testCaseD()
	{
		var traj = trajectoryFactory.singleDim(0f, 0.5f, -1.0f, 2, 3);

		checkTimeOrder(traj);

		// check number of used elements is 2
		assertEquals(2, traj.numParts);

		// check final position is reached
		assertEquals(0.5, traj.getPosition(traj.getTotalTime()), 1e-6f);

		// check final velocity is zero
		assertEquals(0.0, traj.getVelocity(traj.getTotalTime()), 1e-6f);

		// check accelerations for this case
		assertEquals(3.0, traj.parts[0].acc, 0.0);
		assertEquals(-3.0, traj.parts[1].acc, 0.0);

		// check if max velocity is exceeded
		checkVelocity(traj, 2.0);
	}


	@Test
	void testCaseE()
	{
		var traj = trajectoryFactory.singleDim(0f, 2.0f, -1.0f, 2, 3);

		checkTimeOrder(traj);

		// check final position is reached
		assertEquals(2.0, traj.getPosition(traj.getTotalTime()), 1e-6f);

		// check final velocity is zero
		assertEquals(0.0, traj.getVelocity(traj.getTotalTime()), 1e-6f);

		// check accelerations for this case
		assertEquals(3.0, traj.parts[0].acc, 0.0);
		assertEquals(0.0, traj.parts[1].acc, 0.0);
		assertEquals(-3.0f, traj.parts[2].acc, 0.0);

		// check if max velocity is exceeded
		checkVelocity(traj, 2.0);
	}


	@Test
	void testCaseF()
	{
		var traj = trajectoryFactory.singleDim(0f, 0.5f, 3.0f, 2, 3);

		checkTimeOrder(traj);

		// check number of used elements is 2
		assertEquals(2, traj.numParts);

		// check final position is reached
		assertEquals(0.5, traj.getPosition(traj.getTotalTime()), 1e-6f);

		// check final velocity is zero
		assertEquals(0.0, traj.getVelocity(traj.getTotalTime()), 1e-6f);

		// check accelerations for this case
		assertEquals(-3.0f, traj.parts[0].acc, 0.0);
		assertEquals(3.0f, traj.parts[1].acc, 0.0);
	}


	@Test
	void testCaseG()
	{
		var traj = trajectoryFactory.singleDim(0f, 0.5f, 5.0f, 2, 3);

		checkTimeOrder(traj);

		// check final position is reached
		assertEquals(0.5, traj.getPosition(traj.getTotalTime()), 1e-6f);

		// check final velocity is zero
		assertEquals(0.0, traj.getVelocity(traj.getTotalTime()), 1e-6f);

		// check accelerations for this case
		assertEquals(-3.0f, traj.parts[0].acc, 0.0);
		assertEquals(0.0f, traj.parts[1].acc, 0.0);
		assertEquals(3.0, traj.parts[2].acc, 0.0);
	}
}
