/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ball.trajectory;


import org.apache.commons.lang.NotImplementedException;


/**
 * Consultant for straight kicked balls.
 */
public interface IFlatBallConsultant extends IBallConsultant
{
	/**
	 * Get the initial velocity such that the ball has the given velocity after given time
	 *
	 * @param endVel [m/s]
	 * @param time   [s]
	 * @return initial velocity [m/s]
	 */
	double getInitVelForTime(final double endVel, final double time);


	/**
	 * Get the initial velocity such that the ball has travelled the given distance after given time
	 *
	 * @param distance [mm]
	 * @param time     [s]
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
	 * @param endVel   [m/s]
	 * @return initial velocity [m/s]
	 */
	double getInitVelForDist(final double distance, final double endVel);
}
