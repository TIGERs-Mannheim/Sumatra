/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 27, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.functions;

import org.apache.log4j.Logger;

import com.github.g3force.s2vconverter.IString2ValueConverter;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class FunctionConverter implements IString2ValueConverter
{
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(FunctionConverter.class.getName());
	
	
	@Override
	public boolean supportedClass(final Class<?> impl)
	{
		return impl.equals(IFunction1D.class);
	}
	
	
	@Override
	public Object parseString(final Class<?> impl, final String value)
	{
		try
		{
			return FunctionFactory.createFunctionFromString(value);
		} catch (IllegalArgumentException err)
		{
			log.warn("The function " + value
					+ " could not be parsed correctly and was replaced by a constant 0 function.", err);
			return Function1dPoly.constant(0);
		}
	}
	
	
	@Override
	public String toString(final Class<?> impl, final Object value)
	{
		return FunctionFactory.createStringFromFunction((IFunction1D) value);
	}
}
