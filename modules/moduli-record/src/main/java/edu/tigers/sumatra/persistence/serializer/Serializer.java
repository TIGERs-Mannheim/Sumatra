/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.persistence.serializer;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;


/**
 * Abstract class with helper functionality for all Serializers.
 */
@Log4j2
public abstract class Serializer<T> implements PrimitiveSerializer<T>, PrimitiveDeserializer<T>
{
	protected transient GenericSerializer genericSerializer;
	private transient Constructor<T> constructor;

	/**
	 * Numerical ID of the serializer, necessary for type reconstruction in GenericSerializer during deserialization.
	 */
	@Getter
	private final int id;
	/**
	 * Class name of the serialized type.
	 */
	private final String name;


	@SuppressWarnings("unchecked")
	public static <T> Class<T> classForName(String name)
	{
		try
		{
			return (Class<T>) Class.forName(name);
		} catch (ClassNotFoundException e)
		{
			log.warn("Could not find the serialized class {}, substituting with java.lang.Object", name);
			return (Class<T>) Object.class;
		}
	}


	protected Serializer(GenericSerializer genericSerializer, Class<T> type)
	{
		this.genericSerializer = genericSerializer;
		this.id = genericSerializer.getSerializerId();
		this.name = type.getName();
	}


	/**
	 * Necessary for (re-)initialization after metadata deserialization.
	 */
	@SuppressWarnings("unchecked")
	public void transientInit(GenericSerializer genericSerializer)
	{
		this.genericSerializer = genericSerializer;
		Class<T> type = getType();

		try
		{
			constructor = type.getConstructor()::newInstance;
		} catch (NoSuchMethodException e)
		{
			constructor = () -> (T) FieldSerializer.UNSAFE.allocateInstance(type);
		}
	}


	protected Class<T> getType()
	{
		return classForName(name);
	}


	/**
	 * Allocate an instance using a zero argument constructor (if present) or unsafe allocation otherwise.
	 */
	protected T allocate() throws IOException
	{
		try
		{
			return constructor.allocate();
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException e)
		{
			throw new IOException("Could not allocate class", e);
		}
	}


	@Override
	public String toString()
	{
		return id + ":" + getClass().getSimpleName() + ":" + name;
	}


	private interface Constructor<T>
	{
		T allocate() throws InstantiationException, IllegalAccessException, InvocationTargetException;
	}
}
