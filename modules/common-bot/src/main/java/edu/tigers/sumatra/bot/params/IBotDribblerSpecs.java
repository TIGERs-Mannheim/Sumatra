/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.bot.params;


/**
 * Specs for the robot dribbler unit.
 */
public interface IBotDribblerSpecs
{
	/**
	 * @return Maximum acceleration the dribbler can put on the ball.
	 */
	double getMaxBallAcceleration();

	/**
	 * @return Angle from robot negative Y axis (aka back) where a ball can be kept at dribbler [rad].
	 */
	double getMaxRetainingBallAngle();

	/**
	 * @return Dribbling speed in [rpm] in default mode.
	 */
	double getDefaultSpeed();

	/**
	 * @return Maximum dribbler current in [A] in default mode.
	 */
	double getDefaultMaxCurrent();

	/**
	 * @return Dribbling speed in [rpm] in high power mode.
	 */
	double getHighPowerSpeed();

	/**
	 * @return Maximum dribbler current in [A] in high power mode.
	 */
	double getHighPowerMaxCurrent();
}