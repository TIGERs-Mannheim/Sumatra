/*
 * *********************************************************
 * Copyright (c) 2009 - 2012, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 23.05.2012
 * Author(s): osteinbrecher
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.playpattern.exceptions;

/**
 * Exception, thrown by pattern serializer
 * 
 * @author osteinbrecher
 * 
 */
public class PatternSerializerException extends RuntimeException
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final long	serialVersionUID	= 62076190194677539L;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param msg
	 * @param throwable
	 */
	public PatternSerializerException(String msg, Throwable throwable)
	{
		super(msg, throwable);
	}
	
	
	/**
	 * @param throwable
	 */
	public PatternSerializerException(Throwable throwable)
	{
		super(throwable.toString(), throwable);
	}
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
