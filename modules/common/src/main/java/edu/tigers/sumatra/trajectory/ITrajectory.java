/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.trajectory;

import edu.tigers.sumatra.math.IMirrorable;

import java.util.List;


/**
 * Generic trajectory with position, velocity and acceleration.
 *
 * @param <T>
 */
public interface ITrajectory<T> extends IMirrorable<ITrajectory<T>>
{
	/**
	 * Get position at time t.
	 *
	 * @param t time
	 * @return position [mm]
	 */
	T getPositionMM(double t);


	/**
	 * Get position at time t.
	 *
	 * @param t time
	 * @return position [m]
	 */
	T getPosition(double t);


	/**
	 * Get velocity at a certain time.
	 *
	 * @param t time
	 * @return velocity [m/s]
	 */
	T getVelocity(double t);


	/**
	 * Get acceleration at a certain time.
	 *
	 * @param t time
	 * @return acceleration [m/s²]
	 */
	T getAcceleration(double t);


	/**
	 * Get total runtime.
	 *
	 * @return total time for trajectory
	 */
	double getTotalTime();


	/**
	 * @param t time [s]
	 * @return the next destination, if this trajectory is divided into multiple subtract-pathes
	 */
	default T getNextDestination(final double t)
	{
		return getPositionMM(getTotalTime());
	}


	/**
	 * @return the final position in this trajectory
	 */
	default T getFinalDestination()
	{
		return getPositionMM(getTotalTime());
	}

	/**
	 * Get the full state at a certain time.
	 *
	 * @param tt time
	 * @return full state
	 */
	default PosVelAcc<T> getValuesAtTime(final double tt)
	{
		throw new IllegalStateException("Not implemented");
	}

	/**
	 * Get a list of sections based on `tEnd`.
	 *
	 * @return the list of sections
	 */
	default List<Double> getTimeSections()
	{
		throw new IllegalStateException("Not implemented");
	}


	default double getTotalTimeToPrimaryDirection()
	{
		return getTotalTime();
	}
}
