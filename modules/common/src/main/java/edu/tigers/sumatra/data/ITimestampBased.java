package edu.tigers.sumatra.data;


public interface ITimestampBased
{
	/**
	 * @return the timestamp based on {@link System#nanoTime()}
	 */
	long getTimestamp();
}
