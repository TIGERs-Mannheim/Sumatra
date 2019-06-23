/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 13, 2016
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.filter;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * @param <TYPE>
 */
public class DataSync<TYPE>
{
	private List<DataStore>	buffer	= new LinkedList<>();
	private final int			bufferSize;
	
	
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
	public synchronized void add(final long timestamp, final TYPE data)
	{
		DataStore store = new DataStore();
		store.timestamp = timestamp;
		store.data = data;
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
	public synchronized Optional<TYPE> get(final long timestamp)
	{
		DataStore candidate = null;
		for (DataStore ds : buffer)
		{
			if (ds.timestamp > timestamp)
			{
				if (candidate == null)
				{
					candidate = ds;
				} else
				{
					long diff1 = Math.abs(candidate.timestamp - timestamp);
					long diff2 = Math.abs(ds.timestamp - timestamp);
					if (diff1 > diff2)
					{
						return Optional.of(ds.data);
					}
					return Optional.of(candidate.data);
				}
			}
		}
		if (candidate != null)
		{
			return Optional.of(candidate.data);
		}
		if (!buffer.isEmpty())
		{
			return Optional.of(buffer.get(buffer.size() - 1).data);
		}
		return Optional.empty();
	}
	
	
	private class DataStore
	{
		long	timestamp;
		TYPE	data;
		
	}
}
