/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.trajectory;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector3;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * @param <T> Type of Trajectory
 */
@Persistent
public class StubTrajectory<T> implements ITrajectory<T>
{
	
	private final T	posMM;
	private final T	pos;
	private final T	vel;
	private final T	acc;
	
	
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
		return new StubTrajectory<>(Vector2.ZERO_VECTOR, Vector2.ZERO_VECTOR, Vector2.ZERO_VECTOR, Vector2.ZERO_VECTOR);
	}
	
	
	/**
	 * @param posMM static position in [mm]
	 * @return stub trajectory
	 */
	public static ITrajectory<IVector2> vector2Static(IVector2 posMM)
	{
		return new StubTrajectory<>(posMM, posMM.multiplyNew(1e-3), Vector2.ZERO_VECTOR, Vector2.ZERO_VECTOR);
	}
	
	
	/**
	 * @return stub trajectory
	 */
	public static ITrajectory<IVector3> vector3Zero()
	{
		return new StubTrajectory<>(Vector3.ZERO_VECTOR, Vector3.ZERO_VECTOR, Vector3.ZERO_VECTOR, Vector3.ZERO_VECTOR);
	}
	
	
	/**
	 * @param posMM static position in [mm]
	 * @return stub trajectory
	 */
	public static ITrajectory<IVector3> vector3Static(IVector3 posMM)
	{
		return new StubTrajectory<>(posMM, posMM.multiplyNew(Vector3.fromXYZ(1e-3, 1e-3, 1)), Vector3.ZERO_VECTOR,
				Vector3.ZERO_VECTOR);
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
}
