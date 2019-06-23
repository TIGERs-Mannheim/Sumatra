/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 01.08.2010
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.util.collection;


/**
 * A fast, but not as flexible as {@link SyncedLinkedFIFO} implementation of the {@link ISyncedFIFO}-interface, based on
 * {@link ArrayRingBuffer}, so it inherits all of its drawbacks.
 * 
 * @author Gero
 * @param <D>
 * 
 */
public class SyncedArrayFIFO<D> extends ArrayRingBuffer<D> implements ISyncedFIFO<D>
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long	serialVersionUID	= -6089410974928677935L;
	/** timeout for waiting for new entries */
	private static final int	TIMEOUT				= 10000;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param size
	 */
	public SyncedArrayFIFO(int size)
	{
		super(size);
	}
	
	
	// --------------------------------------------------------------------------
	// --- mutators -------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void put(D data)
	{
		addFirst(data);
	}
	
	
	@Override
	public D take() throws InterruptedException
	{
		synchronized (sync)
		{
			D result = pollFirst();
			while (result == null)
			{
				sync.wait(TIMEOUT);
				result = pollFirst();
			}
			
			return result;
		}
	}
	
	
	@Override
	public D takeIfMatches(ICriteria<D> criteria) throws InterruptedException
	{
		throw new UnsupportedOperationException("takeIfMatches is not supported due to implementation details!");
	}
	
	
	@Override
	public D peekFirstIfMatches(ICriteria<D> criteria)
	{
		synchronized (sync)
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
		synchronized (sync)
		{
			D result = peekFirst();
			while (result == null)
			{
				sync.wait(TIMEOUT);
				result = peekFirst();
			}
			return result;
		}
	}
	
	
	@Override
	public D lookIfMatches(ICriteria<D> criteria) throws InterruptedException
	{
		synchronized (sync)
		{
			D result = peekFirst();
			while ((result == null) || !criteria.matches(result))
			{
				sync.wait();
				result = peekFirst();
			}
			return result;
		}
	}
}
