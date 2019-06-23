/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.util;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import com.sleepycat.persist.model.Persistent;
import com.sleepycat.persist.model.PersistentProxy;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent(proxyFor = EnumMap.class)
public class EnumMapProxy implements PersistentProxy<Map<?, ?>>
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
		if (map.isEmpty())
		{
			return map;
		}
		return new EnumMap(map);
	}
}
