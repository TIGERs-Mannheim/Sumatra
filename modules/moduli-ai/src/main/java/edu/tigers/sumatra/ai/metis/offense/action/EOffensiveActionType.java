/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.action;

public enum EOffensiveActionType
{
	PASS,
	/**
	 * A Redirect is also either a Kick or a Pass
	 */
	KICK,
	REDIRECT_KICK,
	DRIBBLE_KICK,
	PROTECT,
	CHOP_TRICK,
	RECEIVE
}
