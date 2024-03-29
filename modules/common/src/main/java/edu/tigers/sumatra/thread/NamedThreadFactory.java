/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 24, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.thread;

import lombok.extern.log4j.Log4j2;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ThreadFactory;


/**
 * Simple ThreadFactory that gives each new thread a name combined with a
 * number starting with 0
 */
@Log4j2
public class NamedThreadFactory implements ThreadFactory
{
	private final String name;
	private final UncaughtExceptionHandler handler;
	private final int priority;

	private int counter = 0;


	/**
	 * @param name     name of the thread
	 * @param handler  this handler will be called on any uncaught exception, but note that this does not work for
	 *                 execution services!
	 * @param priority Priority of the created thread.
	 */
	public NamedThreadFactory(final String name, final UncaughtExceptionHandler handler, final int priority)
	{
		this.name = name;
		this.handler = handler;
		this.priority = priority;
	}


	/**
	 * @param name    name of the thread
	 * @param handler this handler will be called on any uncaught exception, but note that this does not work for
	 *                execution services!
	 */
	public NamedThreadFactory(final String name, final UncaughtExceptionHandler handler)
	{
		this(name, handler, Thread.NORM_PRIORITY);
	}


	/**
	 * @param name     name of the thread
	 * @param priority Priority of the created thread.
	 */
	public NamedThreadFactory(final String name, final int priority)
	{
		this(name, null, priority);
	}


	/**
	 * @param name name of the thread
	 */
	public NamedThreadFactory(final String name)
	{
		this(name, null);
	}


	@SuppressWarnings("java:S1181") // catching throwable intentionally for logging/debugging purposes
	private void interceptUncaughtException(Runnable runnable)
	{
		try
		{
			runnable.run();
		} catch (Throwable e)
		{
			log.error("Uncaught exception", e);
		}
	}


	@Override
	public Thread newThread(final Runnable r)
	{
		String postFix = "";
		if (counter > 0)
		{
			postFix = "-" + counter;
		}
		Thread thread = new Thread(() -> interceptUncaughtException(r), name + postFix);
		thread.setPriority(priority);
		if (handler != null)
		{
			thread.setUncaughtExceptionHandler(handler);
		}
		counter++;
		return thread;
	}
}
