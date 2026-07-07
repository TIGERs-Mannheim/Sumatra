package edu.tigers.sumatra.persistence.serializer;

import sun.reflect.ReflectionFactory;  // NOSONAR java:S1191

import java.lang.reflect.Constructor;


/**
 * Replacement for {@code sun.misc.Unsafe.allocateInstance}: synthesises a
 * constructor that runs only {@code Object.<init>}, leaving fields
 * default-initialised. Concrete fields are then populated by the deserializer
 * (VarHandle for non-final, Field reflection for final).
 * <p>Same mechanism {@link java.io.ObjectInputStream} uses for non-Serializable
 * parents — the JDK-sanctioned escape hatch for skipping a class's own
 * constructor without {@code sun.misc.Unsafe}.
 */
@SuppressWarnings("java:S1191")  // sun.reflect.ReflectionFactory is the public-ish accessor
final class InstanceAllocator
{
	private static final ReflectionFactory REFLECTION_FACTORY = ReflectionFactory.getReflectionFactory();
	private static final Constructor<Object> OBJECT_CTOR = objectCtor();


	private InstanceAllocator()
	{
	}


	private static Constructor<Object> objectCtor()
	{
		try
		{
			return Object.class.getConstructor();
		} catch (NoSuchMethodException e)
		{
			throw new IllegalStateException("Object.class has no zero-arg constructor", e);
		}
	}


	@SuppressWarnings("unchecked")
	static <T> Constructor<T> serializationConstructor(Class<T> type)
	{
		return (Constructor<T>) REFLECTION_FACTORY.newConstructorForSerialization(type, OBJECT_CTOR);
	}
}
