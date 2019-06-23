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
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;


/**
 * Unmodifiable version of BotIDMap.
 * 
 * @author AndreR
 * @param <T>
 * 
 */
@Embeddable
public final class BotIDMapConst<T> implements IBotIDMap<T>
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long	serialVersionUID	= -6737987790216062346L;
	
	/** not final for ObjectDB */
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	private Map<BotID, T>		map;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	private BotIDMapConst(Map<BotID, T> base)
	{
		this.map = base;
	}
	
	
	/**
	 * Creates a unmodifiable version of the given map <strong>using the <i>identical</i> content!</strong>
	 * 
	 * @param base
	 */
	private BotIDMapConst(IBotIDMap<T> base)
	{
		this(base.getContentMap());
	}
	
	
	/**
	 * Inspired by {@link Collections#unmodifiableMap(Map)}
	 * @param base
	 * @return
	 */
	public static <T> BotIDMapConst<T> unmodifiableBotIDMap(IBotIDMap<T> base)
	{
		return new BotIDMapConst<T>(base);
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
			throw new NoObjectWithThisIDException("This BotIDMapConst does not contain a bot with id '" + id + "'!!!");
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
		return Collections.unmodifiableSet(map.entrySet());
	}
	
	
	@Override
	public T put(BotID key, T value)
	{
		throw new UnsupportedOperationException("Cannot put data in a unmodifiable map");
	}
	
	
	@Override
	public void putAll(IBotIDMap<? extends T> put)
	{
		throw new UnsupportedOperationException("Cannot put data in a unmodifiable map");
	}
	
	
	@Override
	public Collection<T> values()
	{
		return Collections.unmodifiableCollection(map.values());
	}
	
	
	@Override
	public T remove(BotID key)
	{
		throw new UnsupportedOperationException("Cannot put data in a unmodifiable map");
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
		return Collections.unmodifiableSet(map.keySet());
	}
	
	
	@Override
	public void clear()
	{
		throw new UnsupportedOperationException("Cannot put data in a unmodifiable map");
	}
	
	
	@Override
	public boolean containsKey(BotID key)
	{
		return map.containsKey(key);
	}
	
	
	@Override
	public Iterator<Entry<BotID, T>> iterator()
	{
		return new ConstBotIDMapIterator<T>(entrySet());
	}
	
	
	private static final class ConstBotIDMapIterator<T> implements Iterator<Entry<BotID, T>>
	{
		private final Iterator<Entry<BotID, T>>	entryIterator;
		
		
		private ConstBotIDMapIterator(Collection<Entry<BotID, T>> entries)
		{
			this.entryIterator = entries.iterator();
		}
		
		
		@Override
		public boolean hasNext()
		{
			return entryIterator.hasNext();
		}
		
		
		@Override
		public Entry<BotID, T> next()
		{
			return entryIterator.next();
		}
		
		
		@Override
		public void remove()
		{
			throw new UnsupportedOperationException("BotIDMapConst doesn't support mutating operations!");
		}
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
