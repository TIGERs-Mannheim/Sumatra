/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package com.github.g3force.s2vconverter;

import edu.tigers.sumatra.math.vector.AVector;
import edu.tigers.sumatra.math.vector.AVector2;
import edu.tigers.sumatra.math.vector.IVector;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.IVectorN;
import edu.tigers.sumatra.math.vector.Vector3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class VectorConverter implements IString2ValueConverter
{
	private static final Logger log = LogManager.getLogger(VectorConverter.class.getName());

	private static final String MSG_NOT_PARSABLE = "Could not parse vector.";


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
			return AVector2.valueOf(value);
		} catch (NumberFormatException err)
		{
			log.warn(MSG_NOT_PARSABLE, err);
			return null;
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
			return null;
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
			return null;
		}
	}


	@Override
	public String toString(final Class<?> impl, final Object value)
	{
		IVector vec = (IVector) value;
		return vec.getSaveableString();
	}

}
