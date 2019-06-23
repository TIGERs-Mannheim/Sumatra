/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math;


/**
 * This exception is thrown if a mathematical/logical error occurs.
 * 
 * @author Gero
 */
public class MathException extends Exception
{
	private static final long serialVersionUID = -321058382938703716L;
	
	
	/**
	 * @param str
	 */
	public MathException(final String str)
	{
		super(str);
	}
	
	
	/**
	 * @param str
	 * @param err
	 */
	public MathException(final String str, Exception err)
	{
		super(str, err);
	}
}
