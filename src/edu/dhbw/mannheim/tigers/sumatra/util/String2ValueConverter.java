/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 17, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.botskills.BotSkillPenaltyShoot.EPenaltyShootFlags;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.SyncedCamFrameBufferV2.MathematicalQuadrants;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AWorldPredictor.PredictorKey;


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
		}
		else if (impl.equals(Integer[].class))
		{
			String[] s = value.split(" *, *");
			if (s.length == 0)
			{
				log.warn("String: " + value + " could not be parsed to Integer[]");
			} else
			{
				s[0] = s[0].replace("[", "");
				s[s.length - 1] = s[s.length - 1].replace("]", "");
				
				Integer[] a = new Integer[s.length];
				for (int i = 0; i < s.length; i++)
				{
					a[i] = Integer.parseInt(s[i]);
				}
				
				return a;
			}
			return new Integer[0];
		}
		else if (impl.equals(PredictorKey[].class))
		{
			String[] s = value.split(" *, *");
			if (s.length == 0)
			{
				log.warn("String: " + value + " could not be parsed to PredictorsKey[]");
			} else
			{
				s[0] = s[0].replace("[", "");
				s[s.length - 1] = s[s.length - 1].replace("]", "");
				
				PredictorKey[] a = new PredictorKey[s.length];
				for (int i = 0; i < s.length; i++)
				{
					try
					{
						a[i] = PredictorKey.valueOf(s[i]);
					} catch (IllegalArgumentException e)
					{
						log.fatal("The entered value for the Worldpredictor is not valid will use: "
								+ PredictorKey.values()[0].toString(), e);
					}
				}
				return a;
			}
			return new PredictorKey[0];
		}
		else if (impl.equals(EPenaltyShootFlags.class))
		{
			try
			{
				return EPenaltyShootFlags.valueOf(value);
			} catch (IllegalArgumentException err)
			{
				return EPenaltyShootFlags.NO_OP;
			}
		}
		else if (impl.equals(MathematicalQuadrants[].class))
		{
			String tmp = value.replace("[", "");
			tmp = tmp.replace("]", "");
			String[] s = tmp.split(" *, *");
			if (tmp.length() == 0)
			{
				return new MathematicalQuadrants[0];
			}
			MathematicalQuadrants[] a = new MathematicalQuadrants[s.length];
			for (int i = 0; i < s.length; i++)
			{
				try
				{
					a[i] = MathematicalQuadrants.valueOf(s[i]);
				} catch (Exception e)
				{
					log.warn("exception while changing the quadrants", e);
					return new MathematicalQuadrants[0];
				}
			}
			return a;
			
		}
		else if (impl.equals(Long.class) || impl.equals(Long.TYPE))
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
		} else if (impl.isArray())
		{
			String[] split = value.split(";");
			Object[] arr;
			try
			{
				arr = (Object[]) Array.newInstance(impl.getComponentType(), split.length);
				
			} catch (ClassCastException err)
			{
				log.error("Could not create array of type " + impl.getComponentType(), err);
				return new Object[0];
			}
			for (int i = 0; i < split.length; i++)
			{
				arr[i] = parseString(impl.getComponentType(), split[i]);
			}
			return arr;
		}
		else if (impl.isArray())
		{
			String[] split = value.split(";");
			Object[] arr = (Object[]) Array.newInstance(impl.getComponentType(), split.length);
			for (int i = 0; i < split.length; i++)
			{
				arr[i] = parseString(impl.getComponentType(), split[i]);
			}
			return arr;
		} else
		{
			throw new IllegalStateException("Unknown implementation: " + impl + ". Can not parse string.");
		}
	}
	
	
	/**
	 * Parse given String-value according to implementation of this parameter
	 * 
	 * @param impl
	 * @param genericsImpls
	 * @param value
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Object parseString(final Class<?> impl, final List<Class<?>> genericsImpls, final String value)
	{
		if (impl.equals(List.class))
		{
			String[] values = value.split(";");
			List<Object> list = new ArrayList<Object>(values.length);
			for (String val : values)
			{
				list.add(parseString(genericsImpls.get(0), val));
			}
			return list;
		}
		throw new IllegalStateException("Unknown implementation: " + impl + ". Can not parse string.");
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
		} else if (impl.isArray())
		{
			Object[] arr = convertPrimitiveArrays(value);
			StringBuilder sb = new StringBuilder();
			boolean first = true;
			for (Object v : arr)
			{
				if (!first)
				{
					sb.append(';');
				}
				first = false;
				sb.append(toString(impl.getComponentType(), v));
			}
			return sb.toString();
		}
		else if (impl.isArray())
		{
			Object[] arr = convertPrimitiveArrays(value);
			StringBuilder sb = new StringBuilder();
			boolean first = true;
			for (Object v : arr)
			{
				if (!first)
				{
					sb.append(';');
				}
				first = false;
				sb.append(toString(impl.getComponentType(), v));
			}
			return sb.toString();
		}
		else if (impl.equals(Integer[].class))
		{
			return Arrays.toString((Integer[]) value);
		}
		else if (impl.equals(PredictorKey[].class))
		{
			return Arrays.toString((PredictorKey[]) value);
		}
		else if (impl.equals(EPenaltyShootFlags.class))
		{
			return ((EPenaltyShootFlags) value).toString();
		}
		else if (impl.equals(MathematicalQuadrants[].class))
		{
			return Arrays.toString((MathematicalQuadrants[]) value);
		} else if (impl.equals(String.class))
		{
			// thats easy :)
			return (String) value;
		}
		else
		{
			throw new IllegalStateException("Unknown implementation: " + impl + ". Can not parse string.");
		}
	}
	
	
	private static Object[] convertPrimitiveArrays(final Object arr)
	{
		if (arr instanceof Object[])
		{
			return (Object[]) arr;
		}
		Object[] ret;
		if (arr instanceof int[])
		{
			int[] pArr = (int[]) arr;
			ret = new Integer[pArr.length];
			for (int i = 0; i < pArr.length; i++)
			{
				ret[i] = (int) pArr[i];
			}
		} else if (arr instanceof long[])
		{
			long[] pArr = (long[]) arr;
			ret = new Long[pArr.length];
			for (int i = 0; i < pArr.length; i++)
			{
				ret[i] = (long) pArr[i];
			}
		} else if (arr instanceof float[])
		{
			float[] pArr = (float[]) arr;
			ret = new Float[pArr.length];
			for (int i = 0; i < pArr.length; i++)
			{
				ret[i] = (float) pArr[i];
			}
		} else if (arr instanceof double[])
		{
			double[] pArr = (double[]) arr;
			ret = new Double[pArr.length];
			for (int i = 0; i < pArr.length; i++)
			{
				ret[i] = (double) pArr[i];
			}
		} else if (arr instanceof char[])
		{
			char[] pArr = (char[]) arr;
			ret = new Character[pArr.length];
			for (int i = 0; i < pArr.length; i++)
			{
				ret[i] = (char) pArr[i];
			}
		} else
		{
			throw new IllegalArgumentException("Unknown array implementation: " + arr);
		}
		return ret;
	}
	
	
	/**
	 * @param value either a Class or a String
	 * @return
	 */
	public static Class<?> getClassFromValue(final Object value)
	{
		if (value.getClass() == Class.class)
		{
			return (Class<?>) value;
		}
		String clazz = (String) value;
		if (clazz.equals("int"))
		{
			return Integer.TYPE;
		}
		if (clazz.equals("long"))
		{
			return Long.TYPE;
		}
		if (clazz.equals("float"))
		{
			return Float.TYPE;
		}
		if (clazz.equals("double"))
		{
			return Double.TYPE;
		}
		if (clazz.equals("boolean"))
		{
			return Boolean.TYPE;
		}
		try
		{
			return Class.forName(clazz);
		} catch (ClassNotFoundException err)
		{
			return String.class;
		}
	}
}
