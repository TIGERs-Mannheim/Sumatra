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
 * @param <RETURN_TYPE>
 */
public interface ITrajectory<RETURN_TYPE>
{
	/**
	 * Get position at time t.
	 * 
	 * @param t time
	 * @return position [mm]
	 */
	RETURN_TYPE getPositionMM(double t);
	
	
	/**
	 * Get position at time t.
	 * 
	 * @param t time
	 * @return position [m]
	 */
	RETURN_TYPE getPosition(double t);
	
	
	/**
	 * Get velocity at a certain time.
	 * 
	 * @param t time
	 * @return velocity
	 */
	RETURN_TYPE getVelocity(double t);
	
	
	/**
	 * Get acceleration at a certain time.
	 * 
	 * @param t time
	 * @return acceleration
	 */
	RETURN_TYPE getAcceleration(double t);
	
	
	/**
	 * Get total runtime.
	 * 
	 * @return total time for trajectory
	 */
	double getTotalTime();
	
	
	/**
	 * @param t
	 * @return
	 */
	default RETURN_TYPE getNextDestination(final double t)
	{
		return getPositionMM(getTotalTime());
	}
}
