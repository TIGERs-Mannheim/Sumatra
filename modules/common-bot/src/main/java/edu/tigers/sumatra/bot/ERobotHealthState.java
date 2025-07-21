/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.bot;

public enum ERobotHealthState
{
	/** Robot is fully operational :) */
	READY,

	/** Robot has some defect but can still make it to the interchange position */
	DEGRADED,

	/** Robot is completely useless, can be empty, not on the field, burning, etc. It won't make it to interchange. */
	UNUSABLE,
}
