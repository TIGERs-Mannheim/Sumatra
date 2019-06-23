/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 01.04.2013
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.math.functions;

import org.apache.commons.lang.StringUtils;

import edu.dhbw.mannheim.tigers.sumatra.util.InstanceableClass.NotCreateableException;


/**
 * Creates 1D functions from strings.
 * 
 * @author AndreR
 */
public final class FunctionFactory
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	private FunctionFactory()
	{
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Create a function from a string.
	 * 
	 * @param text function string
	 * @return function
	 * @throws IllegalArgumentException
	 */
	public static IFunction1D createFunctionFromString(final String text)
	{
		String[] parts = text.split(":");
		
		if (parts.length != 2)
		{
			throw new IllegalArgumentException("No splitting ':' found in text");
		}
		
		final String floats[] = parts[1].split(";");
		final float params[] = new float[floats.length];
		for (int i = 0; i < floats.length; i++)
		{
			params[i] = Float.parseFloat(floats[i]);
		}
		
		for (EFunction ef : EFunction.values())
		{
			if (ef.getId().equals(parts[0]))
			{
				try
				{
					return (IFunction1D) ef.getInstanceableClass().newInstance(params);
				} catch (NotCreateableException err)
				{
					throw new IllegalArgumentException("Could not instantiate class: " + text, err);
				}
			}
		}
		
		throw new IllegalArgumentException("Unknown function");
	}
	
	
	/**
	 * Create a string from a function.
	 * 
	 * @param function
	 * @return
	 */
	public static String createStringFromFunction(final IFunction1D function)
	{
		if (function == null)
		{
			return "";
		}
		String text = function.getIdentifier().getId() + ":";
		
		text += StringUtils.join(function.getParameters(), ";");
		
		return text;
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
