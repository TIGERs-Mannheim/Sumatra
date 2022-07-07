/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.trajectory;

import com.sleepycat.persist.model.Persistent;
import edu.tigers.sumatra.math.AngleMath;
import lombok.RequiredArgsConstructor;

import java.util.List;


/**
 * Bang Bang Trajectory for one dimension for orientation.
 */
@Persistent
@RequiredArgsConstructor
class BangBangTrajectory1DOrient implements ITrajectory<Double>
{
	final BangBangTrajectory1D child;


	@SuppressWarnings("unused") // berkeley
	private BangBangTrajectory1DOrient()
	{
		child = new BangBangTrajectory1D();
	}


	@Override
	public Double getPositionMM(final double t)
	{
		return getPosition(t);
	}


	@Override
	public Double getPosition(final double t)
	{
		return AngleMath.normalizeAngle(child.getPosition(t));
	}


	@Override
	public Double getVelocity(final double t)
	{
		return child.getVelocity(t);
	}


	@Override
	public Double getAcceleration(final double t)
	{
		return child.getAcceleration(t);
	}


	@Override
	public double getTotalTime()
	{
		return child.getTotalTime();
	}


	@Override
	public BangBangTrajectory1DOrient mirrored()
	{
		BangBangTrajectory1D mirrored = new BangBangTrajectory1D();
		mirrored.numParts = child.numParts;
		for (int i = 0; i < child.numParts; i++)
		{
			mirrored.parts[i].tEnd = child.parts[i].tEnd;
			mirrored.parts[i].acc = child.parts[i].acc;
			mirrored.parts[i].v0 = child.parts[i].v0;
			mirrored.parts[i].s0 = (float) AngleMath.mirror(child.parts[i].s0);
		}

		return new BangBangTrajectory1DOrient(mirrored);
	}


	@Override
	public PosVelAcc<Double> getValuesAtTime(final double tt)
	{
		PosVelAcc<Double> valuesAtTime = child.getValuesAtTime(tt);
		return new PosVelAcc<>(AngleMath.normalizeAngle(valuesAtTime.getPos()), valuesAtTime.getVel(),
				valuesAtTime.getAcc());
	}


	@Override
	public List<Double> getTimeSections()
	{
		return child.getTimeSections();
	}
}
