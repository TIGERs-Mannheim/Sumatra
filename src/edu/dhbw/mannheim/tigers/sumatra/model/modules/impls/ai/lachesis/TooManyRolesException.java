/*
 * *********************************************************
 * Copyright (c) 2009 - 2012, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 22, 2012
 * Author(s): NicolaiO
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.lachesis;

/**
 * Indicates that there are more roles than tiger bots.
 * 
 * @author NicolaiO
 * 
 */
public class TooManyRolesException extends Exception
{
	
	/**  */
	private static final long	serialVersionUID	= 4630842222046895707L;
	
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 */
	public TooManyRolesException()
	{
		super();
	}
	
	
	/**
	 * 
	 * @param msg
	 */
	public TooManyRolesException(String msg)
	{
		super(msg);
	}
	
	
	/**
	 * 
	 * @param msg
	 * @param throwable
	 */
	public TooManyRolesException(String msg, Throwable throwable)
	{
		super(msg, throwable);
	}
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
