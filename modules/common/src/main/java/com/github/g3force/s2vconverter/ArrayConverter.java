package com.github.g3force.s2vconverter;

import lombok.extern.log4j.Log4j2;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.stream.IntStream;


/**
 * Convert arrays.
 */
@Log4j2
public class ArrayConverter implements IString2ValueConverter
{
	private final IString2ValueConverter parent;


	/**
	 * Default no-arg constructor for service loader
	 */
	@SuppressWarnings("unused")
	public ArrayConverter()
	{
		parent = String2ValueConverter.getDefault();
	}


	@Override
	public boolean supportedClass(final Class<?> clazz)
	{
		return clazz.isArray();
	}


	@Override
	public Object parseString(final Class<?> impl, final String value)
	{
		String[] split = value.split(";");
		Object[] arr;
		try
		{
			arr = (Object[]) Array.newInstance(impl.getComponentType(),
					split.length);

		} catch (ClassCastException err)
		{
			log.error("Could not create array of type {}", impl.getComponentType(), err);
			return new Object[0];
		}
		for (int i = 0; i < split.length; i++)
		{
			arr[i] = parent.parseString(impl.getComponentType(), split[i]);
		}
		return arr;
	}


	@Override
	public String toString(final Class<?> impl, final Object value)
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
			sb.append(parent.toString(impl.getComponentType(), v));
		}
		return sb.toString();
	}


	private Object[] convertPrimitiveArrays(final Object arr)
	{
		if (arr instanceof Object[] obj)
		{
			return obj;
		}
		return switch (arr)
		{
			case int[] pArr -> Arrays.stream(pArr).boxed().toArray(Integer[]::new);
			case long[] pArr -> Arrays.stream(pArr).boxed().toArray(Long[]::new);
			case float[] pArr -> IntStream.range(0, pArr.length)
					.mapToObj(i -> pArr[i])
					.toArray(Float[]::new);
			case double[] pArr -> Arrays.stream(pArr).boxed().toArray(Double[]::new);
			case char[] pArr -> IntStream.range(0, pArr.length)
					.mapToObj(i -> pArr[i])
					.toArray(Character[]::new);
			default -> throw new IllegalArgumentException("Unknown array implementation: " + arr);
		};
	}
}
