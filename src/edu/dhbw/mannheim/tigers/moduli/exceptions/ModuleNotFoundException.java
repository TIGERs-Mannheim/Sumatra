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
 * Exception if the module-system isn't able to resolve the modules.
 */
public class ModuleNotFoundException extends Exception
{
	
	/**  */
	private static final long	serialVersionUID	= -3273863493959166184L;
	
	
	/**
	 * @param msg
	 */
	public ModuleNotFoundException(String msg)
	{
		super(msg);
	}
}
