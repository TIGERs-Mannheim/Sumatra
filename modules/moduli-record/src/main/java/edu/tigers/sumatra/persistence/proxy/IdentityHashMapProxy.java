/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.persistence.proxy;

import com.sleepycat.persist.model.Persistent;
import com.sleepycat.persist.model.PersistentProxy;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;


/**
 * Proxy for {@link IdentityHashMap}
 */
@Persistent(proxyFor = IdentityHashMap.class)
public class IdentityHashMapProxy implements PersistentProxy<Map<?, ?>>
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
		return new IdentityHashMap<>(map);
	}
}
