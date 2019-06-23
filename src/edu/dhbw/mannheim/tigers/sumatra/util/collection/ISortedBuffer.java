/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 27.07.2010
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.util.collection;

import java.io.Serializable;
import java.util.Deque;
import java.util.List;


/**
 * This interface declares a type that behaves like a simple buffer, but with one addition: {@link #insert(Object)}
 * 
 * @see ISyncedFIFO
 * @author Gero
 * @param <D>
 * 
 */
public interface ISortedBuffer<D> extends List<D>, Deque<D>, IBuffer<D>, Serializable
{
	/**
	 * Tries to inserts the given data into the list, but strictly regarding the order.
	 * If the given element won't fit into the list, because it's "less" then the last object, and the list is full,
	 * <code>false</code> is returned (true otherwise ;-)).
	 * 
	 * @param data
	 * @return Whether the given data fits into the list and has been added
	 */
	boolean insert(D data);
}
