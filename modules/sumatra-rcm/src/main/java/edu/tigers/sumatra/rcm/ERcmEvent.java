/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.rcm;

/**
 * Possible events that can be send from controller to RCM
 */
public enum ERcmEvent
{
	UNASSIGNED,
	NEXT_BOT,
	PREV_BOT,
	UNASSIGN_BOT,
	SPEED_MODE_TOGGLE,
	SPEED_MODE_ENABLE,
	SPEED_MODE_DISABLE,
}
