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
 * The base class for all exceptions that are raised in the pandora-package
 * 
 * @author Gero
 */
public class PandoraException extends RuntimeException
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long	serialVersionUID	= 213817383392849873L;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public PandoraException()
	{
		super();
	}
	
	
	/**
	 * @param msg
	 */
	public PandoraException(String msg)
	{
		super(msg);
	}
	
	
	/**
	 * @param msg
	 * @param throwable
	 */
	public PandoraException(String msg, Throwable throwable)
	{
		super(msg, throwable);
	}
}
