/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.clock;

import java.time.Instant;


/**
 * Utility class to get Unix timestamp-based nano-thingies.
 */
public class NanoTime
{
	private NanoTime()
	{
	}


	public static long getTimestampNow()
	{
		var now = Instant.now();
		return now.getEpochSecond() * 1_000_000_000L + now.getNano();
	}
}
