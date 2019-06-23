/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 27, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.wp.data;

import com.github.g3force.s2vconverter.IString2ValueConverter;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class DynamicPositionConverter implements IString2ValueConverter
{
	
	@Override
	public boolean supportedClass(final Class<?> clazz)
	{
		return clazz.equals(DynamicPosition.class);
	}
	
	
	@Override
	public Object parseString(final Class<?> impl, final String value)
	{
		return DynamicPosition.valueOf(value);
	}
	
	
	@Override
	public String toString(final Class<?> impl, final Object value)
	{
		DynamicPosition pos = (DynamicPosition) value;
		return pos.getSaveableString();
	}
	
}
