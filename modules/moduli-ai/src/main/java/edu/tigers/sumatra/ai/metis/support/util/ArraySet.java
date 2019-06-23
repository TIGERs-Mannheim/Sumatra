/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.support.util;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Iterator;


/**
 * An ArrayList implementation of Set. An ArraySet is good for small sets; it
 * has less overhead than a HashSet or a TreeSet.
 *
 * @author Paul Chew
 *         Created December 2007. For use with Voronoi/Delaunay applet.
 */
public class ArraySet<E> extends AbstractSet<E>
{
	
	private ArrayList<E> items; // Items of the set
	
	
	/**
	 * Create an empty set (default initial capacity is 3).
	 */
	public ArraySet()
	{
		this(3);
	}
	
	
	/**
	 * Create an empty set with the specified initial capacity.
	 * 
	 * @param initialCapacity the initial capacity
	 */
	public ArraySet(int initialCapacity)
	{
		items = new ArrayList<>(initialCapacity);
	}
	
	
	/**
	 * Get the item at the specified index.
	 * 
	 * @param index where the item is located in the ListSet
	 * @return the item at the specified index
	 * @throws IndexOutOfBoundsException if the index is out of bounds
	 */
	public E get(int index)
	{
		return items.get(index);
	}
	
	
	@Override
	@java.lang.SuppressWarnings("squid:S2250") //suppresses inefficiency of contains, because this set should only be used with a small number of entries (see class description)
	public boolean add(E item)
	{
		return !items.contains(item) && items.add(item);
	}
	
	
	@Override
	public Iterator<E> iterator()
	{
		return items.iterator();
	}
	
	
	@Override
	public int size()
	{
		return items.size();
	}
	
}
