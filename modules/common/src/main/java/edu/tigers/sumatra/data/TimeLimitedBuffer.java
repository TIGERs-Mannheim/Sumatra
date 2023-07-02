/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.data;

import lombok.Setter;
import lombok.Value;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * A time-based buffer that removes the oldest elements based on duration and size.
 *
 * @param <T> the type of the buffered value.
 */
public class TimeLimitedBuffer<T>
{
	private final List<Entry<T>> data = new ArrayList<>();
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
		Collections.sort(data);
		reduceBySize();
		reduceByDuration();
	}


	/**
	 * @return the current list of stored elements.
	 */
	public List<T> getElements()
	{
		return data.stream().map(Entry::getValue).collect(Collectors.toUnmodifiableList());
	}


	public Optional<T> getLatest()
	{
		return data.stream().skip(data.size() - 1L).map(Entry::getValue).findAny();
	}


	public Optional<T> getOldest()
	{
		return data.stream().map(Entry::getValue).findFirst();
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
			data.remove(0);
		}
	}


	private void reduceByDuration()
	{
		if (maxDuration <= 0 || data.isEmpty())
		{
			// no duration limit
			return;
		}
		long maxTimestamp = data.get(data.size() - 1).timestamp;
		long minTimestamp = maxTimestamp - (long) (maxDuration * 1e9);
		while (!data.isEmpty() && data.get(0).timestamp < minTimestamp)
		{
			data.remove(0);
		}
	}

	public void reset()
	{
		data.clear();
	}

	public void reduceByAbsoluteDuration(long currentTimestamp)
	{
		if (maxDuration <= 0 || data.isEmpty())
		{
			// no duration limit
			return;
		}
		while (!data.isEmpty() && data.get(0).timestamp < currentTimestamp - (long) (maxDuration * 1e9))
		{
			data.remove(0);
		}
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
