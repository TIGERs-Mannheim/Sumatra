package edu.tigers.sumatra.moduli.exceptions;

import java.io.Serial;


/**
 * Exception if the module-system isn't able to find a module.
 */
public class ModuleNotFoundException extends RuntimeException
{
	@Serial
	private static final long serialVersionUID = -3273863493959166184L;


	/**
	 * @param msg of the exception
	 */
	public ModuleNotFoundException(final String msg)
	{
		super(msg);
	}
}
