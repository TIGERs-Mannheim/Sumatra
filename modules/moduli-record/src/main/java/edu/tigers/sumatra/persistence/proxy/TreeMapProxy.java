/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.persistence.proxy;

import com.sleepycat.persist.model.Persistent;
import com.sleepycat.persist.model.PersistentProxy;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;


/**
 * Proxy for {@link TreeMap}
 */
@Persistent(proxyFor = TreeMap.class)
public class TreeMapProxy implements PersistentProxy<Map<?, ?>>
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
		return new TreeMap<>(map);
	}
}
