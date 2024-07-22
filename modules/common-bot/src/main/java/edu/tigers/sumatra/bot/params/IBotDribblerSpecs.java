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
	 * @return Dribbling speed in [m/s] in default mode.
	 */
	double getDefaultSpeed();

	/**
	 * @return Maximum dribbler force in [N] in default mode.
	 */
	double getDefaultForce();

	/**
	 * @return Dribbling speed in [m/s] in high power mode.
	 */
	double getHighPowerSpeed();

	/**
	 * @return Maximum dribbler force in [N] in high power mode.
	 */
	double getHighPowerForce();
}