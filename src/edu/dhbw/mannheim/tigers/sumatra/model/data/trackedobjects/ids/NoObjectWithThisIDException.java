/*
 * *********************************************************
 * Copyright (c) 2009 - 2012, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 24.05.2012
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids;

/**
 * Thrown by the {@link IBotIDMap} implementations whenever a {@link IBotIDMap#get(BotID)} is called and the map doesn't
 * contain a object with the given ID!
 * 
 * @author Gero
 * 
 */
public class NoObjectWithThisIDException extends RuntimeException
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long	serialVersionUID	= -6667693255367884125L;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public NoObjectWithThisIDException()
	{
		super();
	}
	
	
	/**
	 * @param msg
	 */
	public NoObjectWithThisIDException(String msg)
	{
		super(msg);
	}
	
	
	/**
	 * @param msg
	 * @param throwable
	 */
	public NoObjectWithThisIDException(String msg, Throwable throwable)
	{
		super(msg, throwable);
	}
}