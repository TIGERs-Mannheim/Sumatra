/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.observer;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Basic distributor that manages consumers.
 *
 * @param <T> the type of the consumer
 */
@Log4j2
public class BasicDistributor<T> implements BasicSubscriber<T>
{
	@Getter(AccessLevel.PACKAGE)
	private final Map<String, T> consumers = new ConcurrentHashMap<>();


	@Override
	public void subscribe(String id, T consumer)
	{
		var currentConsumer = consumers.putIfAbsent(id, consumer);
		if (currentConsumer != null)
		{
			throw new IllegalStateException("There is already a consumer for id " + id);
		}
	}


	@Override
	public void unsubscribe(String id)
	{
		consumers.remove(id);
	}


	public void clear()
	{
		if (!consumers.isEmpty())
		{
			throw new IllegalStateException("There are still consumers registered: " + consumers.keySet());
		}
	}
}
