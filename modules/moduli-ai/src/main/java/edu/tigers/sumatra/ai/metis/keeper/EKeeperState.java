/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.keeper;

import edu.tigers.sumatra.statemachine.IEvent;


/**
 * This enum represents all possible states of the Keeper
 */
public enum EKeeperState implements IEvent
{
	CRITICAL,
	MOVE_TO_PENALTY_AREA,
	CHIP_FAST,
	BALL_PLACEMENT,
	STOPPED,
	PULL_BACK,
	
	// for oneOnOne keeper
	CATCH_OVER_CHIP,
	INTERCEPT_AND_GO_OUT,
	RAMBO
}
