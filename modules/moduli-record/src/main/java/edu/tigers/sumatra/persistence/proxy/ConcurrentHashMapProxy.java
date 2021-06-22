/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.persistence.proxy;

import com.sleepycat.persist.model.Persistent;
import com.sleepycat.persist.model.PersistentProxy;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Proxy for {@link ConcurrentHashMap}
 */
@Persistent(proxyFor = ConcurrentHashMap.class)
public class ConcurrentHashMapProxy implements PersistentProxy<Map<?, ?>>
{
	private Map<?, ?> map;


	@Override
	public void initializeProxy(final Map<?, ?> object)
	{
		map = new HashMap<>(object);
	}


	@Override
	public Map<?, ?> convertProxy()
	{
		return new ConcurrentHashMap<>(map);
	}
}
