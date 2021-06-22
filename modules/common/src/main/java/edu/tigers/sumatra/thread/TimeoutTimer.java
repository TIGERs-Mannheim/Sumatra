/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.thread;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


@RequiredArgsConstructor
@Log4j2
public class TimeoutTimer
{
	private final long timeout;
	private final String name;
	private final Runnable callback;

	private ScheduledExecutorService executorService;
	private ScheduledFuture<?> activeSchedule = null;


	public void start()
	{
		this.executorService = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("TimeoutTimer " + name));
	}


	public void stop()
	{
		executorService.shutdown();
	}


	public void startTimeout()
	{
		if (activeSchedule != null)
		{
			throw new IllegalStateException("There is already an active schedule");
		}
		activeSchedule = executorService.schedule(this::timeout, timeout, TimeUnit.MILLISECONDS);
	}


	public void cancelTimeout()
	{
		if (activeSchedule != null)
		{
			activeSchedule.cancel(false);
			activeSchedule = null;
		}
	}


	private void timeout()
	{
		activeSchedule = null;
		callback.run();
	}
}
