/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.trajectory;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.math.vector.Vector3f;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * @param <T> Type of Trajectory
 */
@Persistent
public class StubTrajectory<T> implements ITrajectory<T>
{
	
	private final T posMM;
	private final T pos;
	private final T vel;
	private final T acc;
	
	
	@SuppressWarnings("unused")
	StubTrajectory()
	{
		posMM = null;
		pos = null;
		vel = null;
		acc = null;
	}
	
	
	/**
	 * Create trajectory with zero time and a static position.
	 * 
	 * @param posMM
	 * @param pos
	 * @param vel
	 * @param acc
	 */
	public StubTrajectory(final T posMM, final T pos, final T vel, final T acc)
	{
		this.posMM = posMM;
		this.pos = pos;
		this.vel = vel;
		this.acc = acc;
	}
	
	
	/**
	 * @return stub trajectory
	 */
	public static ITrajectory<Double> scalarZero()
	{
		return new StubTrajectory<>(0.0, 0.0, 0.0, 0.0);
	}
	
	
	/**
	 * @param posMM static position in [mm]
	 * @return stub trajectory
	 */
	public static ITrajectory<Double> vector2Static(double posMM)
	{
		return new StubTrajectory<>(posMM, posMM / 1000, 0.0, 0.0);
	}
	
	
	/**
	 * @return stub trajectory
	 */
	public static ITrajectory<IVector2> vector2Zero()
	{
		return new StubTrajectory<>(Vector2f.ZERO_VECTOR, Vector2f.ZERO_VECTOR, Vector2f.ZERO_VECTOR,
				Vector2f.ZERO_VECTOR);
	}
	
	
	/**
	 * @param posMM static position in [mm]
	 * @return stub trajectory
	 */
	public static ITrajectory<IVector2> vector2Static(IVector2 posMM)
	{
		return new StubTrajectory<>(posMM, posMM.multiplyNew(1e-3), Vector2f.ZERO_VECTOR, Vector2f.ZERO_VECTOR);
	}
	
	
	/**
	 * @return stub trajectory
	 */
	public static ITrajectory<IVector3> vector3Zero()
	{
		return new StubTrajectory<>(Vector3f.ZERO_VECTOR, Vector3f.ZERO_VECTOR, Vector3f.ZERO_VECTOR,
				Vector3f.ZERO_VECTOR);
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
	
	
	@SuppressWarnings("unchecked")
	@Override
	public StubTrajectory mirrored()
	{
		assert posMM != null;
		assert pos != null;
		assert vel != null;
		assert acc != null;
		if (posMM instanceof Double)
		{
			return new StubTrajectory(-(Double) posMM, -(Double) pos, -(Double) vel, -(Double) acc);
		} else if (posMM instanceof IVector2)
		{
			return new StubTrajectory(((IVector2) posMM).multiplyNew(-1), ((IVector2) pos).multiplyNew(-1),
					((IVector2) vel).multiplyNew(-1), ((IVector2) acc).multiplyNew(-1));
		} else if (posMM instanceof IVector3)
		{
			IVector3 newPosMM = Vector3.from2d((((IVector3) posMM).getXYVector()).multiplyNew(-1),
					AngleMath.normalizeAngle(((IVector3) posMM).z() + AngleMath.PI));
			IVector3 newPos = Vector3.from2d((((IVector3) pos).getXYVector()).multiplyNew(-1),
					AngleMath.normalizeAngle(((IVector3) pos).z() + AngleMath.PI));
			IVector3 newVel = Vector3.from2d((((IVector3) vel).getXYVector()).multiplyNew(-1),
					((IVector3) vel).z() + AngleMath.PI);
			IVector3 newAcc = Vector3.from2d((((IVector3) acc).getXYVector()).multiplyNew(-1),
					((IVector3) acc).z() + AngleMath.PI);
			return new StubTrajectory(newPosMM, newPos, newVel, newAcc);
		}
		throw new IllegalStateException("Unsupported generic type: " + posMM.getClass());
	}
}
