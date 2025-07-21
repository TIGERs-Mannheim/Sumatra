/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.observer;

import edu.tigers.sumatra.util.Safe;
import lombok.extern.log4j.Log4j2;

import java.util.function.Consumer;


/**
 * Event distributor that notifies all registered observers on new events.
 *
 * @param <T> the type of the event
 */
@Log4j2
public class EventDistributor<T> extends BasicDistributor<Consumer<T>> implements EventSubscriber<T>
{
	/**
	 * Notify all registered observers with the given event.
	 *
	 * @param event the event
	 */
	public void newEvent(T event)
	{
		synchronized (this)
		{
			getConsumers().values().forEach(consumer -> Safe.run(consumer, event));
		}
	}
}
