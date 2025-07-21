/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.persistence.serializer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;


/**
 * Serializer for the arbitrary collection type T.
 * This serializer can be used for all collections which can be reconstructed
 * with a zero argument constructor and repeated add calls.
 */
@SuppressWarnings("rawtypes")
public class CollectionSerializer<T extends Collection> extends Serializer<T>
{
	public CollectionSerializer(GenericSerializer genericSerializer, Class<T> type)
	{
		super(genericSerializer, type);
	}


	@Override
	public void serialize(MappedDataOutputStream stream, T object) throws IOException
	{
		stream.write(object.size());
		for (Object entry : object)
		{
			genericSerializer.serialize(stream, entry);
		}
	}


	@Override
	@SuppressWarnings("unchecked")
	public T deserialize(ByteBuffer buffer) throws IOException
	{
		T instance = allocate();

		int size = PrimitiveDeserializer.readInt(buffer);
		for (int i = 0; i < size; i++)
		{
			instance.add(genericSerializer.deserialize(buffer));
		}

		return instance;
	}
}
