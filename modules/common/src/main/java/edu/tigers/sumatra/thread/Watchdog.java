/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.thread;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * The watchdog monitors timeout events.
 * The watchdog sets a variable that needs to be reset during each watchdog
 * period. If this is not done, the watchdog will call its observers and
 * terminate.
 */
@Log4j2
@RequiredArgsConstructor
public class Watchdog
{
	private final long period;
	private final String name;
	private final Runnable callback;

	@Setter
	private boolean active = true;
	private long lastPing;
	private ScheduledExecutorService executorService;


	public void start()
	{
		lastPing = System.nanoTime();
		active = true;
		this.executorService = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("Watchdog " + name));
		this.executorService.scheduleWithFixedDelay(this::trigger, period, period, TimeUnit.MILLISECONDS);
	}


	public void stop()
	{
		if (executorService != null)
		{
			executorService.shutdown();
		}
	}


	/**
	 * reset the watchdog
	 */
	public void reset()
	{
		lastPing = System.nanoTime();
	}


	private void trigger()
	{
		if (!active)
		{
			return;
		}
		long now = System.nanoTime();
		long diff = now - lastPing;
		if (diff > TimeUnit.MILLISECONDS.toNanos(period))
		{
			log.debug("Timed out. Now: {}ns, last ping: {}ns, diff: {}ns",
					now, lastPing, diff);
			callback.run();
			reset();
		}
	}
}
