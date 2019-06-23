/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.strategy;

import edu.tigers.sumatra.statemachine.IEvent;


/**
 * Defines the possible strategies in offense
 */
public enum EOffensiveStrategy implements IEvent
{
	KICK,
	STOP,
	INTERCEPT,
	RECEIVE_PASS,
	DELAY,
	SUPPORTIVE_ATTACKER,
	FREE_SKIRMISH
}
