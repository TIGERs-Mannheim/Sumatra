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
 * Simple interface for objects that are able to decide whether an object matches certain criteria
 * 
 * @author Gero
 * @param <D>
 * 
 */
public interface ICriteria<D>
{
	/**
	 * 
	 */
	ICriteria<?>	TRUE	= new ICriteria<Object>()
								{
									@Override
									public boolean matches(Object object)
									{
										return true;
									}
								};
	
	
	/**
	 * @param object
	 * @return <code>True</code> if matches, <code>false</code> otherwise.
	 */
	boolean matches(D object);
}
