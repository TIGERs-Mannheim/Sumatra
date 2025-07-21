/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.observer;

import edu.tigers.sumatra.util.Safe;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;


/**
 * Distributes frames to all registered subscribers.
 *
 * @param <T> the type of the frame
 */
public class FrameDistributor<T> extends EventDistributor<T> implements FrameSubscriber<T>
{
	private final Map<String, Runnable> clearConsumers = new ConcurrentHashMap<>();

	private T lastFrame;


	@Override
	public void subscribe(String id, Consumer<T> consumer)
	{
		super.subscribe(id, consumer);
		var event = lastFrame;
		if (event != null)
		{
			Safe.run(consumer, event);
		}
	}


	@Override
	public void subscribeClear(String id, Runnable runnable)
	{
		var currentConsumer = clearConsumers.putIfAbsent(id, runnable);
		if (currentConsumer != null)
		{
			throw new IllegalStateException("There is already a consumer for id " + id);
		}
	}


	@Override
	public void unsubscribeClear(String id)
	{
		clearConsumers.remove(id);
	}


	/**
	 * Notify all subscribers about a new frame.
	 * This method is synchronized to ensure that the consumers are not called in parallel, which they might not expect.
	 *
	 * @param frame
	 */
	public void newFrame(T frame)
	{
		super.newEvent(frame);
		lastFrame = frame;
	}


	/**
	 * Notify all subscribers to clear the frame.
	 * This method is synchronized to ensure that the consumers are not called in parallel, which they might not expect.
	 */
	public void clearFrame()
	{
		lastFrame = null;
		synchronized (this)
		{
			clearConsumers.values().forEach(Safe::run);
		}
	}


	@Override
	public void clear()
	{
		clearFrame();
		super.clear();
	}
}
