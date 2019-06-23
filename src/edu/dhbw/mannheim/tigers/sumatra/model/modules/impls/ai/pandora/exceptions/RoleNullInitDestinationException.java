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
 * This exception is raised whenever
 * {@link edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole#initDestination(edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2)}
 * is called
 * with a <code>null</code> as parameter
 * 
 * @author Gero
 * 
 */
public class RoleNullInitDestinationException extends PandoraException
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long	serialVersionUID	= 5825655420269149943L;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public RoleNullInitDestinationException()
	{
		super();
	}
	
	
	/**
	 * @param msg
	 */
	public RoleNullInitDestinationException(String msg)
	{
		super(msg);
	}
	
	
	/**
	 * @param msg
	 * @param throwable
	 */
	public RoleNullInitDestinationException(String msg, Throwable throwable)
	{
		super(msg, throwable);
	}
}
