/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 19.07.2015
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory;

import com.sleepycat.persist.model.Persistent;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;


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
	 * @param maxAcc
	 * @param maxBrk
	 * @param maxVel
	 */
	public BangBangTrajectory1DOrient(final float initialPos, final float finalPos, final float initialVel,
			final float maxAcc, final float maxBrk,
			final float maxVel)
	{
		this.initialPos = initialPos;
		this.finalPos = finalPos;
		this.initialVel = initialVel;
		this.maxAcc = maxAcc;
		this.maxBrk = maxBrk;
		this.maxVel = maxVel;
		
		float sDiffShort = AngleMath.normalizeAngle(finalPos - initialPos);
		float sDiffLong;
		if (sDiffShort < 0)
		{
			sDiffLong = sDiffShort + (2 * AngleMath.PI);
		} else
		{
			sDiffLong = sDiffShort - (2 * AngleMath.PI);
		}
		
		generateTrajectory(sDiffLong, initialVel, maxAcc, maxBrk, maxVel);
		float tLong = getTotalTime();
		generateTrajectory(sDiffShort, initialVel, maxAcc, maxBrk, maxVel);
		float tShort = getTotalTime();
		
		if (tLong < tShort)
		{
			generateTrajectory(sDiffLong, initialVel, maxAcc, maxBrk, maxVel);
		}
		
		calcVelPos(initialPos, initialVel);
	}
	
	
	@Override
	public float getPosition(final float t)
	{
		return AngleMath.normalizeAngle(getValuesAtTime(t).pos);
	}
}
