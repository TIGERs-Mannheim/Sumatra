/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.gamelog;

import lombok.AllArgsConstructor;
import lombok.Getter;


/**
 * Container for a logged message. Just bundles time, type, and binary data.
 */
@Getter
@AllArgsConstructor
public class GameLogMessage
{
	/** Receiver timestamp in ns. */
	private long timestampNs;

	private EMessageType type;

	/** Binary message data. */
	private byte[] data;

	public void adjustTimestamp(final long adj)
	{
		timestampNs += adj;
	}
}
