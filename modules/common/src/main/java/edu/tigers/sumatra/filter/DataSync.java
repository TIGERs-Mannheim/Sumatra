/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.filter;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.log4j.Logger;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * @param <T>
 */
public class DataSync<T>
{
	private static final Logger log = Logger.getLogger(DataSync.class.getName());
	
	private final int bufferSize;
	private List<DataStore> buffer = new LinkedList<>();
	
	
	/**
	 * @param bufferSize
	 */
	public DataSync(final int bufferSize)
	{
		this.bufferSize = bufferSize;
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
			log.debug("Clearing buffer, since incoming timestamp is smaller than buffered timestamp (" + timestamp + "<"
					+ buffer.get(0).getTimestamp() + ")");
			buffer.clear();
		}
		if (buffer.size() >= bufferSize)
		{
			buffer.remove(0);
		}
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
				assert previous.getTimestamp() <= timestamp;
				assert current.getTimestamp() >= timestamp;
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
	
	
	public synchronized void reset()
	{
		buffer.clear();
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
			return new ToStringBuilder(this)
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
		
		
		@Override
		public String toString()
		{
			return new ToStringBuilder(this)
					.append("first", first)
					.append("second", second)
					.toString();
		}
	}
}
