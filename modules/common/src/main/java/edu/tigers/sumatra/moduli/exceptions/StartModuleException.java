/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.moduli.exceptions;

/**
 * Exception if the module-system isn't able to start the modules.
 */
public class StartModuleException extends RuntimeException
{
	/**
	 * @param msg of the exception
	 * @param cause of the exception
	 */
	public StartModuleException(final String msg, final Throwable cause)
	{
		super(msg, cause);
	}
}
