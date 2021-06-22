/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.integration;

import lombok.extern.log4j.Log4j2;


@Log4j2
public abstract class SimTimeBlocker
{
	private final double maxDuration;
	private Long latestTime;
	private Long startTime;


	public SimTimeBlocker(final double maxDuration)
	{
		this.maxDuration = maxDuration;
	}


	protected void updateTime(long timestamp)
	{
		latestTime = timestamp;
		if (startTime == null)
		{
			startTime = latestTime;
		}
	}


	public abstract SimTimeBlocker await();


	protected boolean isTimeUp()
	{
		return getDuration() > maxDuration;
	}


	public double getDuration()
	{
		if (latestTime == null || startTime == null)
		{
			return 0;
		}
		return (latestTime - startTime) / 1e9;
	}
}
