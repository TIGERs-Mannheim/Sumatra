/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 17, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.util;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.DynamicPosition;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.functions.Function1dPoly;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.functions.FunctionFactory;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.functions.IFunction1D;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector3;


/**
 * Convert values of certain important classes from string to value and vice versa.
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public final class String2ValueConverter
{
	private static final Logger	log	= Logger.getLogger(String2ValueConverter.class.getName());
	
	
	private String2ValueConverter()
	{
	}
	
	
	/**
	 * Parse given String-value according to implementation of this parameter
	 * 
	 * @param impl
	 * @param value
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Object parseString(final Class<?> impl, final String value)
	{
		if (impl.equals(Integer.class) || impl.equals(Integer.TYPE))
		{
			try
			{
				return Integer.valueOf(value);
			} catch (NumberFormatException err)
			{
				return 0;
			}
		} else if (impl.equals(Long.class) || impl.equals(Long.TYPE))
		{
			try
			{
				return Long.valueOf(value);
			} catch (NumberFormatException err)
			{
				return 0L;
			}
		} else if (impl.equals(Float.class) || impl.equals(Float.TYPE))
		{
			try
			{
				return Float.valueOf(value);
			} catch (NumberFormatException err)
			{
				return 0.0f;
			}
		} else if (impl.equals(Double.class) || impl.equals(Double.TYPE))
		{
			try
			{
				return Double.valueOf(value);
			} catch (NumberFormatException err)
			{
				return 0.0;
			}
		} else if (impl.equals(IVector2.class))
		{
			try
			{
				return Vector2.valueOf(value);
			} catch (NumberFormatException err)
			{
				return AVector2.ZERO_VECTOR;
			}
		} else if (impl.equals(IVector3.class))
		{
			try
			{
				return Vector3.valueOf(value);
			} catch (NumberFormatException err)
			{
				return AVector3.ZERO_VECTOR;
			}
		} else if (impl.equals(Boolean.class) || impl.equals(Boolean.TYPE))
		{
			return Boolean.valueOf(value);
		} else if (impl.isEnum())
		{
			return Enum.valueOf((Class<Enum>) impl, value);
		} else if (impl.equals(DynamicPosition.class))
		{
			return DynamicPosition.valueOf(value);
		} else if (impl.equals(IFunction1D.class))
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
		} else if (impl.equals(String.class))
		{
			return value;
		}
		
		else
		{
			throw new IllegalStateException("Unknown implementation: " + impl + ". Can not parse string.");
		}
	}
	
	
	/**
	 * @param impl
	 * @param value
	 * @return
	 */
	public static String toString(final Class<?> impl, final Object value)
	{
		if (impl.equals(Integer.class) || impl.equals(Integer.TYPE))
		{
			return Integer.toString((Integer) value);
		} else if (impl.equals(Long.class) || impl.equals(Long.TYPE))
		{
			return Long.toString((Long) value);
		} else if (impl.equals(Float.class) || impl.equals(Float.TYPE))
		{
			return Float.toString((Float) value);
		} else if (impl.equals(Double.class) || impl.equals(Double.TYPE))
		{
			return Double.toString((Double) value);
		} else if (impl.equals(IVector2.class))
		{
			IVector2 vec = (IVector2) value;
			return String.format("%f,%f", vec.x(), vec.y());
		} else if (impl.equals(IVector3.class))
		{
			IVector3 vec = (IVector3) value;
			return String.format("%f,%f,%f", vec.x(), vec.y(), vec.z());
		} else if (impl.equals(Boolean.class) || impl.equals(Boolean.TYPE))
		{
			return Boolean.toString((Boolean) value);
		} else if (impl.isEnum())
		{
			return ((Enum<?>) value).name();
		} else if (impl.equals(DynamicPosition.class))
		{
			DynamicPosition pos = (DynamicPosition) value;
			return pos.getSaveableString();
		} else if (impl.equals(IFunction1D.class))
		{
			return FunctionFactory.createStringFromFunction((IFunction1D) value);
		} else if (impl.equals(String.class))
		{
			return value.toString();
		}
		
		else
		{
			throw new IllegalStateException("Unknown implementation: " + impl + ". Can not parse string.");
		}
	}
}
