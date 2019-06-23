/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 22.07.2010
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.util.collection;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;


/**
 * An implementation of the {@link ISyncedFIFO}-interface, with a defined size, synchronized accesses, and based on a
 * {@link LinkedList}
 * 
 * @author Gero
 * @param <D>
 * 
 */
public class SyncedLinkedFIFO<D> extends LinkedList<D> implements ISyncedFIFO<D>
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long	serialVersionUID	= -4349746168495530189L;
	/** timeout for waiting for new entries */
	private static final int	TIMEOUT				= 10000;
	
	private final Object			lock					= new Object();
	private final int				allowedSize;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param allowedSize
	 */
	public SyncedLinkedFIFO(int allowedSize)
	{
		super();
		
		this.allowedSize = allowedSize;
	}
	
	
	/**
	 * @param allowedSize
	 * @param old
	 */
	public SyncedLinkedFIFO(int allowedSize, Collection<? extends D> old)
	{
		super();
		
		this.allowedSize = allowedSize;
		
		synchronized (lock)
		{
			int i = 0;
			for (final D d : old)
			{
				if (i > allowedSize)
				{
					break;
				}
				
				add(d);
				
				i++;
			}
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	protected int allowedSize()
	{
		return allowedSize;
	}
	
	
	@Override
	public void put(D data)
	{
		synchronized (lock)
		{
			super.push(data);
			
			if (super.size() > allowedSize)
			{
				super.removeLast();
			}
			
			lock.notifyAll();
		}
	}
	
	
	@Override
	public D peek()
	{
		synchronized (lock)
		{
			return super.peek();
		}
	}
	
	
	@Override
	public D poll()
	{
		synchronized (lock)
		{
			return super.poll();
		}
	}
	
	
	@Override
	public D remove()
	{
		synchronized (lock)
		{
			return super.remove();
		}
	}
	
	
	@Override
	public D take() throws InterruptedException
	{
		synchronized (lock)
		{
			D result = poll();
			while (result == null)
			{
				lock.wait(TIMEOUT);
				result = poll();
			}
			
			return result;
		}
	}
	
	
	@Override
	public D takeIfMatches(ICriteria<D> criteria) throws InterruptedException
	{
		synchronized (lock)
		{
			D result = peekFirst();
			while ((result == null) || !criteria.matches(result))
			{
				final Iterator<D> it = iterator();
				while (it.hasNext())
				{
					final D item = it.next();
					if (criteria.matches(item))
					{
						it.remove();
						// Jump and away...
						return item;
					}
				}
				lock.wait(TIMEOUT);
				result = peekFirst();
			}
			// At this point, the first element got chosen: Remove it!
			removeFirst();
			return result;
		}
	}
	
	
	@Override
	public D peekFirstIfMatches(ICriteria<D> criteria)
	{
		synchronized (lock)
		{
			D result = peekFirst();
			if ((result != null) && !criteria.matches(result))
			{
				result = null;
			}
			return result;
		}
	}
	
	
	@Override
	public D look() throws InterruptedException
	{
		synchronized (lock)
		{
			D result = peekFirst();
			while (result == null)
			{
				lock.wait();
				result = peekFirst();
			}
			return result;
		}
	}
	
	
	@Override
	public D lookIfMatches(ICriteria<D> criteria) throws InterruptedException
	{
		synchronized (lock)
		{
			D result = peekFirst();
			while ((result == null) || !criteria.matches(result))
			{
				lock.wait();
				result = peekFirst();
			}
			return result;
		}
	}
}
