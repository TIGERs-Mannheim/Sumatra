/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.offense.attacker.states;


import edu.tigers.sumatra.statemachine.IEvent;


public enum EAttackerState implements IEvent
{
	APPROACH_AND_STOP_BALL,
	APPROACH_BALL_LINE,
	DRIBBLE,
	DRIBBLING_KICK,
	FREE_KICK,
	KICK,
	PROTECT,
	SKIRMISH,
	RECEIVE,
	REDIRECT
}