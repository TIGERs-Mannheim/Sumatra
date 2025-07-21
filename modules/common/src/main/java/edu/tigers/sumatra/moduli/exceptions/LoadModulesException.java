/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.moduli.exceptions;

/**
 * Exception if the module-system isn't able to resolve the modules.
 */
public class LoadModulesException extends Exception
{
	private static final long serialVersionUID = -5850251277618067045L;


	/**
	 * @param msg of the exception
	 * @param cause of the exception
	 */
	public LoadModulesException(final String msg, final Throwable cause)
	{
		super(msg, cause);
	}


	/**
	 * @param msg of the exception
	 */
	public LoadModulesException(final String msg)
	{
		super(msg);
	}
}
