/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.action;

/**
 * defines offensive Actions
 */
public enum EOffensiveAction
{
	/**
	 * Pass ball to another tiger
	 */
	PASS,
	/**
	 * Kick the ball into a free area on the field
	 */
	KICK_INS_BLAUE,
	/**
	 * Kick ball away of current situation (if no better way is found)
	 */
	CLEARING_KICK,
	/**
	 * Simply shoot on goal, without any steps in between
	 */
	GOAL_SHOT,
	/**
	 * Protect the ball and buy some time
	 */
	PROTECT,
	/**
	 * Everything related to redirecting, can be pass or goal_shot
	 */
	REDIRECT,
	/**
	 * Receive the ball without the intention to redirect it immediately
	 */
	RECEIVE,
}
