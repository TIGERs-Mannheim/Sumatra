package edu.tigers.sumatra.botmanager.serial;

import java.io.Serial;


/**
 * Serial parser exception class.
 *
 * @author AndreR
 */
public class SerialException extends Exception
{
	@Serial
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
