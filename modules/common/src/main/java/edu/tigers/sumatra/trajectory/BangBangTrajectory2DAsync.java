/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.trajectory;

import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;


/**
 * Asynchronous Bang Bang Trajectory for two dimensions.
 * X and Y are not synchronized in this version. The trajectory tries
 * to get on the line defined by target position and primary direction first.
 */
@ToString
@Getter
@RequiredArgsConstructor
class BangBangTrajectory2DAsync implements ITrajectory<IVector2>
{
	final BangBangTrajectory2D child;
	final IVector2 initialPos;
	final double rotation;


	@Override
	public Vector2 getPositionMM(final double t)
	{
		return child.getPositionMM(t).turn(rotation).add(initialPos.multiplyNew(1e3));
	}


	@Override
	public Vector2 getPosition(final double t)
	{
		return child.getPosition(t).turn(rotation).add(initialPos);
	}


	@Override
	public Vector2 getVelocity(final double t)
	{
		return child.getVelocity(t).turn(rotation);
	}


	@Override
	public Vector2 getAcceleration(final double t)
	{
		return child.getAcceleration(t).turn(rotation);
	}


	@Override
	public double getTotalTime()
	{
		return Math.max(child.x.getTotalTime(), child.y.getTotalTime());
	}


	@Override
	public double getTotalTimeToPrimaryDirection()
	{
		return child.y.getTotalTime();
	}


	@Override
	public BangBangTrajectory2DAsync mirrored()
	{
		return new BangBangTrajectory2DAsync(
				child,
				initialPos.multiplyNew(-1),
				AngleMath.normalizeAngle(rotation + AngleMath.PI));
	}


	@Override
	public PosVelAcc<IVector2> getValuesAtTime(final double tt)
	{
		PosVelAcc<IVector2> valuesAtTime = child.getValuesAtTime(tt);
		return new PosVelAcc<>(
				valuesAtTime.getPos().turnNew(rotation).add(initialPos),
				valuesAtTime.getVel().turnNew(rotation),
				valuesAtTime.getAcc().turnNew(rotation)
		);
	}


	@Override
	public List<Double> getTimeSections()
	{
		return child.getTimeSections();
	}
}
