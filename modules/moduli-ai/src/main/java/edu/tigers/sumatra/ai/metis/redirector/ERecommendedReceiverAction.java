/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.redirector;

/**
 * Describes a recommended action that a receiver can take to better catch the ball
 */
public enum ERecommendedReceiverAction
{
	/**
	 * Do nothing special, just catch the ball as usually
	 */
	NONE,
	/**
	 * Disrupt enemy receiver to lesser his chance of a successful redirect
	 */
	DISRUPT_ENEMY,
	/**
	 * Try to overtake the enemy
	 */
	CATCH_BEFORE_ENEMY,
	/**
	 * Use to offensive to receive the ball
	 */
	DOUBLE_ATTACKER
}
