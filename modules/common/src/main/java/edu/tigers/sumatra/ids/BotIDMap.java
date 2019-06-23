/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ids;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import com.sleepycat.persist.model.Persistent;


/**
 * Type safe way to map a BotID with something.
 * 
 * @author AndreR
 * @param <T>
 */
@Persistent
public class BotIDMap<T> implements IBotIDMap<T>
{
	private static final long serialVersionUID = -5736073179625081902L;
	
	private final SortedMap<BotID, T> map;
	
	
	/**
	 * Default
	 */
	public BotIDMap()
	{
		map = new TreeMap<>();
	}
	
	
	/**
	 * Creates BotIDMap from existing Java-Map, shallow copy
	 * 
	 * @param initialMap
	 */
	public BotIDMap(final Map<BotID, T> initialMap)
	{
		map = new TreeMap<>(initialMap);
	}
	
	
	/**
	 * @param iMap
	 */
	public BotIDMap(final IBotIDMap<T> iMap)
	{
		map = new TreeMap<>(iMap.getContentMap());
	}
	
	
	@Override
	public T get(final BotID id) throws NoObjectWithThisIDException
	{
		final T result = map.get(id);
		if (result == null)
		{
			throw new NoObjectWithThisIDException("This BotIDMap does not contain a bot with id '" + id + "'!!!");
		}
		return result;
	}
	
	
	@Override
	public T getWithNull(final BotID id)
	{
		return map.get(id);
	}
	
	
	@Override
	public Set<Entry<BotID, T>> entrySet()
	{
		return map.entrySet();
	}
	
	
	@Override
	public T put(final BotID key, final T value)
	{
		return map.put(key, value);
	}
	
	
	@Override
	public Collection<T> values()
	{
		return map.values();
	}
	
	
	@Override
	public void putAll(final IBotIDMap<? extends T> put)
	{
		for (final Entry<BotID, ? extends T> entry : put.entrySet())
		{
			map.put(entry.getKey(), entry.getValue());
		}
	}
	
	
	@Override
	public T remove(final BotID key)
	{
		return map.remove(key);
	}
	
	
	@Override
	public int size()
	{
		return map.size();
	}
	
	
	@Override
	public boolean containsValue(final T value)
	{
		return map.containsValue(value);
	}
	
	
	@Override
	public boolean isEmpty()
	{
		return map.isEmpty();
	}
	
	
	@Override
	public Set<BotID> keySet()
	{
		return map.keySet();
	}
	
	
	@Override
	public void clear()
	{
		map.clear();
	}
	
	
	@Override
	public boolean containsKey(final BotID key)
	{
		return map.containsKey(key);
	}
	
	
	@Override
	public Iterator<Entry<BotID, T>> iterator()
	{
		return map.entrySet().iterator();
	}
	
	
	@Override
	public Map<BotID, T> getContentMap()
	{
		return map;
	}
}
