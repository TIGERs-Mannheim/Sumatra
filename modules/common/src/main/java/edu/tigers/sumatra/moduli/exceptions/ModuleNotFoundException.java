/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.moduli.exceptions;

/**
 * Exception if the module-system isn't able to find a module.
 */
public class ModuleNotFoundException extends RuntimeException
{
	private static final long serialVersionUID = -3273863493959166184L;


	/**
	 * @param msg of the exception
	 */
	public ModuleNotFoundException(final String msg)
	{
		super(msg);
	}
}
