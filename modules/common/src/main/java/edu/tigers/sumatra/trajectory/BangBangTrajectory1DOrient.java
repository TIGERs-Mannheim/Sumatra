/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 19.07.2015
 * Author(s): AndreR
 * *********************************************************
 */
package edu.tigers.sumatra.trajectory;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.math.AngleMath;


/**
 * @author AndreR
 */
@Persistent
public class BangBangTrajectory1DOrient extends BangBangTrajectory1D
{
	
	@SuppressWarnings("unused")
	private BangBangTrajectory1DOrient()
	{
	}
	
	
	/**
	 * @param initialPos
	 * @param finalPos
	 * @param initialVel
	 * @param maxVel
	 * @param maxAcc
	 */
	public BangBangTrajectory1DOrient(final double initialPos, final double finalPos, final double initialVel,
			final double maxVel, final double maxAcc)
	{
		this.initialPos = initialPos;
		this.finalPos = finalPos;
		this.initialVel = initialVel;
		this.maxAcc = maxAcc;
		this.maxVel = maxVel;
		
		double sDiffShort = AngleMath.normalizeAngle(finalPos - initialPos);
		double sDiffLong;
		if (sDiffShort < 0)
		{
			sDiffLong = sDiffShort + (2 * AngleMath.PI);
		} else
		{
			sDiffLong = sDiffShort - (2 * AngleMath.PI);
		}
		
		generateTrajectory(initialPos, initialVel, initialPos + sDiffLong, maxVel, maxAcc);
		double tLong = getTotalTime();
		generateTrajectory(initialPos, initialVel, initialPos + sDiffShort, maxVel, maxAcc);
		double tShort = getTotalTime();
		
		if (tLong < tShort)
		{
			generateTrajectory(initialPos, initialVel, initialPos + sDiffLong, maxVel, maxAcc);
		}
	}
	
	
	@Override
	public Double getPositionMM(final double t)
	{
		return AngleMath.normalizeAngle(getValuesAtTime(t).pos);
	}
}
