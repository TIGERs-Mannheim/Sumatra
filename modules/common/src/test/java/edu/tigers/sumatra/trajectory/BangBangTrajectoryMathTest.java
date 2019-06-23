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

import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.math.Vector2f;


/**
 * Test BangBangTrajectoryMath
 * 
 * @author ArneS
 */
public class BangBangTrajectoryMathTest
{
	// Constants for brake functions
	final IVector2	testVel			= new Vector2f(1.0, 1.0);
	final double	testMaxBrk		= 1.0;
											
	// Additional constants for maxvel functions
	final IVector2	testInitialVel	= new Vector2f(0.5, 0.5);
	final double	testMaxAcc		= 0.5;
	final double	testMaxVel		= 2.0;
											
											
	/**
	 * Test functions concerning brake distance and -time calculations
	 */
	@Test
	public void testBrakeDistanceFunctions()
	{
		checkBrakeVector();
		checkBrakeDistance();
		checkBrakeTime();
	}
	
	
	/**
	 * Test functions concerning maximum velocity distance calculations
	 */
	@Test
	public void testDistanceToAchieveMaxVel()
	{
		checkMaxVelDistance();
		checkMaxVelVector();
	}
	
	
	private void checkMaxVelDistance()
	{
		double distance = BangBangTrajectoryMath.distanceToAchieveMaxVelocity(testInitialVel, testMaxVel, testMaxAcc,
				testMaxBrk);
		ITrajectory<IVector2> traj = new BangBangTrajectory2D(new Vector2(0, 0),
				new Vector2(1, 1).normalizeNew().multiplyNew(distance),
				testInitialVel, testMaxAcc, testMaxBrk, testMaxVel + 100); // maxvel has an offset because it should be
																								// calculated properly --> that is to check...
		double resultvmax = BangBangTrajectoryMath.maxVelocityOfTrajectory(traj);
		Assert.assertTrue(SumatraMath.isEqual(resultvmax, testMaxVel, 5e-3));
		
	}
	
	
	private void checkMaxVelVector()
	{
		IVector2 distance = BangBangTrajectoryMath.distanceVectorToAchieveMaxVelocity(testInitialVel, testMaxVel,
				testMaxAcc, testMaxBrk);
		ITrajectory<IVector2> traj = new BangBangTrajectory2D(new Vector2(0, 0), distance, testInitialVel, testMaxAcc,
				testMaxBrk, testMaxVel + 100);
		Assert.assertTrue(SumatraMath.isEqual(BangBangTrajectoryMath.maxVelocityOfTrajectory(traj), testMaxVel, 5e-3));
	}
	
	
	private void checkBrakeVector()
	{
		IVector2 expectedDistanceVector = new Vector2f(SumatraMath.sqrt(2) / 2.0, SumatraMath.sqrt(2) / 2.0);
		IVector2 resultVector = BangBangTrajectoryMath.brakeDistanceVector(testVel, testMaxBrk);
		Assert.assertTrue(expectedDistanceVector.equals(resultVector, 1e-6));
	}
	
	
	private void checkBrakeDistance()
	{
		double expectedDistance = 1.0;
		double resultDistance = BangBangTrajectoryMath.brakeDistance(testVel, testMaxBrk);
		Assert.assertTrue(SumatraMath.isEqual(expectedDistance, resultDistance, 1e-6));
	}
	
	
	private void checkBrakeTime()
	{
		double expectedTime = SumatraMath.sqrt(2);
		double resultTime = BangBangTrajectoryMath.timeToBreak(testVel, testMaxBrk);
		Assert.assertTrue(SumatraMath.isEqual(expectedTime, resultTime, 1e-6));
	}
	
}
