/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math;

import lombok.RequiredArgsConstructor;

import java.util.function.BooleanSupplier;
import java.util.function.LongSupplier;


/**
 * Debounce a boolean function for a set duration, before it becomes true.
 * When the boolean function returns false, the value is immediately reset to false.
 */
@RequiredArgsConstructor
public class DebounceTruthy
{
	private final BooleanSupplier fn;
	private final LongSupplier timestamp;
	private final double duration;

	private boolean initialized = false;
	private long firstTruthyTimestamp;


	public boolean get()
	{
		if (!initialized)
		{
			if (fn.getAsBoolean())
			{
				firstTruthyTimestamp = timestamp.getAsLong() - (long) (duration * 1e9);
			}
			initialized = true;
		}
		if (fn.getAsBoolean())
		{
			if (firstTruthyTimestamp == 0)
			{
				firstTruthyTimestamp = timestamp.getAsLong();
			}
		} else
		{
			firstTruthyTimestamp = 0;
		}
		return firstTruthyTimestamp != 0 && (timestamp.getAsLong() - firstTruthyTimestamp) / 1e9 >= duration;
	}
}
