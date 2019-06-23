/*
 * *********************************************************
 * Copyright (c) 2010 DLR Oberpfaffenhofen KN
 * Project: flightControl
 * Date: Mar 5, 2010
 * Authors:
 * Bernhard Perun <bernhard.perun@dlr.de>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.moduli.exceptions;

/**
 * Exception if the module-system isn't able to start the modules.
 */
public class InitModuleException extends Exception
{
	
	/**  */
	private static final long	serialVersionUID	= -712078133464068775L;
	
	
	/**
	 * @param msg
	 * @param cause
	 */
	public InitModuleException(String msg, Throwable cause)
	{
		super(msg, cause);
	}
}
