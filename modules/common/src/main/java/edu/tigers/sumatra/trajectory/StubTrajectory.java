/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.trajectory;

import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.math.vector.Vector3f;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.Collections;
import java.util.List;


/**
 * @param <T> Type of Trajectory
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StubTrajectory<T> implements ITrajectory<T>
{
	private final T posMM;
	private final T pos;
	private final T vel;
	private final T acc;


	public StubTrajectory()
	{
		posMM = null;
		pos = null;
		vel = null;
		acc = null;
	}


	/**
	 * @param posMM static position in [mm]
	 * @return stub trajectory
	 */
	public static ITrajectory<IVector3> vector3Static(IVector3 posMM)
	{
		return new StubTrajectory<>(posMM, posMM.multiplyNew(Vector3.fromXYZ(1e-3, 1e-3, 1)), Vector3f.ZERO_VECTOR,
				Vector3f.ZERO_VECTOR);
	}


	@Override
	public T getPositionMM(final double t)
	{
		return posMM;
	}


	@Override
	public T getPosition(final double t)
	{
		return pos;
	}


	@Override
	public T getVelocity(final double t)
	{
		return vel;
	}


	@Override
	public T getAcceleration(final double t)
	{
		return acc;
	}


	@Override
	public double getTotalTime()
	{
		return 0;
	}


	@Override
	public T getNextDestination(final double t)
	{
		return posMM;
	}


	@Override
	public T getFinalDestination()
	{
		return posMM;
	}


	@Override
	public PosVelAcc<T> getValuesAtTime(final double tt)
	{
		return new PosVelAcc<>(getPosition(tt), getVelocity(tt), getAcceleration(tt));
	}


	@Override
	public List<Double> getTimeSections()
	{
		return Collections.emptyList();
	}


	@SuppressWarnings({ "unchecked", "squid:S1905" }) // fp: cast is required here
	@Override
	public StubTrajectory<T> mirrored()
	{
		assert posMM != null;
		assert pos != null;
		assert vel != null;
		assert acc != null;
		if (posMM instanceof Double)
		{
			return (StubTrajectory<T>) new StubTrajectory<>(-(Double) posMM, -(Double) pos, -(Double) vel, -(Double) acc);
		} else if (posMM instanceof IVector2)
		{
			return (StubTrajectory<T>) new StubTrajectory<>(((IVector2) posMM).multiplyNew(-1),
					((IVector2) pos).multiplyNew(-1),
					((IVector2) vel).multiplyNew(-1), ((IVector2) acc).multiplyNew(-1));
		} else if (posMM instanceof IVector3 pos3)
		{
			IVector3 newPosMM = Vector3.from2d(((pos3).getXYVector()).multiplyNew(-1),
					AngleMath.normalizeAngle((pos3).z() + AngleMath.PI));
			IVector3 newPos = Vector3.from2d((((IVector3) pos).getXYVector()).multiplyNew(-1),
					AngleMath.normalizeAngle(((IVector3) pos).z() + AngleMath.PI));
			IVector3 newVel = Vector3.from2d((((IVector3) vel).getXYVector()).multiplyNew(-1),
					((IVector3) vel).z() + AngleMath.PI);
			IVector3 newAcc = Vector3.from2d((((IVector3) acc).getXYVector()).multiplyNew(-1),
					((IVector3) acc).z() + AngleMath.PI);
			return (StubTrajectory<T>) new StubTrajectory<>(newPosMM, newPos, newVel, newAcc);
		}
		throw new IllegalStateException("Unsupported generic type: " + posMM.getClass());
	}


	@Override
	public double getMaxSpeed()
	{
		return 0.0;
	}
}
