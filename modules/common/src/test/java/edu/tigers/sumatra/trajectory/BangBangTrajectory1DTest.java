/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.trajectory;

import org.junit.Assert;
import org.junit.Test;

import edu.tigers.sumatra.trajectory.BangBangTrajectory1D.BBTrajectoryPart;


/**
 * Test 1D bang bang trajectories.
 * 
 * @author AndreR
 */
public class BangBangTrajectory1DTest
{
	private void checkTimeOrder(final BangBangTrajectory1D traj)
	{
		double tLast = 0.0;
		for (BBTrajectoryPart part : traj.getParts())
		{
			Assert.assertTrue(part.tEnd >= tLast);
			tLast = part.tEnd;
		}
	}
	
	
	private void checkVelocity(final BangBangTrajectory1D traj, final double vMax)
	{
		for (BBTrajectoryPart part : traj.getParts())
		{
			Assert.assertTrue(Math.abs(part.v0) <= vMax);
		}
	}
	
	
	/** */
	@Test
	public void testCaseA()
	{
		BangBangTrajectory1D traj = new BangBangTrajectory1D(0f, 0.5f, 0.5f, 2, 3);
		
		checkTimeOrder(traj);
		
		// check number of used elements is 2
		Assert.assertTrue(traj.getParts().size() == 2);
		
		// check final position is reached
		Assert.assertEquals(traj.getPosition(traj.getTotalTime()), 0.5, 1e-6f);
		
		// check final velocity is zero
		Assert.assertEquals(traj.getVelocity(traj.getTotalTime()), 0.0, 1e-6f);
		
		// check accelerations for this case
		Assert.assertTrue(traj.getPart(0).acc == 3.0);
		Assert.assertTrue(traj.getPart(1).acc == -3.0f);
		
		// check if max velocity is exceeded
		checkVelocity(traj, 2.0);
	}
	
	
	/** */
	@Test
	public void testCaseB()
	{
		BangBangTrajectory1D traj = new BangBangTrajectory1D(0f, 2.0f, 0.5f, 2, 3);
		
		checkTimeOrder(traj);
		
		// check number of used elements is 3
		Assert.assertTrue(traj.getParts().size() == 3);
		
		// check final position is reached
		Assert.assertEquals(traj.getPosition(traj.getTotalTime()), 2.0, 1e-6f);
		
		// check final velocity is zero
		Assert.assertEquals(traj.getVelocity(traj.getTotalTime()), 0.0, 1e-6f);
		
		// check accelerations for this case
		Assert.assertTrue(traj.getPart(0).acc == 3.0);
		Assert.assertTrue(traj.getPart(1).acc == 0.0);
		Assert.assertTrue(traj.getPart(2).acc == -3.0);
		
		// check if max velocity is exceeded
		checkVelocity(traj, 2.0);
	}
	
	
	/** */
	@Test
	public void testCaseC()
	{
		BangBangTrajectory1D traj = new BangBangTrajectory1D(0f, 2.0f, 2.5f, 2, 3);
		
		checkTimeOrder(traj);
		
		// check number of used elements is 3
		Assert.assertTrue(traj.getParts().size() == 3);
		
		// check final position is reached
		Assert.assertEquals(traj.getPosition(traj.getTotalTime()), 2.0, 1e-6f);
		
		// check final velocity is zero
		Assert.assertEquals(traj.getVelocity(traj.getTotalTime()), 0.0, 1e-6f);
		
		// check accelerations for this case
		Assert.assertTrue(traj.getPart(0).acc == -3.0f);
		Assert.assertTrue(traj.getPart(1).acc == 0.0);
		Assert.assertTrue(traj.getPart(2).acc == -3.0f);
	}
	
	
	/** */
	@Test
	public void testCaseD()
	{
		BangBangTrajectory1D traj = new BangBangTrajectory1D(0f, 0.5f, -1.0f, 2, 3);
		
		checkTimeOrder(traj);
		
		// check number of used elements is 2
		Assert.assertTrue(traj.getParts().size() == 2);
		
		// check final position is reached
		Assert.assertEquals(traj.getPosition(traj.getTotalTime()), 0.5, 1e-6f);
		
		// check final velocity is zero
		Assert.assertEquals(traj.getVelocity(traj.getTotalTime()), 0.0, 1e-6f);
		
		// check accelerations for this case
		Assert.assertTrue(traj.getPart(0).acc == 3.0);
		Assert.assertTrue(traj.getPart(1).acc == -3.0);
		
		// check if max velocity is exceeded
		checkVelocity(traj, 2.0);
	}
	
	
	/** */
	@Test
	public void testCaseE()
	{
		BangBangTrajectory1D traj = new BangBangTrajectory1D(0f, 2.0f, -1.0f, 2, 3);
		
		checkTimeOrder(traj);
		
		// check final position is reached
		Assert.assertEquals(traj.getPosition(traj.getTotalTime()), 2.0, 1e-6f);
		
		// check final velocity is zero
		Assert.assertEquals(traj.getVelocity(traj.getTotalTime()), 0.0, 1e-6f);
		
		// check accelerations for this case
		Assert.assertTrue(traj.getPart(0).acc == 3.0);
		Assert.assertTrue(traj.getPart(1).acc == 0.0);
		Assert.assertTrue(traj.getPart(2).acc == -3.0f);
		
		// check if max velocity is exceeded
		checkVelocity(traj, 2.0);
	}
	
	
	/** */
	@Test
	public void testCaseF()
	{
		BangBangTrajectory1D traj = new BangBangTrajectory1D(0f, 0.5f, 3.0f, 2, 3);
		
		checkTimeOrder(traj);
		
		// check number of used elements is 2
		Assert.assertTrue(traj.getParts().size() == 2);
		
		// check final position is reached
		Assert.assertEquals(traj.getPosition(traj.getTotalTime()), 0.5, 1e-6f);
		
		// check final velocity is zero
		Assert.assertEquals(traj.getVelocity(traj.getTotalTime()), 0.0, 1e-6f);
		
		// check accelerations for this case
		Assert.assertTrue(traj.getPart(0).acc == -3.0f);
		Assert.assertTrue(traj.getPart(1).acc == 3.0f);
	}
	
	
	/** */
	@Test
	public void testCaseG()
	{
		BangBangTrajectory1D traj = new BangBangTrajectory1D(0f, 0.5f, 5.0f, 2, 3);
		
		checkTimeOrder(traj);
		
		// check final position is reached
		Assert.assertEquals(traj.getPosition(traj.getTotalTime()), 0.5, 1e-6f);
		
		// check final velocity is zero
		Assert.assertEquals(traj.getVelocity(traj.getTotalTime()), 0.0, 1e-6f);
		
		// check accelerations for this case
		Assert.assertTrue(traj.getPart(0).acc == -3.0f);
		Assert.assertTrue(traj.getPart(1).acc == 0.0f);
		Assert.assertTrue(traj.getPart(2).acc == 3.0);
	}
}
