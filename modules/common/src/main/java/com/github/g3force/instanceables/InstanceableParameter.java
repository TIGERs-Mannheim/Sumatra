/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 11, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package com.github.g3force.instanceables;

import com.github.g3force.s2vconverter.String2ValueConverter;

import java.util.Arrays;
import java.util.List;


/**
 * Parameter of a {@link InstanceableClass}.
 */
public class InstanceableParameter implements IInstanceableParameter
{
	private static final String2ValueConverter VALUE_CONVERTER = String2ValueConverter.getDefault();

	private final Class<?> impl;
	private final String description;
	private final String defaultValue;
	private final List<Class<?>> genericsImpls;


	public InstanceableParameter(
			final Class<?> impl,
			final String description,
			final String defaultValue,
			final Class<?>... genericsImpls
	)
	{
		this.impl = impl;
		this.description = description;
		this.defaultValue = defaultValue;
		this.genericsImpls = Arrays.asList(genericsImpls);
	}


	/**
	 * Parse given String to value
	 *
	 * @param value the value to be parsed
	 * @return the new instance
	 */
	@Override
	public Object parseString(final String value)
	{
		if (genericsImpls.isEmpty())
		{
			return VALUE_CONVERTER.parseString(impl, value);
		}
		return VALUE_CONVERTER.parseString(impl, genericsImpls, value);
	}


	/**
	 * @return the impl
	 */
	@Override
	public final Class<?> getImpl()
	{
		return impl;
	}


	/**
	 * @return the description
	 */
	@Override
	public final String getDescription()
	{
		return description;
	}


	/**
	 * @return the defaultValue
	 */
	@Override
	public final String getDefaultValue()
	{
		return defaultValue;
	}
}
