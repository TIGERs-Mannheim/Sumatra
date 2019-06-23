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

import java.io.Serializable;


/**
 * This interface defines some kind of (synchronized) FIFO, with a small touch of a queue (take()...)
 * 
 * @author Gero
 * 
 */
public interface ISyncedFIFO<D> extends Serializable, IBuffer<D>, Iterable<D>
{
	/**
	 * @return Retrieves and removes the first object from the FIFO; waits if there's none present
	 * @throws InterruptedException If interrupted while waiting for data to retrieve
	 */
	public D take() throws InterruptedException;
	

	/**
	 * @return Retrieves and removes the first object from the FIFO that matches the criteria; waits if there's none
	 *         present
	 * @throws InterruptedException If interrupted while waiting for data to retrieve
	 */
	public D takeIfMatches(ICriteria<D> criteria) throws InterruptedException;
	
	
	/**
	 * @return The first element from the FIFO (without removing it). Waits if there's none.
	 */
	public D look() throws InterruptedException;
	
	
	/**
	 * @param criteria The {@link ICriteria} the element has to match with
	 * @return The first element from the FIFO. Waits if there's none, or it doesn't match criteria.
	 */
	public D lookIfMatches(ICriteria<D> criteria) throws InterruptedException;

	
	/**
	 * @param criteria The {@link ICriteria} the element has to match with
	 * @return The first element from the FIFO. If there's none, or it doesn't match the given {@link ICriteria},
	 *         <code>null</code> is returned.
	 */
	public D peekFirstIfMatches(ICriteria<D> criteria);
}
