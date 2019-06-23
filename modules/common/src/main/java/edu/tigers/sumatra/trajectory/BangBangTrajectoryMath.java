/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 23.01.2016
 * Author(s): Arne Sachtler <arne.sachtler@dlr.de>
 * *********************************************************
 */
package edu.tigers.sumatra.trajectory;

import static java.lang.Math.max;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.planarcurve.PlanarCurve;
import edu.tigers.sumatra.planarcurve.PlanarCurveSegment;


/**
 * Util class for BangBangTrajectories
 *
 * @author ArneS <arne.sachtler@dlr.de>
 * @author AndreR <andre@ryll.cc>
 */
public final class BangBangTrajectoryMath
{
	private BangBangTrajectoryMath()
	{
	}
	
	
	/**
	 * Calculate brake distance as 2-dimensional vector.
	 *
	 * @param initialVel
	 * @param brkMax
	 * @return Vector2 containing brake distance
	 */
	public static IVector2 brakeDistanceVector(final IVector2 initialVel, final double brkMax)
	{
		double absSpeed = initialVel.getLength();
		double distance = 0.5 * (1.0 / brkMax) * absSpeed * absSpeed;
		Vector2 direction = initialVel.normalizeNew();
		return direction.multiply(distance);
	}
	
	
	/**
	 * Calculate shortest possible break distance
	 *
	 * @param initialVel
	 * @param brkMax
	 * @return shortest possible break distance as double
	 */
	public static double brakeDistance(final IVector2 initialVel, final double brkMax)
	{
		return brakeDistanceVector(initialVel, brkMax).getLength();
	}
	
	
	/**
	 * Create new BangBangTrajectory2D to brake as fast as possible
	 *
	 * @param curPos
	 * @param initialVel
	 * @param brkMax
	 * @return new BangBangTrajectory2D that can be applied to bot in order to brake as fast as possible
	 */
	public static BangBangTrajectory2D createShortestBrakeTrajectory(final IVector2 curPos, final IVector2 initialVel,
			final double brkMax)
	{
		IVector2 brakeVector = brakeDistanceVector(initialVel, brkMax);
		return new BangBangTrajectory2D(curPos, curPos.addNew(brakeVector), initialVel, initialVel.getLength(), brkMax);
	}
	
	
	/**
	 * Calculate the time to brake as max brake Acceleration
	 *
	 * @param initialVel
	 * @param brkMax
	 * @return
	 */
	public static double timeToBrake(final IVector2 initialVel, final double brkMax)
	{
		return Math.sqrt((2.0 * Math.abs(brakeDistance(initialVel, brkMax))) / Math.abs(brkMax));
	}
	
	
	/**
	 * Calculate distance to achieve maximal velocity given. vmax is parallel to initial vel.
	 *
	 * @param initialVel
	 * @param maxVel
	 * @param maxAcc
	 * @return distance
	 */
	public static double distanceToAchieveMaxVelocity(final IVector2 initialVel, final double maxVel,
			final double maxAcc)
	{
		return distanceToAchieveMaxVelocity(initialVel.getLength(), maxVel, maxAcc);
	}
	
	
	private static double distanceToAchieveMaxVelocity(final double initialVel, final double maxVel,
			final double maxAcc)
	{
		double t1 = (maxVel - initialVel) / maxAcc; // acceleration phase
		double tb = maxVel / maxAcc; // brake phase
		double s1 = (initialVel * t1) + (0.5 * maxAcc * t1 * t1); // distance in acceleration phase
		double sb = (maxVel * tb) - (0.5 * maxAcc * tb * tb); // distance in brake phase
		return s1 + sb;
	}
	
	
	/**
	 * Calculate distance to achieve maximal velocity given. vmax is parallel to initial vel.
	 *
	 * @param initialVel
	 * @param maxVel
	 * @param maxAcc
	 * @return distance as vector
	 */
	public static IVector2 distanceVectorToAchieveMaxVelocity(final IVector2 initialVel, final double maxVel,
			final double maxAcc)
	{
		IVector2 direction = initialVel.normalizeNew();
		return direction.multiplyNew(distanceToAchieveMaxVelocity(initialVel, maxVel, maxAcc));
	}
	
	
	/**
	 * Analytically look for the maximum velocity in trajectory
	 *
	 * @param traj
	 * @return vmax
	 */
	public static double maxVelocityOfTrajectory(final BangBangTrajectory2D traj)
	{
		double vmax = 0;
		PlanarCurve curve = traj.getPlanarCurve();
		for (PlanarCurveSegment seg : curve.getSegments())
		{
			double vel = seg.getVel().getLength2();
			if (vel > vmax)
			{
				vmax = vel;
			}
		}
		
		return vmax * 1e-3;
	}
	
	
	/**
	 * Get virtual (not necessarily reachable) destination to generate trajectory in order to reach given point at given
	 * time. Target velocity does not have to be zero
	 *
	 * @param curPos
	 * @param initialVel
	 * @param desiredPos
	 * @param maxAcc
	 * @param maxVel
	 * @param time
	 * @param tolerance
	 * @return
	 */
	public static IVector2 getVirtualDestinationToReachPositionInTime(final IVector2 curPos, final IVector2 initialVel,
			final IVector2 desiredPos, final double maxAcc, final double maxVel, final double time,
			final double tolerance)
	{
		ITrajectory<IVector2> direct = new BangBangTrajectory2D(curPos.multiplyNew(1e-3), desiredPos.multiplyNew(1e-3),
				initialVel, maxVel, maxAcc);
		
		IVector2 delta = desiredPos.subtractNew(curPos);
		if (direct.getPositionMM(time).isCloseTo(desiredPos, tolerance)
				|| ((time * initialVel.getLength2() * 1e3) > delta.getLength2()) || (time < 0))
		{
			return desiredPos;
		}
		
		double projectedVel = initialVel.scalarProduct(delta.multiplyNew(1e-3)) / delta.multiplyNew(1e-3).getLength2();
		
		double timeToMaxSpeed = (maxVel - projectedVel) / maxAcc; // time to accelerate to maximum speed
		double uniformVelocityTime = max(time - timeToMaxSpeed, 0);
		
		double accelerationTime = Math.min(timeToMaxSpeed, time);
		
		double longestPossibleDistance = 1000
				* ((0.5 * maxAcc * accelerationTime * accelerationTime) + (maxVel * uniformVelocityTime));
		
		if (longestPossibleDistance < delta.getLength())
		{
			// it won't be possible to reach target in given time slot. Desperate maxvel destination will be given.
			return curPos.addNew(delta.normalizeNew().multiplyNew(delta.getLength2() * 10));
		}
		
		double accelerationPhaseTime = ((sqrt(pow(maxVel, 2) +
				(((-2 * projectedVel) - (2 * maxAcc * time)) * maxVel)
				+ pow(projectedVel, 2) + (2 * maxAcc * 1e-3 * delta.getLength2()))
				- maxVel) + projectedVel) / maxAcc;
		
		double staticVelocityPhaseTime = time - accelerationPhaseTime;
		
		double breakPhaseTime = maxVel / maxAcc;
		
		double virtualDistance = ((projectedVel * accelerationPhaseTime) + (0.5 * maxAcc * pow(accelerationPhaseTime, 2))
				+ (maxVel * (staticVelocityPhaseTime + breakPhaseTime))) - (0.5 * maxAcc * pow(breakPhaseTime, 2));
		virtualDistance *= 1e3;
		return delta.scaleToNew(virtualDistance).addNew(curPos);
	}
}