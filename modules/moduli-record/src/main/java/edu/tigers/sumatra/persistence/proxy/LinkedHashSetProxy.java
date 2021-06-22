/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.persistence.proxy;

import com.sleepycat.persist.model.Persistent;
import com.sleepycat.persist.model.PersistentProxy;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;


/**
 * Proxy for {@link LinkedHashSet}
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


	@Override
	public Set<?> convertProxy()
	{
		return new LinkedHashSet<>(set);
	}
}
