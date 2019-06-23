/*
 * *********************************************************
 * Copyright (c) 2009 - 2012, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 24.01.2012
 * Author(s): AndreR
 * *********************************************************
 */
package edu.tigers.sumatra.ids;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


/**
 * Interface for BotIDMap and BotIDMapConst
 * 
 * @author AndreR
 * @param <T>
 */
public interface IBotIDMap<T> extends Serializable, Iterable<Entry<BotID, T>>
{
	/**
	 * @param id
	 * @return The object associated with the given id
	 * @throws NoObjectWithThisIDException Thrown if there is no object associated with
	 *            the given id!
	 */
	T get(BotID id);
	
	
	/**
	 * @param id
	 * @return A type-safe version of {@link java.util.Map#get(Object)}. <code>null</code> if there is no object
	 *         associated with
	 *         the given id!
	 */
	T getWithNull(BotID id);
	
	
	/**
	 * @return
	 */
	Set<Entry<BotID, T>> entrySet();
	
	
	/**
	 * @param key
	 * @param value
	 * @return
	 */
	T put(BotID key, T value);
	
	
	/**
	 * @return
	 */
	Collection<T> values();
	
	
	/**
	 * @param put
	 */
	void putAll(IBotIDMap<? extends T> put);
	
	
	/**
	 * @param key
	 * @return
	 */
	T remove(BotID key);
	
	
	/**
	 * @return
	 */
	int size();
	
	
	/**
	 * @param value
	 * @return
	 */
	boolean containsValue(Object value);
	
	
	/**
	 * @return
	 */
	boolean isEmpty();
	
	
	/**
	 * @return
	 */
	Set<BotID> keySet();
	
	
	/**
	 *
	 */
	void clear();
	
	
	/**
	 * @param key
	 * @return
	 */
	boolean containsKey(BotID key);
	
	
	/**
	 * Carefully with this! It is primary intended for internal purposes
	 * 
	 * @return The {@link Map} this {@link BotIDMap} is based on
	 */
	Map<BotID, T> getContentMap();
}
