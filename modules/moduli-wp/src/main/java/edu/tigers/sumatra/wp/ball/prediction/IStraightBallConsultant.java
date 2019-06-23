/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.ball.prediction;


import org.apache.commons.lang.NotImplementedException;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface IStraightBallConsultant
{
	/**
	 * Get the initial velocity such that the ball has the given velocity after given time
	 *
	 * @param endVel [m/s]
	 * @param time [s]
	 * @return initial velocity [m/s]
	 */
	double getInitVelForTime(final double endVel, final double time);
	
	
	/**
	 * Get the velocity after <code>travelTime</code> for a kick with <code>kickSpeed</code>
	 * 
	 * @param kickSpeed [m/s]
	 * @param travelTime [s]
	 * @return velocity [m/s]
	 */
	double getVelForKickByTime(double kickSpeed, double travelTime);
	
	
	/**
	 * Get the initial velocity such that the ball has travelled the given distance after given time
	 *
	 * @param distance [mm]
	 * @param time [s]
	 * @return initial velocity [m/s]
	 */
	default double getInitVelForTimeDist(final double distance, final double time)
	{
		throw new NotImplementedException("remove me when implemented");
	}
	
	
	/**
	 * Get the initial velocity such that the ball travels the given distance and has the given end velocity.
	 *
	 * @param distance [mm]
	 * @param endVel [m/s]
	 * @return initial velocity [m/s]
	 */
	double getInitVelForDist(final double distance, final double endVel);
	
	
	/**
	 * Get the time duration that the ball requires to travel given distance when kicked with given velocity.
	 * 
	 * @param distance in [mm]
	 * @param kickVel in [m/s]
	 * @return the time duration in [s]
	 */
	double getTimeForKick(final double distance, final double kickVel);
}
