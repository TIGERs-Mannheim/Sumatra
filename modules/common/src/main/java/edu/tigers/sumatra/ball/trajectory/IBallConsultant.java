/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ball.trajectory;

/**
 * Consultant for chipped balls.
 */
public interface IBallConsultant
{
	/**
	 * Calculate the time for a chip kick to the specified target
	 *
	 * @param distance  the pass distance [mm]
	 * @param kickSpeed the initial absolute chip kick speed [m/s]
	 * @return the duration of the kick until the ball reaches target
	 */
	double getTimeForKick(double distance, double kickSpeed);


	/**
	 * Get the velocity after <code>travelTime</code> for a kick with <code>kickSpeed</code>
	 *
	 * @param kickSpeed  [m/s]
	 * @param travelTime [s]
	 * @return velocity [m/s]
	 */
	double getVelForKickByTime(final double kickSpeed, final double travelTime);
}
