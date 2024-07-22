/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.data;

import lombok.Setter;
import lombok.Value;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Optional;


/**
 * A time-based buffer that removes the oldest elements based on duration and size.
 *
 * @param <T> the type of the buffered value.
 */
public class TimeLimitedBuffer<T>
{
	private final Deque<Entry<T>> data = new ArrayDeque<>();
	@Setter
	private int maxElements;
	@Setter
	private double maxDuration;


	/**
	 * Add a new entry and reduce the buffer, if required.
	 *
	 * @param timestamp
	 * @param value
	 */
	public void add(long timestamp, T value)
	{
		data.add(new Entry<>(timestamp, value));
		reduceBySize();
		reduceByDuration();
	}


	/**
	 * @return the current list of stored elements.
	 */
	public List<T> getElements()
	{
		return data.stream().map(Entry::getValue).toList();
	}


	public Optional<T> getLatest()
	{
		return data.isEmpty() ? Optional.empty() : Optional.of(data.getLast().value);
	}


	public Optional<T> getOldest()
	{
		return data.isEmpty() ? Optional.empty() : Optional.of(data.getFirst().value);
	}

	public T getValuePercentile(double percentile)
	{
		return data.stream()
				.map(Entry::getValue)
				.sorted()
				.skip(Math.max(0, Math.round(data.size() * percentile) - 1))
				.findFirst()
				.orElseThrow();
	}


	private void reduceBySize()
	{
		if (maxElements <= 0)
		{
			// No size limit
			return;
		}
		while (data.size() > maxElements)
		{
			data.removeFirst();
		}
	}


	private void reduceByDuration()
	{
		if(data.isEmpty())
		{
			return;
		}

		reduceByAbsoluteDuration(data.getLast().timestamp);
	}

	public void reduceByAbsoluteDuration(long currentTimestamp)
	{
		if (maxDuration <= 0)
		{
			// no duration limit
			return;
		}

		while (!data.isEmpty() && data.getFirst().timestamp < currentTimestamp - (long) (maxDuration * 1e9))
		{
			data.removeFirst();
		}
	}

	public void reset()
	{
		data.clear();
	}

	@Value
	private static class Entry<T> implements Comparable<Entry<T>>
	{
		long timestamp;
		T value;


		@Override
		public int compareTo(Entry<T> o)
		{
			return Long.compare(timestamp, o.timestamp);
		}
	}
}
