/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.filter;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;


/**
 * Store arbitrary data by timestamp and retrieve it as the closest pair to a given timestamp.
 *
 * @param <T> the data type to be stored along with a timestamp
 */
public class DataSync<T extends IInterpolatable<T>>
{
	private final long horizon;
	private List<DataStore> buffer = new LinkedList<>();


	/**
	 * @param horizon the time horizon [s] of the buffer
	 */
	public DataSync(final double horizon)
	{
		this.horizon = Math.round(horizon * 1e9);
	}


	/**
	 * @param timestamp
	 * @param data
	 */
	public synchronized void add(final long timestamp, final T data)
	{
		DataStore store = new DataStore(timestamp, data);
		if (!buffer.isEmpty() && timestamp < buffer.get(0).getTimestamp())
		{
			buffer.clear();
		}
		buffer.removeIf(d -> d.timestamp < timestamp - horizon);
		buffer.add(store);
	}


	/**
	 * Get the closest data to given timestamp
	 *
	 * @param timestamp
	 * @return
	 */
	public synchronized Optional<DataPair> get(final long timestamp)
	{
		if (buffer.size() < 2 || buffer.get(0).getTimestamp() > timestamp
				|| buffer.get(buffer.size() - 1).getTimestamp() < timestamp)
		{
			return Optional.empty();
		}

		DataStore previous = null;
		for (DataStore current : buffer)
		{
			if (current.timestamp >= timestamp && previous != null)
			{
				Validate.isTrue(previous.getTimestamp() <= timestamp);
				Validate.isTrue(current.getTimestamp() >= timestamp);
				return Optional.of(new DataPair(previous, current));
			}
			previous = current;
		}
		return Optional.empty();
	}


	public synchronized Optional<DataStore> getLatest()
	{
		if (buffer.isEmpty())
		{
			return Optional.empty();
		}
		return Optional.of(buffer.get(buffer.size() - 1));
	}


	public synchronized Optional<DataStore> getOldest()
	{
		if (buffer.isEmpty())
		{
			return Optional.empty();
		}
		return Optional.of(buffer.get(0));
	}


	public synchronized void reset()
	{
		buffer.clear();
	}


	public List<DataStore> getBuffer()
	{
		return Collections.unmodifiableList(buffer);
	}

	public class DataStore
	{
		private long timestamp;
		private T data;


		public DataStore(final long timestamp, final T data)
		{
			this.timestamp = timestamp;
			this.data = data;
		}


		public long getTimestamp()
		{
			return timestamp;
		}


		public T getData()
		{
			return data;
		}


		@Override
		public String toString()
		{
			return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
					.append("timestamp", timestamp)
					.append("data", data)
					.toString();
		}
	}

	public class DataPair
	{
		private DataStore first;
		private DataStore second;


		public DataPair(final DataStore first, final DataStore second)
		{
			this.first = first;
			this.second = second;
		}


		public DataStore getFirst()
		{
			return first;
		}


		public DataStore getSecond()
		{
			return second;
		}


		public T interpolate(long timestamp)
		{
			assert first.getTimestamp() <= timestamp : first.getTimestamp() + " " + timestamp;
			assert second.getTimestamp() >= timestamp : second.getTimestamp() + " " + timestamp;
			long timeDiff = second.getTimestamp() - first.getTimestamp();
			if (timeDiff == 0)
			{
				return first.getData();
			}
			assert timeDiff >= 0 : timeDiff;
			long targetToSecond = timestamp - first.getTimestamp();
			assert targetToSecond >= 0 : targetToSecond;
			double percentageOfSecond = (double) targetToSecond / timeDiff;
			assert percentageOfSecond >= 0.0 : percentageOfSecond;
			assert percentageOfSecond <= 1.0 : percentageOfSecond;
			return first.getData().interpolate(second.getData(), percentageOfSecond);
		}


		@Override
		public String toString()
		{
			return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
					.append("first", first)
					.append("second", second)
					.toString();
		}
	}
}
