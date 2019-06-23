/*
 * *********************************************************
 * Copyright (c) 2009 - 2012, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 24.05.2012
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.exceptions;



/**
 * Thrown whenever a method of an {@link edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole}
 * -instance is called which needs its
 * {@link edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole#getBotID()} while this
 * has not been set yet.
 * 
 * @author Gero
 * 
 */
public class RoleUnitializedBotIDException extends PandoraException
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long	serialVersionUID	= 3502455136037290580L;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public RoleUnitializedBotIDException()
	{
		super();
	}
	
	
	/**
	 * @param msg
	 */
	public RoleUnitializedBotIDException(String msg)
	{
		super(msg);
	}
	
	
	/**
	 * @param msg
	 * @param throwable
	 */
	public RoleUnitializedBotIDException(String msg, Throwable throwable)
	{
		super(msg, throwable);
	}
}
