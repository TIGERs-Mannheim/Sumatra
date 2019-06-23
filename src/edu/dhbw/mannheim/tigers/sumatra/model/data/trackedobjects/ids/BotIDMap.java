/*
 * *********************************************************
 * Copyright (c) 2009 - 2012, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 24.01.2012
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;


/**
 * Type safe way to map a BotID with something.
 * 
 * @author AndreR
 * @param <T>
 * 
 */
@Embeddable
public class BotIDMap<T> implements IBotIDMap<T>
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long	serialVersionUID	= -5736073179625081902L;
	
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	private Map<BotID, T>		map;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public BotIDMap()
	{
		map = new HashMap<BotID, T>();
	}
	
	
	/**
	 * @param initialCapacity
	 */
	public BotIDMap(int initialCapacity)
	{
		map = new HashMap<BotID, T>(initialCapacity);
	}
	
	
	/**
	 * @param initialCapacity
	 * @param loadFactor
	 */
	public BotIDMap(int initialCapacity, float loadFactor)
	{
		map = new HashMap<BotID, T>(initialCapacity, loadFactor);
	}
	
	
	/**
	 * @param iMap
	 */
	public BotIDMap(IBotIDMap<T> iMap)
	{
		map = new HashMap<BotID, T>(iMap.size());
		for (final Entry<BotID, T> entry : iMap.entrySet())
		{
			map.put(entry.getKey(), entry.getValue());
		}
	}
	
	
	/**
	 * Creates a BotIDMap from a bot list
	 * 
	 * @param foeBots
	 * @param botList
	 * @return
	 */
	public static BotIDMap<TrackedBot> createBotIDMapFoes(final BotIDMapConst<TrackedBot> foeBots,
			final List<BotID> botList)
	{
		BotIDMap<TrackedBot> map = new BotIDMap<TrackedBot>();
		for (BotID botID : botList)
		{
			map.put(botID, foeBots.get(botID));
		}
		return map;
	}
	
	
	/**
	 * Creates a BotIDMap from a bot list
	 * 
	 * @param tigerBots
	 * @param botList
	 * @return
	 */
	public static BotIDMap<TrackedTigerBot> createBotIDMapTigers(final BotIDMapConst<TrackedTigerBot> tigerBots,
			final List<BotID> botList)
	{
		BotIDMap<TrackedTigerBot> map = new BotIDMap<TrackedTigerBot>();
		for (BotID botID : botList)
		{
			map.put(botID, tigerBots.get(botID));
		}
		return map;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public T get(BotID id)
	{
		final T result = map.get(id);
		if (result == null)
		{
			throw new NoObjectWithThisIDException("This BotIDMap does not contain a bot with id '" + id + "'!!!");
		}
		return result;
	}
	
	
	@Override
	public T getWithNull(BotID id)
	{
		return map.get(id);
	}
	
	
	@Override
	public Set<Entry<BotID, T>> entrySet()
	{
		return map.entrySet();
	}
	
	
	@Override
	public T put(BotID key, T value)
	{
		return map.put(key, value);
	}
	
	
	@Override
	public Collection<T> values()
	{
		return map.values();
	}
	
	
	@Override
	public void putAll(IBotIDMap<? extends T> put)
	{
		for (final Entry<BotID, ? extends T> entry : put.entrySet())
		{
			map.put(entry.getKey(), entry.getValue());
		}
	}
	
	
	@Override
	public T remove(BotID key)
	{
		return map.remove(key);
	}
	
	
	@Override
	public int size()
	{
		return map.size();
	}
	
	
	@Override
	public boolean containsValue(Object value)
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
	public boolean containsKey(BotID key)
	{
		return map.containsKey(key);
	}
	
	
	@Override
	public Iterator<Entry<BotID, T>> iterator()
	{
		return map.entrySet().iterator();
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public Map<BotID, T> getContentMap()
	{
		return map;
	}
}
