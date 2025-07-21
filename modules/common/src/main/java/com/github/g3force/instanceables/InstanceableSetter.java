package com.github.g3force.instanceables;

import com.github.g3force.s2vconverter.String2ValueConverter;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;


/**
 * An instanceable parameter for a setter (applied after instance creation).
 *
 * @param <T> Class of the parameter
 * @param <R> Class of the setter
 */
public class InstanceableSetter<T, R> implements IInstanceableParameter
{
	private static final String2ValueConverter VALUE_CONVERTER = String2ValueConverter.getDefault();

	private final Class<T> impl;
	private final String description;
	private final String defaultValue;
	private final List<Class<?>> genericsImpls;
	private final BiConsumer<R, T> setter;


	public InstanceableSetter(
			final Class<T> impl,
			final String description,
			final String defaultValue,
			final BiConsumer<R, T> setter,
			final Class<?>... genericsImpls
	)
	{
		this.impl = impl;
		this.description = description;
		this.defaultValue = defaultValue;
		this.setter = setter;
		this.genericsImpls = Arrays.asList(genericsImpls);
	}


	@Override
	public T parseString(final String value)
	{
		if (genericsImpls.isEmpty())
		{
			return VALUE_CONVERTER.parse(impl, value);
		}
		return VALUE_CONVERTER.parse(impl, genericsImpls, value);
	}


	public void apply(R instance, String value)
	{
		T paramInstance = parseString(value);
		setter.accept(instance, paramInstance);
	}


	@Override
	public Class<T> getImpl()
	{
		return impl;
	}


	@Override
	public String getDescription()
	{
		return description;
	}


	@Override
	public String getDefaultValue()
	{
		return defaultValue;
	}
}
