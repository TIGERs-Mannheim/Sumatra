/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.Collection;
import java.util.function.Consumer;


/**
 * Execute operations safe by catching and logging any {@link Exception}s.
 */
@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Safe
{
	/**
	 * Run the runnable, catching any exception and logging it out.
	 *
	 * @param runnable the runnable to run
	 */
	public static void run(Runnable runnable)
	{
		try
		{
			runnable.run();
		} catch (Throwable e)
		{
			log.error("Unexpected exception", e);
		}
	}


	/**
	 * Run the consumer, catching any exception and logging it out.
	 *
	 * @param consumer the consumer to run
	 * @param element  the element to consume
	 * @param <T>      the type of the element
	 */
	public static <T> void run(Consumer<T> consumer, T element)
	{
		try
		{
			consumer.accept(element);
		} catch (Throwable e)
		{
			log.error("Unexpected exception", e);
		}
	}


	/**
	 * Apply consumer to all elements in the collections, catching exception per consumer, without effecting
	 * the execution of the others.
	 *
	 * @param collection the elements to go through
	 * @param consumer   the consumer that the elements are applied to
	 * @param <T>        type of the elements
	 */
	public static <T> void forEach(Collection<T> collection, Consumer<T> consumer)
	{
		collection.forEach(element -> run(consumer, element));
	}
}
