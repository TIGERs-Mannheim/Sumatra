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


/**
 * Describes the minimal methods a Buffer must implement
 * 
 * @author Gero
 * 
 * @param <D>
 */
public interface IBuffer<D>
{
	
	/**
	 * @return Returns and removes the first element of the FIFO; <code>null</code> if none
	 */
	public abstract D poll();
	

	/**
	 * @return Returns but does NOT remove the first element of the FIFO. Null, if there is none.
	 */
	public abstract D peek();
	

	/**
	 * Inserts data at the front of the FIFO
	 * @param data
	 */
	public abstract void put(D data);
	
}