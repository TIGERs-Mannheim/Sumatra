/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.util;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import com.sleepycat.persist.model.Persistent;
import com.sleepycat.persist.model.PersistentProxy;


/**
 * @author Mark Geiger <Mark.Geiger@dlr.de>
 */
@Persistent(proxyFor = LinkedHashSet.class)
public class LinkedHashSetProxy implements PersistentProxy<Set<?>>
{
	private Set<?> set;
	
	
	@Override
	public void initializeProxy(final Set<?> object)
	{
		set = new HashSet<>(object);
	}
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Set<?> convertProxy()
	{
		return new LinkedHashSet<>(set);
	}
}
