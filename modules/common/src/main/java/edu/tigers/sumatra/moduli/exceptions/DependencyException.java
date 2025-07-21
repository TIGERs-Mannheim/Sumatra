/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.moduli.exceptions;

/**
 * Exception if the module-system isn't able to resolve the modules.
 */
public class DependencyException extends Exception
{
	private static final long serialVersionUID = -688270423190284593L;


	/**
	 * @param msg of the exception
	 */
	public DependencyException(final String msg)
	{
		super(msg);
	}


	public DependencyException(final String message, final Throwable cause)
	{
		super(message, cause);
	}
}
