/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 11, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package com.github.g3force.instanceables;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;


/**
 * An {@link InstanceableClass} can be used to create an object from a class and a set of parameters.
 */
public class InstanceableClass<T>
{
	private final Class<T> impl;
	private final List<InstanceableParameter> ctorParams = new ArrayList<>();
	private final List<InstanceableSetter<?, T>> setterParams = new ArrayList<>();


	public InstanceableClass(final Class<T> impl, final InstanceableParameter... ctorParams)
	{
		this.impl = impl;
		this.ctorParams.addAll(Arrays.asList(ctorParams));
	}


	public static <T> InstanceableClass<T> ic(final Class<T> impl, final InstanceableParameter... ctorParams)
	{
		return new InstanceableClass<>(impl, ctorParams);
	}


	public InstanceableClass<T> setterParam(final InstanceableSetter<?, T> setter)
	{
		setterParams.add(setter);
		return this;
	}


	public InstanceableClass<T> ctorParam(final InstanceableParameter param)
	{
		ctorParams.add(param);
		return this;
	}


	public <R> InstanceableClass<T> setterParam(
			final Class<R> impl,
			final String description,
			final String defaultValue,
			final BiConsumer<T, R> setter,
			final Class<?>... genericsImpls
	)
	{
		setterParams.add(new InstanceableSetter<>(impl, description, defaultValue, setter, genericsImpls));
		return this;
	}


	public InstanceableClass<T> ctorParam(
			final Class<?> impl,
			final String description,
			final String defaultValue,
			final Class<?>... genericsImpls
	)
	{
		ctorParams.add(new InstanceableParameter(impl, description, defaultValue, genericsImpls));
		return this;
	}


	/**
	 * Create a new instance with the specified arguments
	 *
	 * @param args the parameters to the constructor
	 * @return a new instance
	 */
	@SuppressWarnings({ "unchecked" })
	public T newInstance(final Object... args)
	{
		try
		{
			Constructor<?> con = getConstructor();
			return (T) con.newInstance(args);
		} catch (NoSuchMethodException err)
		{
			throw new NotCreateableException("Wrong constructor types.", err);
		} catch (final Exception err)
		{
			throw new NotCreateableException("Can not create instance", err);
		}
	}


	/**
	 * Create a new instance with the specified arguments
	 *
	 * @param values the parameters to the constructor and to the setters
	 * @return a new instance
	 * @throws NotCreateableException if the instance could not be created
	 */
	public T newInstance(final List<String> values)
	{
		if (values.size() != ctorParams.size() + setterParams.size())
		{
			throw new NotCreateableException("Wrong number of parameters: " + values);
		}

		int i = 0;
		List<Object> params = new ArrayList<>(this.ctorParams.size());
		for (InstanceableParameter param : this.ctorParams)
		{
			String value = values.get(i++);
			Object o = param.parseString(value);
			params.add(o);
		}

		T instance = newInstance(params.toArray());
		for (InstanceableSetter<?, T> setter : setterParams)
		{
			String value = values.get(i++);
			setter.apply(instance, value);
		}
		return instance;
	}


	/**
	 * Create a new instance with default parameters (as defined in enum)
	 *
	 * @return a new instance
	 * @throws NotCreateableException if the instance could not be created
	 */
	public T newDefaultInstance()
	{
		if (ctorParams.isEmpty())
		{
			return newInstance();
		}
		List<Object> objParams = new ArrayList<>(ctorParams.size());
		for (InstanceableParameter param : ctorParams)
		{
			Object objParam = param.parseString(param.getDefaultValue());
			objParams.add(objParam);
		}
		T o = newInstance(objParams.toArray());
		for (InstanceableSetter<?, T> setter : setterParams)
		{
			setter.apply(o, setter.getDefaultValue());
		}
		return o;
	}


	/**
	 * @return the public constructor associated with the stored parameters.
	 * @throws NoSuchMethodException if no constructor for the stored parameters could be found.
	 */
	private Constructor<T> getConstructor() throws NoSuchMethodException
	{
		Class<?>[] paramTypes = new Class<?>[ctorParams.size()];
		for (int i = 0; i < ctorParams.size(); i++)
		{
			paramTypes[i] = ctorParams.get(i).getImpl();
		}
		return impl.getConstructor(paramTypes);
	}


	/**
	 * @return a list with the constructor params and the setters
	 */
	public List<IInstanceableParameter> getAllParams()
	{
		List<IInstanceableParameter> allParams = new ArrayList<>(ctorParams);
		allParams.addAll(setterParams);
		return allParams;
	}


	public static class NotCreateableException extends RuntimeException
	{
		private static final long serialVersionUID = 89775383135278930L;


		public NotCreateableException(final String message, final Throwable cause)
		{
			super(message, cause);
		}


		public NotCreateableException(final String message)
		{
			super(message);
		}
	}


	/**
	 * @return the impl
	 */
	public final Class<?> getImpl()
	{
		return impl;
	}
}
