/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.vector;

import org.apache.log4j.Logger;

import com.github.g3force.s2vconverter.IString2ValueConverter;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class VectorConverter implements IString2ValueConverter
{
	@SuppressWarnings("unused")
	private static final Logger	log					= Logger.getLogger(VectorConverter.class.getName());
	
	private static final String	MSG_NOT_PARSABLE	= "Could not parse vector.";
	
	
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
			return parseVector2(value);
		} else if (impl.equals(IVector3.class))
		{
			return parseVector3(value);
		} else if (impl.equals(IVectorN.class) || impl.equals(IVector.class))
		{
			return parseVectorN(value);
		}
		return null;
	}
	
	
	private Object parseVector2(final String value)
	{
		try
		{
			return Vector2.valueOf(value);
		} catch (NumberFormatException err)
		{
			log.warn(MSG_NOT_PARSABLE, err);
			return Vector2f.ZERO_VECTOR;
		}
	}
	
	
	private Object parseVector3(final String value)
	{
		try
		{
			return Vector3.valueOf(value);
		} catch (NumberFormatException err)
		{
			log.warn(MSG_NOT_PARSABLE, err);
			return Vector3f.ZERO_VECTOR;
		}
	}
	
	
	private Object parseVectorN(final String value)
	{
		try
		{
			return AVector.valueOf(value);
		} catch (NumberFormatException err)
		{
			log.warn(MSG_NOT_PARSABLE, err);
			return VectorN.zero(0);
		}
	}
	
	
	@Override
	public String toString(final Class<?> impl, final Object value)
	{
		IVector vec = (IVector) value;
		return vec.getSaveableString();
	}
	
}
