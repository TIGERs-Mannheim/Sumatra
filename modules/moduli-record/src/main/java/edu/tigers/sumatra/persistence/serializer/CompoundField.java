/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.persistence.serializer;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;


/**
 * Field wrapper allowing for either Reflection access (for records) or Unsafe access (for arbitrary objects).
 */
@Log4j2
public class CompoundField<T>
{
	private static final Map<String, Class<?>> primitiveNames = new HashMap<>();
	private static final Map<Class<?>, FieldSerializer<?>> primitiveFieldSerializers = new HashMap<>();


	private final String declaringClass;
	@Getter
	private final String name;
	private final String typeName;

	@Getter
	private transient Class<?> type;
	private transient Field field;
	private transient long offset;
	private transient FieldSerializer<?> fieldSerializer;


	static
	{
		primitive(boolean.class, new FieldSerializer.BooleanFieldSerializer());
		primitive(char.class, new FieldSerializer.CharFieldSerializer());
		primitive(byte.class, new FieldSerializer.ByteFieldSerializer());
		primitive(short.class, new FieldSerializer.ShortFieldSerializer());
		primitive(int.class, new FieldSerializer.IntFieldSerializer());
		primitive(long.class, new FieldSerializer.LongFieldSerializer());
		primitive(float.class, new FieldSerializer.FloatFieldSerializer());
		primitive(double.class, new FieldSerializer.DoubleFieldSerializer());
	}


	@SuppressWarnings("unchecked")
	public static <T> FieldSerializer<T> getFieldSerializer(GenericSerializer genericSerializer, Class<T> type)
	{
		if (primitiveFieldSerializers.containsKey(type))
		{
			return (FieldSerializer<T>) primitiveFieldSerializers.get(type);
		}

		return (FieldSerializer<T>) genericSerializer;
	}


	private static <T> void primitive(Class<T> type, FieldSerializer<T> fieldSerializer)
	{
		primitiveNames.put(type.getName(), type);
		primitiveFieldSerializers.put(type, fieldSerializer);
	}


	public CompoundField(GenericSerializer genericSerializer, Field field, boolean unsafe)
	{
		declaringClass = field.getDeclaringClass().getName();
		name = field.getName();

		type = field.getType();
		typeName = type.getName();

		this.field = field;
		init(genericSerializer, unsafe);
	}


	public void initDeserializer(GenericSerializer genericSerializer, boolean unsafe)
	{
		type = primitiveNames.get(typeName);
		if (type == null)
		{
			type = Serializer.classForName(typeName);
		}

		findField(Serializer.classForName(declaringClass));
		init(genericSerializer, unsafe);
	}


	public void serializeSafe(MappedDataOutputStream stream, Object object) throws IOException
	{
		try
		{
			fieldSerializer.serializeSafe(field, stream, object);
		} catch (IllegalAccessException e)
		{
			throw new IOException("Could not get field", e);
		}
	}


	public void serializeUnsafe(MappedDataOutputStream stream, Object object) throws IOException
	{
		fieldSerializer.serializeUnsafe(offset, stream, object);
	}


	@SuppressWarnings("unchecked")
	public T deserialize(ByteBuffer buffer) throws IOException
	{
		return (T) fieldSerializer.deserialize(buffer);
	}


	public void deserializeUnsafe(ByteBuffer buffer, Object object) throws IOException
	{
		if (field == null)  // Field does not exist anymore
		{
			deserialize(buffer);
			return;
		}

		fieldSerializer.deserializeUnsafe(offset, buffer, object);
	}


	// Accessibility bypass is necessary for private field (de-)serialization
	// Replacement for objectFieldOffset would be in jdk.internal.misc.Unsafe - which needs a java call change for access
	@SuppressWarnings({ "deprecation", "java:S3011" })
	private void init(GenericSerializer genericSerializer, boolean unsafe)
	{
		fieldSerializer = getFieldSerializer(genericSerializer, type);

		if (field == null)  // Field does not exist anymore
		{
			return;
		}

		if (unsafe)
		{
			offset = FieldSerializer.UNSAFE.objectFieldOffset(field);
		} else
		{
			field.setAccessible(true);
		}
	}


	private void findField(Class<?> declaringClass)
	{
		try
		{
			field = declaringClass.getDeclaredField(name);
			if (field.getType() == type)
			{
				return;
			}

		} catch (NoSuchFieldException e)
		{
			// Ignored
		}

		Class<?> superclass = declaringClass.getSuperclass();
		if (superclass == Object.class)
		{
			log.warn("Could not find field {}, skipping the field during deserialization.", this);
			field = null;
			return;
		}

		findField(superclass);
	}


	@Override
	public String toString()
	{
		return typeName + " " + declaringClass + "." + name;
	}
}
