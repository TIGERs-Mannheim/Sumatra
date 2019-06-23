/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 01.04.2013
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.math.functions;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;


/**
 * Creates 1D functions from strings.
 * 
 * @author AndreR
 * 
 */
public final class Function1DFactory
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	private Function1DFactory()
	{
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Add a available 1D functions to this list for the factory to know them.
	 * 
	 * @return
	 */
	private static List<IFunction1D> createFunctionList()
	{
		List<IFunction1D> functions = new ArrayList<IFunction1D>();
		functions.add(new Function1DConstant());
		functions.add(new Function1DLinear());
		functions.add(new Function1DPolyOrder2());
		
		return functions;
	}
	
	
	/**
	 * Create a function from a string.
	 * 
	 * @param text function string
	 * @return function
	 */
	public static IFunction1D createFunctionFromString(String text)
	{
		List<IFunction1D> functions = createFunctionList();
		
		String[] parts = text.split(":");
		
		if (parts.length != 2)
		{
			throw new IllegalArgumentException("No splitting ':' found in text");
		}
		
		final String floats[] = parts[1].split(",");
		List<Float> params = new ArrayList<Float>();
		for (String f : floats)
		{
			params.add(Float.parseFloat(f));
		}
		
		for (IFunction1D f : functions)
		{
			if (f.getIdentifier().equals(parts[0]))
			{
				f.setParameters(params);
				
				return f;
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
	public static String createStringFromFunction(IFunction1D function)
	{
		if (function == null)
		{
			return "";
		}
		String text = function.getIdentifier() + ":";
		
		text += StringUtils.join(function.getParameters(), ",");
		
		return text;
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
