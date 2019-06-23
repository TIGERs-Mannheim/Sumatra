/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.referee.data;

/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public enum EGameState
{
	/**  */
	HALT,
	/**  */
	STOP,
	/**  */
	RUNNING,
	/**  */
	TIMEOUT,
	/**  */
	BREAK,
	/**  */
	POST_GAME,
	
	/**  */
	PREPARE_KICKOFF,
	/**  */
	KICKOFF,
	/**  */
	PREPARE_PENALTY,
	/**  */
	PENALTY,
	/** */
	DIRECT_FREE,
	/** */
	INDIRECT_FREE,
	/**  */
	BALL_PLACEMENT,;
}
