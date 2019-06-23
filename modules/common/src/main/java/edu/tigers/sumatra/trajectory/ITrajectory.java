/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 24, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.trajectory;

/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * @param <T>
 */
public interface ITrajectory<T>
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
