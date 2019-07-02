/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.referee.data;

/**
 * All available, team independent game states
 */
public enum EGameState
{
	UNKNOWN,
	HALT,
	STOP,
	RUNNING,
	TIMEOUT,
	BREAK,
	POST_GAME,

	PREPARE_KICKOFF,
	KICKOFF,
	PREPARE_PENALTY,
	PENALTY,
	DIRECT_FREE,
	INDIRECT_FREE,
	BALL_PLACEMENT,

	;
}
