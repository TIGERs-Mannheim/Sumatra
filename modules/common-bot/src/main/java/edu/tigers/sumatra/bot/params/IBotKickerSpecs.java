/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.bot.params;


/**
 * Specs for the robot kicker-dribbler unit.
 */
public interface IBotKickerSpecs
{
	/**
	 * @return the chipAngle
	 */
	double getChipAngle();


	/**
	 * @return the maxAbsoluteChipVelocity
	 */
	double getMaxAbsoluteChipVelocity();


	/**
	 * @return the maxAbsoluteStraightVelocity
	 */
	double getMaxAbsoluteStraightVelocity();

	/**
	 * @return max dribble speed to apply on robot
	 */
	double getMaxDribbleSpeed();

	/**
	 * @return gain factor to apply to dribble speed from skill
	 */
	double getDribbleSpeedGain();
}