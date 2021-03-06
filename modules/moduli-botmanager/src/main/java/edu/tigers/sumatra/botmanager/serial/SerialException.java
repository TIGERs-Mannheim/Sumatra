/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 24.10.2013
 * Author(s): AndreR
 * *********************************************************
 */
package edu.tigers.sumatra.botmanager.serial;

/**
 * Serial parser exception class.
 * 
 * @author AndreR
 */
public class SerialException extends Exception
{
	private static final long serialVersionUID = 1460159527145410276L;
	
	
	/**
	 * @param message
	 */
	public SerialException(final String message)
	{
		super(message);
	}
	
	
	/**
	 * @param message
	 * @param cause
	 */
	public SerialException(final String message, final Throwable cause)
	{
		super(message, cause);
	}
}
