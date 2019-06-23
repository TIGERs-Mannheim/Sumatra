/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 28.07.2010
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.util.collection;

import java.util.Collection;
import java.util.LinkedList;


/**
 * Base class for buffers with a given size, who can insert data stable using {@link #insert(Object)}
 * 
 * @author Gero
 * @param <D>
 * 
 */
public abstract class ASortedBuffer<D> extends LinkedList<D> implements ISortedBuffer<D>
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long	serialVersionUID	= 2170718043154560764L;
	
	
	protected final Object		sync					= new Object();
	private final int				allowedSize;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param allowedSize
	 */
	public ASortedBuffer(int allowedSize)
	{
		super();
		
		this.allowedSize = allowedSize;
	}
	
	
	/**
	 * @param allowedSize
	 * @param old
	 */
	public ASortedBuffer(int allowedSize, Collection<? extends D> old)
	{
		super();
		
		this.allowedSize = allowedSize;
		
		synchronized (sync)
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
		synchronized (sync)
		{
			super.push(data);
			
			if (super.size() > allowedSize)
			{
				super.removeLast();
			}
			
			sync.notifyAll();
		}
	}
	
	
	@Override
	public D peek()
	{
		synchronized (sync)
		{
			return super.peek();
		}
	}
	
	
	@Override
	public D poll()
	{
		synchronized (sync)
		{
			return super.poll();
		}
	}
	
	
	@Override
	public D remove()
	{
		synchronized (sync)
		{
			return super.remove();
		}
	}
	
}
