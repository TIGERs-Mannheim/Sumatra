/*
 * *********************************************************
 * Copyright (c) 2009 - 2012, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 29.05.2012
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.exceptions;



/**
 * This exception is thrown if a {@link edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole}
 * which should already be assigned to a robot (by calling its
 * {@link edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole#assignBotID(edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID)}
 * -method has not been
 * assigned yet!
 * 
 * @author Gero
 */
public class RoleHasNotBeenAssignedException extends PandoraException
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long	serialVersionUID	= -2187312652207029480L;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public RoleHasNotBeenAssignedException()
	{
		super();
	}
	
	
	/**
	 * @param msg
	 */
	public RoleHasNotBeenAssignedException(String msg)
	{
		super(msg);
	}
	
	
	/**
	 * @param msg
	 * @param throwable
	 */
	public RoleHasNotBeenAssignedException(String msg, Throwable throwable)
	{
		super(msg, throwable);
	}
}
