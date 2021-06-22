/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.cam;

import java.util.Collection;
import java.util.Queue;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Synchronize between vision time and Sumatra internal time
 */
public class TimeSync
{
	private static final Logger log = LogManager.getLogger(TimeSync.class.getName());

	private static final int BUFFER_SIZE = 30;

	private long offset = 0;
	private final Queue<Long> offsetBuffer = new CircularFifoQueue<>(BUFFER_SIZE);
	private final Queue<Long> diffBuffer = new CircularFifoQueue<>(BUFFER_SIZE);


	/**
	 * @param timestamp
	 */
	public void update(final double timestamp)
	{
		long tNow = System.nanoTime();
		long localSentNs = convertVision2LocalTime(timestamp, offset);
		long diff = tNow - localSentNs;

		diffBuffer.add(diff);

		double avgDiff = Math.abs(average(diffBuffer));

		if ((avgDiff > 3e8) || !offsetBuffer.isEmpty())
		{
			offsetBuffer.add(calcOffset(tNow, timestamp));
			offset = (long) average(offsetBuffer);
			if (avgDiff < 100_000)
			{
				log.info("Synced with Vision clock. offset={}ns diff={}ns", offset, avgDiff);
				offsetBuffer.clear();
			}
		}
	}


	/**
	 * Convert from unix seconds to internal nano seconds
	 *
	 * @param timestamp
	 * @return
	 */
	public long sync(final double timestamp)
	{
		return convertVision2LocalTime(timestamp, offset);
	}


	/**
	 * Convert from internal nano seconds to unix seconds
	 *
	 * @param timestamp
	 * @return
	 */
	public double reverseSync(final long timestamp)
	{
		return (timestamp + offset) * 1e-9;
	}


	private long convertVision2LocalTime(final double visionS, final long offset)
	{
		return ((long) (visionS * 1e9)) - offset;
	}


	private long calcOffset(final long tNow, final double tVision)
	{
		return (long) (tVision * 1e9) - tNow;
	}


	private double average(final Collection<Long> deque)
	{
		int size = deque.size();
		double avg = 0;
		for (Long l : deque)
		{
			avg += (double) l / size;
		}
		return avg;
	}
}
