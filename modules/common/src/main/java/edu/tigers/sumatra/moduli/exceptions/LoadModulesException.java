package edu.tigers.sumatra.moduli.exceptions;

import java.io.Serial;


/**
 * Exception if the module-system isn't able to resolve the modules.
 */
public class LoadModulesException extends Exception
{
	@Serial
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
