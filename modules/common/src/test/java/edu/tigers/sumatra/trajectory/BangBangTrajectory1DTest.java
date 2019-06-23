/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 04.06.2015
 * Author(s): AndreR
 * *********************************************************
 */
package edu.tigers.sumatra.trajectory;

import org.junit.Assert;
import org.junit.Test;


/**
 * Test 1D bang bang trajectories.
 * 
 * @author AndreR
 */
public class BangBangTrajectory1DTest
{
	private void checkTimeOrder(final BangBangTrajectory1D traj)
	{
		Assert.assertTrue(traj.getPart(0).tEnd >= 0.0);
		Assert.assertTrue(traj.getPart(1).tEnd >= traj.getPart(0).tEnd);
		Assert.assertTrue(traj.getPart(2).tEnd >= traj.getPart(1).tEnd);
		Assert.assertTrue(traj.getPart(3).tEnd >= traj.getPart(2).tEnd);
	}
	
	
	private void checkVelocity(final BangBangTrajectory1D traj, final double vMax)
	{
		for (int i = 0; i < BangBangTrajectory1D.BANG_BANG_TRAJECTORY_1D_PARTS; i++)
		{
			Assert.assertTrue(Math.abs(traj.getPart(i).v0) <= vMax);
		}
	}
	
	
	/** */
	@Test
	public void testCaseA()
	{
		BangBangTrajectory1D traj = new BangBangTrajectory1D(0, 0.5, 0.5, 3, 5, 2);
		
		checkTimeOrder(traj);
		
		// check number of used elements equals 2
		Assert.assertTrue(traj.getPart(1).tEnd == traj.getPart(2).tEnd);
		Assert.assertTrue(traj.getPart(2).tEnd == traj.getPart(3).tEnd);
		
		// check final position is reached
		Assert.assertEquals(traj.getPosition(traj.getTotalTime()), 0.5, 1e-6f);
		
		// check final velocity is zero
		Assert.assertEquals(traj.getVelocity(traj.getTotalTime()), 0.0, 1e-6f);
		
		// check accelerations for this case
		Assert.assertTrue(traj.getPart(0).acc == 3.0);
		Assert.assertTrue(traj.getPart(1).acc == -5.0f);
		
		// check if max velocity is exceeded
		checkVelocity(traj, 2.0);
	}
	
	
	/** */
	@Test
	public void testCaseB()
	{
		BangBangTrajectory1D traj = new BangBangTrajectory1D(0, 2.0, 0.5, 3, 5, 2);
		
		checkTimeOrder(traj);
		
		// check number of used elements equals 3
		Assert.assertTrue(traj.getPart(2).tEnd == traj.getPart(3).tEnd);
		
		// check final position is reached
		Assert.assertEquals(traj.getPosition(traj.getTotalTime()), 2.0, 1e-6f);
		
		// check final velocity is zero
		Assert.assertEquals(traj.getVelocity(traj.getTotalTime()), 0.0, 1e-6f);
		
		// check accelerations for this case
		Assert.assertTrue(traj.getPart(0).acc == 3.0);
		Assert.assertTrue(traj.getPart(1).acc == 0.0);
		Assert.assertTrue(traj.getPart(2).acc == -5.0f);
		
		// check if max velocity is exceeded
		checkVelocity(traj, 2.0);
	}
	
	
	/** */
	@Test
	public void testCaseC()
	{
		BangBangTrajectory1D traj = new BangBangTrajectory1D(0, 2.0, 2.5, 3, 5, 2);
		
		checkTimeOrder(traj);
		
		// check number of used elements equals 3
		Assert.assertTrue(traj.getPart(2).tEnd == traj.getPart(3).tEnd);
		
		// check final position is reached
		Assert.assertEquals(traj.getPosition(traj.getTotalTime()), 2.0, 1e-6f);
		
		// check final velocity is zero
		Assert.assertEquals(traj.getVelocity(traj.getTotalTime()), 0.0, 1e-6f);
		
		// check accelerations for this case
		Assert.assertTrue(traj.getPart(0).acc == -5.0f);
		Assert.assertTrue(traj.getPart(1).acc == 0.0);
		Assert.assertTrue(traj.getPart(2).acc == -5.0f);
	}
	
	
	/** */
	@Test
	public void testCaseD()
	{
		BangBangTrajectory1D traj = new BangBangTrajectory1D(0, 0.5, -1.0f, 3, 5, 2);
		
		checkTimeOrder(traj);
		
		// check number of used elements equals 3
		Assert.assertTrue(traj.getPart(2).tEnd == traj.getPart(3).tEnd);
		
		// check final position is reached
		Assert.assertEquals(traj.getPosition(traj.getTotalTime()), 0.5, 1e-6f);
		
		// check final velocity is zero
		Assert.assertEquals(traj.getVelocity(traj.getTotalTime()), 0.0, 1e-6f);
		
		// check accelerations for this case
		Assert.assertTrue(traj.getPart(0).acc == 5.0);
		Assert.assertTrue(traj.getPart(1).acc == 3.0);
		Assert.assertTrue(traj.getPart(2).acc == -5.0f);
		
		// check if max velocity is exceeded
		checkVelocity(traj, 2.0);
	}
	
	
	/** */
	@Test
	public void testCaseE()
	{
		BangBangTrajectory1D traj = new BangBangTrajectory1D(0, 2.0, -1.0f, 3, 5, 2);
		
		checkTimeOrder(traj);
		
		// check final position is reached
		Assert.assertEquals(traj.getPosition(traj.getTotalTime()), 2.0, 1e-6f);
		
		// check final velocity is zero
		Assert.assertEquals(traj.getVelocity(traj.getTotalTime()), 0.0, 1e-6f);
		
		// check accelerations for this case
		Assert.assertTrue(traj.getPart(0).acc == 5.0);
		Assert.assertTrue(traj.getPart(1).acc == 3.0);
		Assert.assertTrue(traj.getPart(2).acc == 0.0);
		Assert.assertTrue(traj.getPart(3).acc == -5.0f);
		
		// check if max velocity is exceeded
		checkVelocity(traj, 2.0);
	}
	
	
	/** */
	@Test
	public void testCaseF()
	{
		BangBangTrajectory1D traj = new BangBangTrajectory1D(0, 0.5, 3.0, 3, 5, 2);
		
		checkTimeOrder(traj);
		
		// check number of used elements equals 3
		Assert.assertTrue(traj.getPart(2).tEnd == traj.getPart(3).tEnd);
		
		// check final position is reached
		Assert.assertEquals(traj.getPosition(traj.getTotalTime()), 0.5, 1e-6f);
		
		// check final velocity is zero
		Assert.assertEquals(traj.getVelocity(traj.getTotalTime()), 0.0, 1e-6f);
		
		// check accelerations for this case
		Assert.assertTrue(traj.getPart(0).acc == -5.0f);
		Assert.assertTrue(traj.getPart(1).acc == -3.0f);
		Assert.assertTrue(traj.getPart(2).acc == 5.0);
	}
	
	
	/** */
	@Test
	public void testCaseG()
	{
		BangBangTrajectory1D traj = new BangBangTrajectory1D(0, 0.5, 5.0, 3, 5, 2);
		
		checkTimeOrder(traj);
		
		// check final position is reached
		Assert.assertEquals(traj.getPosition(traj.getTotalTime()), 0.5, 1e-6f);
		
		// check final velocity is zero
		Assert.assertEquals(traj.getVelocity(traj.getTotalTime()), 0.0, 1e-6f);
		
		// check accelerations for this case
		Assert.assertTrue(traj.getPart(0).acc == -5.0f);
		Assert.assertTrue(traj.getPart(1).acc == -3.0f);
		Assert.assertTrue(traj.getPart(2).acc == 0.0);
		Assert.assertTrue(traj.getPart(3).acc == 5.0);
	}
}
