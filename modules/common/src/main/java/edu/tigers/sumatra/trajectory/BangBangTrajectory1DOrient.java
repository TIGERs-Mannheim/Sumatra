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
	public BangBangTrajectory1DOrient(final float initialPos, final float finalPos, final float initialVel,
			final float maxVel, final float maxAcc)
	{
		float sDiffShort = (float) AngleMath.difference(finalPos, initialPos);
		this.initialPos = initialPos;
		this.finalPos = initialPos + sDiffShort;
		this.initialVel = initialVel;
		this.maxAcc = maxAcc;
		this.maxVel = maxVel;
		
		init();
		generateTrajectory();
	}
	
	
	@Override
	public Double getPositionMM(final double t)
	{
		return AngleMath.normalizeAngle(getValuesAtTime(t).pos);
	}
	
	
	@Override
	public BangBangTrajectory1DOrient mirrored()
	{
		return new BangBangTrajectory1DOrient((float) AngleMath.mirror(initialPos), (float) AngleMath.mirror(finalPos),
				-initialVel,
				maxVel, maxAcc);
	}
}
