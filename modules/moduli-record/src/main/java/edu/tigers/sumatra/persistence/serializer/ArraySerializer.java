/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.persistence.serializer;

import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;


/**
 * Serializer for arrays of the arbitrary type T.
 */
public class ArraySerializer<T> extends Serializer<T>
{
	private transient Class<?> componentType;
	private transient FieldSerializer<?> fieldSerializer;


	public ArraySerializer(GenericSerializer genericSerializer, Class<T> type)
	{
		super(genericSerializer, type);
		init(genericSerializer, type);
	}


	@Override
	public void serialize(MappedDataOutputStream stream, T object) throws IOException
	{
		int length = Array.getLength(object);
		stream.write(length);
		for (int i = 0; i < length; i++)
		{
			fieldSerializer.serializeArray(i, stream, object);
		}
	}


	@Override
	@SuppressWarnings("unchecked")
	public T deserialize(ByteBuffer buffer) throws IOException
	{
		int length = PrimitiveDeserializer.readInt(buffer);
		T instance = (T) Array.newInstance(componentType, length);

		for (int i = 0; i < length; i++)
		{
			fieldSerializer.deserializeArray(i, buffer, instance);
		}

		return instance;
	}


	@Override
	public void transientInit(GenericSerializer genericSerializer)
	{
		init(genericSerializer, getType());
	}


	private void init(GenericSerializer genericSerializer, Class<?> type)
	{
		componentType = type.componentType();
		fieldSerializer = CompoundField.getFieldSerializer(genericSerializer, componentType);
	}
}
