/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.action;

/**
 * defines offensive Actions
 */
public enum EOffensiveAction
{
	/** PASS: Pass ball to another tiger */
	PASS,
	/** */
	KICKOFF,
	/** MOVING_KICK: first Kick ball beside foeBot, then go there and kick to goal. */
	KICK_INS_BLAUE,
	/** PUSHING_KICK: Go in front of foeBot to prevent him kicking the ball. */
	PUSHING_KICK,
	/** CLEARING_KICK: Kick ball away of current situation (if no better way is found) */
	CLEARING_KICK,
	/** PULL_BACK: Pull back ball with dribbler usage, then kick */
	PULL_BACK,
	/** GOAL_SHOT: Simply shoot on goal, without any steps in between */
	GOAL_SHOT,
	/** FINISHER_KICK: do some nice IFinisher move to trick opponents */
	FINISHER_KICK,
	/** PROTECT: protect the ball and buy some time */
	PROTECT,
	/**
	 * Everything related to redirecting, can be pass or goal_shot
	 */
	REDIRECT,
	/**
	 * CATCH
	 */
	CATCH
}
