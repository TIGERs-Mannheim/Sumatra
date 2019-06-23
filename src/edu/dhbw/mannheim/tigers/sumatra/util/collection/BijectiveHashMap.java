/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 16.01.2011
 * Author(s): Malte
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.util.collection;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;


/**
 * Bijective HashMap. Every Key has a distince Value AND every value has a distinct Key!
 * 
 * @author MalteM
 * @param <K>
 * @param <V>
 * 
 */
public class BijectiveHashMap<K, V>
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger	log	= Logger.getLogger(BijectiveHashMap.class.getName());
	
	private final HashMap<K, V>	keyValueMap;
	private final HashMap<V, K>	valueKeyMap;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public BijectiveHashMap()
	{
		keyValueMap = new HashMap<K, V>();
		valueKeyMap = new HashMap<V, K>();
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Put key and value in the map.
	 * put
	 * @param i
	 * @param value
	 */
	public void put(K i, V value)
	{
		if (keyValueMap.containsKey(i) || keyValueMap.containsValue(value))
		{
			log.warn("Can't put key: '" + i.toString() + "' and value: '" + value.toString() + "'"
					+ "in Bijective HashMap. Already there.");
		} else
		{
			keyValueMap.put(i, value);
			valueKeyMap.put(value, i);
		}
	}
	
	
	/**
	 * Put given entry in the map.
	 * put
	 * @param entry
	 */
	public void put(Entry<K, V> entry)
	{
		this.put(entry.getKey(), entry.getValue());
	}
	
	
	/**
	 * Returns the <b>Key</b> to a given <b>Value</b>.
	 * @param v
	 * @return key
	 */
	public K getKey(V v)
	{
		return valueKeyMap.get(v);
	}
	
	
	/**
	 * Returns the <b>Value</b> to a given <b>Key</b>.
	 * @param k
	 * @return value
	 */
	public V getValue(K k)
	{
		return keyValueMap.get(k);
	}
	
	
	/**
	 * @param v
	 */
	public void removeValue(V v)
	{
		final K k = valueKeyMap.get(v);
		valueKeyMap.remove(v);
		keyValueMap.remove(k);
	}
	
	
	/**
	 * @param k
	 */
	public void removeKey(K k)
	{
		final V v = keyValueMap.get(k);
		keyValueMap.remove(k);
		valueKeyMap.remove(v);
		
	}
	
	
	/**
	 * Clears the whole map.
	 * 
	 */
	public void clear()
	{
		keyValueMap.clear();
		valueKeyMap.clear();
	}
	
	
	/**
	 * Returns a Collection of all values.
	 * @return
	 */
	public Collection<V> values()
	{
		return keyValueMap.values();
	}
	
	
	/**
	 * Returns a Set of all Keys.
	 * @return
	 */
	public Set<K> keySet()
	{
		return keyValueMap.keySet();
	}
	
	
	/**
	 * Puts the given map in the BijectiveHashMap.
	 * @param map
	 */
	public void putAll(Map<K, V> map)
	{
		final Iterator<Entry<K, V>> it = map.entrySet().iterator();
		while (it.hasNext())
		{
			this.put(it.next());
		}
	}
}
