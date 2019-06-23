/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 23.01.2016
 * Author(s): Arne Sachtler <arne.sachtler@dlr.de>
 * *********************************************************
 */
package edu.tigers.sumatra.trajectory;

import edu.tigers.sumatra.math.AVector2;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.Vector2;


/**
 * Util class for BangBangTrajectories
 * 
 * @author ArneS <arne.sachtler@dlr.de>
 */
public final class BangBangTrajectoryMath
{
	private static final double	DT_PRECISE					= 0.01;
																			
																			
	private static final double	BINARY_SEARCH_EPSILON	= 0.01;
																			
																			
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
		double distance = (1.0 / 2.0) * (1.0 / brkMax) * absSpeed * absSpeed;
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
	public static ITrajectory<IVector2> createShortestBrakeTrajectory(final IVector2 curPos, final IVector2 initialVel,
			final double brkMax)
	{
		IVector2 brakeVector = brakeDistanceVector(initialVel, brkMax);
		return new BangBangTrajectory2D(curPos, curPos.addNew(brakeVector), initialVel, 0.0, brkMax,
				initialVel.getLength());
				
	}
	
	
	/**
	 * Calculate the time to break as max break Acceleration
	 * 
	 * @param initialVel
	 * @param brkMax
	 * @return
	 */
	public static double timeToBreak(final IVector2 initialVel, final double brkMax)
	{
		return SumatraMath.sqrt((2.0 * SumatraMath.abs(brakeDistance(initialVel, brkMax))) / SumatraMath.abs(brkMax));
	}
	
	
	/**
	 * Calculate distance to achieve maximal velocity given. vmax is parallel to initial vel.
	 * 
	 * @param initialVel
	 * @param maxVel
	 * @param maxAcc
	 * @param maxBrk
	 * @return distance
	 */
	public static double distanceToAchieveMaxVelocity(final IVector2 initialVel, final double maxVel,
			final double maxAcc, final double maxBrk)
	{
		return distanceToAchieveMaxVelocity(initialVel.getLength(), maxVel, maxAcc, maxBrk);
	}
	
	
	private static double distanceToAchieveMaxVelocity(final double initialVel, final double maxVel,
			final double maxAcc,
			final double maxBrk)
	{
		double t1 = (maxVel - initialVel) / maxAcc; // acceleration phase
		double tb = maxVel / maxBrk; // brake phase
		double s1 = (initialVel * t1) + ((1.0 / 2.0) * maxAcc * t1 * t1); // distance in acceleration phase
		double sb = (maxVel * tb) - ((1.0 / 2.0) * maxBrk * tb * tb); // distance in brake phase
		return s1 + sb;
	}
	
	
	/**
	 * Calculate distance to achieve maximal velocity given. vmax is parallel to initial vel.
	 * 
	 * @param initialVel
	 * @param maxVel
	 * @param maxAcc
	 * @param maxBrk
	 * @return distance as vector
	 */
	public static IVector2 distanceVectorToAchieveMaxVelocity(final IVector2 initialVel, final double maxVel,
			final double maxAcc, final double maxBrk)
	{
		IVector2 direction = initialVel.normalizeNew();
		return direction.multiplyNew(distanceToAchieveMaxVelocity(initialVel, maxVel, maxAcc, maxBrk));
	}
	
	
	/**
	 * Numerically look for the maximum velocity in trajectory
	 * 
	 * @param traj
	 * @return vmax
	 */
	public static double maxVelocityOfTrajectory(final ITrajectory<IVector2> traj)
	{
		return maxVelocityOfTrajectory(traj, DT_PRECISE);
	}
	
	
	/**
	 * Get virtual (not necessarily reachable) destination to generate trajectory in order to reach given point at given
	 * time. Target velocity does not have to be zero
	 * 
	 * @param curPos
	 * @param initialVel
	 * @param desiredPos
	 * @param maxAcc
	 * @param maxBrk
	 * @param maxVel
	 * @param time
	 * @return
	 */
	public static IVector2 getVirtualDestinationToReachPositionInTime(final IVector2 curPos, final IVector2 initialVel,
			final IVector2 desiredPos, final double maxAcc, final double maxBrk, final double maxVel, final double time)
	{
		ITrajectory<IVector2> direct = new BangBangTrajectory2D(curPos.multiplyNew(1e-3), desiredPos.multiplyNew(1e-3),
				initialVel, maxAcc, maxAcc, maxVel);
				
		if (direct.getPositionMM(time).equals(desiredPos, 1.0))
		{
			return desiredPos; // passt schon so
		}
		
		IVector2 delta = desiredPos.subtractNew(curPos);
		
		double t_tovmax = maxVel / maxAcc; // time to accelerate to maximum speed
		double t_uniform = time - t_tovmax;
		
		double s_maxdistance = (1000 * ((0.5 * maxAcc * t_tovmax * t_tovmax))) + (maxVel * t_uniform); // longest possible
																																		// distance
		// to given time
		if (s_maxdistance < delta.getLength())
		{
			// it won't be possible to reach target in given time slot. Desperate maxvel destination will be given.
			return curPos.addNew(delta.normalizeNew().multiplyNew(s_maxdistance));
		}
		
		double left = 0.0;
		double right = s_maxdistance;
		double probe = s_maxdistance / 2.0;
		
		do
		{
			IVector2 probeVec = delta.scaleToNew(probe);
			ITrajectory<IVector2> probeTraj = new BangBangTrajectory2D(AVector2.ZERO_VECTOR, probeVec.multiplyNew(1e-3),
					initialVel, maxAcc, maxBrk, maxVel);
			if (probeTraj.getPositionMM(time).getLength() > delta.getLength())
			{
				right = probe;
			} else
			{
				left = probe;
			}
			probe = ((right - left) / 2) + left;
		} while ((right - left) > BINARY_SEARCH_EPSILON);
		
		IVector2 resultVector = delta.scaleToNew(probe);
		return resultVector.addNew(curPos);
		
	}
	
	
	/**
	 * Numerically look for the maximum velocity in trajectory
	 * 
	 * @param traj
	 * @param precision
	 * @return vmax
	 */
	public static double maxVelocityOfTrajectory(final ITrajectory<IVector2> traj, final double precision)
	{
		double vmax = 0;
		for (double t = 0; t < traj.getTotalTime(); t += precision)
		{
			if (traj.getVelocity(t).getLength() > vmax)
			{
				vmax = traj.getVelocity(t).getLength();
			}
		}
		return vmax;
	}
}