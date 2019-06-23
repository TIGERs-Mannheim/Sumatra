/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;


/**
 * Buffer data based on the timestamp span
 * 
 * @param <T> the data type to buffer
 */
public class TimestampBasedBuffer<T extends ITimestampBased>
{
	private final long bufferDuration;
	private final List<T> buffer = new ArrayList<>();
	
	
	/**
	 * @param bufferDuration the duration in [s]
	 */
	public TimestampBasedBuffer(final double bufferDuration)
	{
		this.bufferDuration = (long) (bufferDuration * 1e9);
	}
	
	
	private void reduceBuffer()
	{
		if (buffer.isEmpty())
		{
			return;
		}
		long latestTimestamp = buffer.get(buffer.size() - 1).getTimestamp();
		buffer.removeIf(d -> (latestTimestamp - d.getTimestamp()) > bufferDuration);
	}
	
	
	public void add(T data)
	{
		reduceBuffer();
		buffer.add(data);
	}
	
	
	public List<T> getData()
	{
		return Collections.unmodifiableList(buffer);
	}
	
	
	public int size()
	{
		return buffer.size();
	}
	
	
	public T get(int i)
	{
		return buffer.get(i);
	}
	
	
	public Optional<T> getOldest()
	{
		if (buffer.isEmpty())
		{
			return Optional.empty();
		}
		return Optional.of(buffer.get(0));
	}
	
	
	public Optional<T> getLatest()
	{
		if (buffer.isEmpty())
		{
			return Optional.empty();
		}
		return Optional.of(buffer.get(buffer.size()));
	}
}
