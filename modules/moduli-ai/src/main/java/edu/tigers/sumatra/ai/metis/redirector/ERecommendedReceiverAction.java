/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.redirector;

/**
 * Describes a recommended action that a receiver can take to better catch the ball
 */
public enum ERecommendedReceiverAction
{
	/**
	 * Do nothing special, just catch the ball as usual
	 */
	NONE,
	/**
	 * Disrupt opponent receiver to lesser his chance of a successful redirect
	 */
	DISRUPT_OPPONENT,
	/**
	 * Use two attackers to receive the ball
	 */
	DOUBLE_ATTACKER,
}
