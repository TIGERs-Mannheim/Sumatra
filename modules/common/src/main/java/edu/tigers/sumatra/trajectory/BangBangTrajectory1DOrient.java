/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
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
		
		double sDiffShort = AngleMath.difference(finalPos, initialPos);
		generateTrajectory(initialPos, initialVel, initialPos + sDiffShort, maxVel, maxAcc);
	}
	
	
	@Override
	public Double getPositionMM(final double t)
	{
		return AngleMath.normalizeAngle(getValuesAtTime(t).pos);
	}
	
	
	@Override
	public BangBangTrajectory1DOrient mirrored()
	{
		return new BangBangTrajectory1DOrient(AngleMath.mirror(initialPos), AngleMath.mirror(finalPos), -initialVel,
				maxVel, maxAcc);
	}
}
