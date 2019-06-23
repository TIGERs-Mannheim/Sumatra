/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 27, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.math;

import org.apache.log4j.Logger;

import com.github.g3force.s2vconverter.IString2ValueConverter;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class VectorConverter implements IString2ValueConverter
{
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(VectorConverter.class.getName());
	
	
	@Override
	public boolean supportedClass(final Class<?> impl)
	{
		return impl.equals(IVector.class)
				|| impl.equals(IVector2.class)
				|| impl.equals(IVector3.class)
				|| impl.equals(IVectorN.class);
	}
	
	
	@Override
	public Object parseString(final Class<?> impl, final String value)
	{
		if (impl.equals(IVector2.class))
		{
			try
			{
				return Vector2.valueOf(value);
			} catch (NumberFormatException err)
			{
				log.error("Could not parse vector.", err);
				return AVector2.ZERO_VECTOR;
			}
		} else if (impl.equals(IVector3.class))
		{
			try
			{
				return Vector3.valueOf(value);
			} catch (NumberFormatException err)
			{
				log.error("Could not parse vector.", err);
				return AVector3.ZERO_VECTOR;
			}
		} else if (impl.equals(IVectorN.class) || impl.equals(IVector.class))
		{
			try
			{
				return AVector.valueOf(value);
			} catch (NumberFormatException err)
			{
				log.error("Could not parse vector.", err);
				return new VectorN(0);
			}
		}
		return null;
	}
	
	
	@Override
	public String toString(final Class<?> impl, final Object value)
	{
		IVector vec = (IVector) value;
		return vec.getSaveableString();
	}
	
}
