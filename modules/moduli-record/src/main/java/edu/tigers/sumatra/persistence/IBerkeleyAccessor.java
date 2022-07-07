/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.persistence;

import com.sleepycat.persist.EntityStore;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;


/**
 * Accessor interface to retrieve elements from berkeley db
 *
 * @param <T> the element type of this storage
 */
public interface IBerkeleyAccessor<T>
{
	/**
	 * Open the accessor
	 *
	 * @param entityStore
	 */
	void open(EntityStore entityStore);


	/**
	 * @return the number of elements stored
	 */
	long size();


	/**
	 * Load all stored elements. Use this carefully, as there might not be sufficient memory!
	 *
	 * @return all stored elements
	 */
	List<T> load();


	/**
	 * @return action to apply on each entity
	 */
	void forEach(Consumer<T> consumer);


	/**
	 * Write elements to database
	 *
	 * @param elements
	 */
	void write(final Collection<T> elements);


	/**
	 * Write a single element to database
	 *
	 * @param element
	 */
	void write(T element);


	/**
	 * @param key
	 * @return the element nearest to given key
	 */
	T get(long key);

	/**
	 * @return the very first (smallest) key in this storage
	 */
	Long getFirstKey();


	/**
	 * @return the very last (largest) key in this storage
	 */
	Long getLastKey();


	/**
	 * @param key some key
	 * @return the nearest key to the given one
	 */
	Long getNearestKey(long key);


	/**
	 * @return the key after given key
	 */
	Long getNextKey(long key);


	/**
	 * @return the key before given key
	 */
	Long getPreviousKey(long key);


	/**
	 * @return true, if the key is a Sumatra timestamp
	 */
	boolean isSumatraTimestampBased();
}
