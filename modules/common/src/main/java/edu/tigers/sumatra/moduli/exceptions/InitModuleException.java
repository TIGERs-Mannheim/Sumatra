/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.moduli.exceptions;

/**
 * Exception if the module-system isn't able to initialize the modules.
 */
public class InitModuleException extends RuntimeException
{
	/**
	 * @param msg of the exception
	 * @param cause of the exception
	 */
	public InitModuleException(final String msg, final Throwable cause)
	{
		super(msg, cause);
	}
}
