/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.persistence.proxy;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import com.sleepycat.persist.model.Persistent;
import com.sleepycat.persist.model.PersistentProxy;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
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
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Map<?, ?> convertProxy()
	{
		return new TreeMap<>(map);
	}
}
