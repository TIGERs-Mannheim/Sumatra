/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.gamelog;

public enum GameLogCompareResult
{
	/** Use IGNORE if a message is not applicable for comparison. */
	IGNORE,
	/** Message matches some condition. */
	MATCH,
	/** Message does not match some condition. */
	MISMATCH,
}
