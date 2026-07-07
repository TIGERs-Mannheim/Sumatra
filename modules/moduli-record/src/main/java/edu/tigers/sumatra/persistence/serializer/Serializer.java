package edu.tigers.sumatra.persistence.serializer;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;


/**
 * Abstract class with helper functionality for all Serializers.
 * <p>
 * java:S3011: Intentionally bypassing access control checks
 */
@Log4j2
@SuppressWarnings("java:S3011")
public abstract class Serializer<T> implements PrimitiveSerializer<T>, PrimitiveDeserializer<T>
{
	protected transient GenericSerializer genericSerializer;
	private transient Allocator<T> constructor;

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
		initAllocator(type);
	}


	/**
	 * Necessary for (re-)initialization after metadata deserialization.
	 */
	public void transientInit(GenericSerializer genericSerializer)
	{
		this.genericSerializer = genericSerializer;
		initAllocator(getType());
	}


	@SuppressWarnings("unchecked")
	private void initAllocator(Class<T> type)
	{
		try
		{
			constructor = type.getConstructor()::newInstance;
		} catch (NoSuchMethodException e)
		{
			Constructor<T> ctor = InstanceAllocator.serializationConstructor(type);
			if (ctor == null)
			{
				throw new IllegalStateException(
						"No zero-arg constructor and ReflectionFactory could not synthesise one for " + type, e);
			}
			ctor.setAccessible(true);
			constructor = ctor::newInstance;
		}
	}


	protected Class<T> getType()
	{
		return classForName(name);
	}


	/**
	 * Allocate an instance via a zero-arg constructor when available, otherwise via a
	 * {@link java.lang.reflect.Constructor} synthesised by
	 * {@code sun.reflect.ReflectionFactory#newConstructorForSerialization} that runs
	 * only {@code Object.<init>} (matching {@link java.io.ObjectInputStream} semantics).
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


	private interface Allocator<T>
	{
		T allocate() throws InstantiationException, IllegalAccessException, InvocationTargetException;
	}
}
