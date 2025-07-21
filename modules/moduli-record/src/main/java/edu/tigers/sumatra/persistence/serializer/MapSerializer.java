/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.persistence.serializer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Set;


/**
 * Serializer for regular generic map types
 * which can be reconstructed with a zero argument constructor and repeated .put() calls.
 */
@SuppressWarnings("rawtypes")
public class MapSerializer<T extends Map> extends Serializer<T>
{
	public MapSerializer(GenericSerializer genericSerializer, Class<T> type)
	{
		super(genericSerializer, type);
	}


	@Override
	@SuppressWarnings("unchecked")
	public void serialize(MappedDataOutputStream stream, T object) throws IOException
	{
		stream.write(object.size());

		Set<Map.Entry> entrySet = object.entrySet();
		for (Map.Entry<?, ?> entry : entrySet)
		{
			genericSerializer.serialize(stream, entry.getKey());
			genericSerializer.serialize(stream, entry.getValue());
		}
	}


	@Override
	public T deserialize(ByteBuffer buffer) throws IOException
	{
		T instance = allocate();
		fill(buffer, instance);
		return instance;
	}


	@SuppressWarnings("unchecked")
	protected void fill(ByteBuffer buffer, T instance) throws IOException
	{
		int size = PrimitiveDeserializer.readInt(buffer);
		for (int i = 0; i < size; i++)
		{
			Object key = genericSerializer.deserialize(buffer);
			instance.put(key, genericSerializer.deserialize(buffer));
		}
	}
}
