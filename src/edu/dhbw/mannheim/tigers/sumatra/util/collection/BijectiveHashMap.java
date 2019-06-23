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
 * @author MalteM
 * TODO: This Bijective Hash Map is only usable for objects from the same class.
 * 		We could maybe expand it for Objects from the same class and all inherating classes?
 */
public class BijectiveHashMap<K, V>
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private HashMap<K, V>	keyValueMap;
	private HashMap<V, K>	valueKeyMap;
	
	protected final Logger	log	= Logger.getLogger(getClass());
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
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
	 * 
	 * @param key
	 * @param value
	 */
	public void put(K key, V value)
	{
		if (keyValueMap.containsKey(key) || keyValueMap.containsValue(value))
		{
			log.warn("Can't put key: '" + key.toString() + "' and value: '" + value.toString() + "'"
					+ "in Bijective HashMap. Already there.");
		} else
		{
			keyValueMap.put(key, value);
			valueKeyMap.put(value, key);
		}	
	}
	
	/**
	 * Put given entry in the map.
	 * 
	 * @param Entry
	 */
	public void put(Entry<K, V> entry)
	{
		this.put(entry.getKey(), entry.getValue());
	}
	
	
	/**
	 * Returns the <b>Key</b> to a given <b>Value</b>.
	 * @param value
	 * @return key
	 */
	public K getKey(V v)
	{
		return valueKeyMap.get(v);
	}
	
	
	/**
	 * Returns the <b>Value</b> to a given <b>Key</b>.
	 * @param key
	 * @return value
	 */
	public V getValue(K k)
	{
		return keyValueMap.get(k);
	}
	
	
	public void removeValue(V v)
	{
		K k = valueKeyMap.get(v);
		valueKeyMap.remove(v);
		keyValueMap.remove(k);
	}
	
	
	public void removeKey(K k)
	{
		V v = keyValueMap.get(k);
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
	 */
	public Collection<V> values()
	{
		return keyValueMap.values();
	}
	
	/**
	 * Returns a Set of all Keys.
	 */
	public Set<K> keySet()
	{
		return keyValueMap.keySet();
	}
	
	/**
	 * Puts the given map in the BijectiveHashMap.
	 */
	public void putAll(Map <K,V> map)
	{
		Iterator<Entry<K,V>> it = map.entrySet().iterator();
		while(it.hasNext())
		{
			this.put(it.next());
		}		
	}
}
