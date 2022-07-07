/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.pass.rating;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


/**
 * Types of rating a pass.
 */
@RequiredArgsConstructor
public enum EPassRating
{
	/**
	 * Rating of how well the pass can be performed in terms of kick speed and receiving speed
	 */
	PASSABILITY("pa"),

	/**
	 * Rating of how well opponents can intercept the pass
	 */
	INTERCEPTION("i"),

	/**
	 * Scoring goals by redirect from the pass target
	 */
	REFLECT_GOAL_KICK("r"),

	/**
	 * Scoring goals by kicking stopped ball from the pass target
	 */
	GOAL_KICK("g"),

	/**
	 * Rating of the pressure made on the opponents
	 */
	PRESSURE("pr"),

	;

	@Getter
	private final String abbreviation;
}
