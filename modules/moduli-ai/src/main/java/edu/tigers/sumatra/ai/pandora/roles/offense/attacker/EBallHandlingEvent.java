/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.offense.attacker;

import edu.tigers.sumatra.statemachine.IEvent;


public enum EBallHandlingEvent implements IEvent
{
	BALL_MOVES,
	BALL_MOVES_AWAY_FROM_ME,
	BALL_MOVES_TOWARDS_ME,
	BALL_STOPPED_BY_BOT,
	BALL_STOPPED_MOVING,
	BALL_LOST,
	
	BALL_APPROACHED,
	BALL_LINE_APPROACHED,
	BALL_RECEIVED,
	BALL_NOT_RECEIVED,
	BALL_NOT_REDIRECTED,
	BALL_NOT_REDIRECTABLE,
	
	SWITCH_TO_REDIRECT,
	
	BALL_POSSESSION_THREATENED,
	BALL_POSSESSION_SAVE,
	
	FREE_KICK,
	SWITCH_TO_RUN_UP,
	
	BALL_KICKED
}
