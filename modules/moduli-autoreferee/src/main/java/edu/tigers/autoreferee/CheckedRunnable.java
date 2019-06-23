/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 6, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoreferee;

import java.util.Optional;


/**
 * @author "Lukas Magel"
 */
@FunctionalInterface
public interface CheckedRunnable
{
	
	/**
	 * Executes the supplied function and returns the caught exception if any occurred
	 * 
	 * @param f The function that is executed synchronously
	 * @return an Optional containing the caught exception or an empty optional if none occurred
	 */
	public static Optional<Exception> execAndCatchAll(final CheckedRunnable f)
	{
		return f.execAndCatchAll();
	}
	
	
	/**
	 * Executes the runnable and returns the caught exception if any occurred
	 * 
	 * @return an Optional containing the caught exception or an empty optional if none occurred
	 */
	public default Optional<Exception> execAndCatchAll()
	{
		try
		{
			run();
		} catch (Exception e)
		{
			return Optional.of(e);
		}
		return Optional.empty();
	}
	
	
	/**
	 * @throws Exception
	 */
	public void run() throws Exception;
}
