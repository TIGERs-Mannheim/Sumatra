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
public class DependencyException extends Exception
{
	
	/**  */
	private static final long	serialVersionUID	= -688270423190284593L;
	
	
	/**
	 * @param msg
	 */
	public DependencyException(String msg)
	{
		super(msg);
	}
}
