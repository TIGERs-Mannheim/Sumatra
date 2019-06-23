/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.trajectory;

import edu.tigers.sumatra.math.IMirrorable;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
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
	 * @return velocity
	 */
	T getVelocity(double t);
	
	
	/**
	 * Get acceleration at a certain time.
	 * 
	 * @param t time
	 * @return acceleration
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
}
