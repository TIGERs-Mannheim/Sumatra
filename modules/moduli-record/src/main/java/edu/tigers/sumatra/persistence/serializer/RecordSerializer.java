/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.persistence.serializer;

import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;


/**
 * Serializer for the arbitrary record type T.
 * The serializer uses regular reflection as unsafe access is restricted on records.
 * Will likely fail when reflection access to the record is restricted by a module.
 */
@Log4j2
public class RecordSerializer<T extends Record> extends Serializer<T>
{

	private CompoundField<?>[] fields;

	private transient Constructor<T> constructor;


	public RecordSerializer(GenericSerializer genericSerializer, Class<T> type)
	{
		super(genericSerializer, type);
		fields = ObjectSerializer.getFields(genericSerializer, type, false);
	}


	@Override
	public void serialize(MappedDataOutputStream stream, T object) throws IOException
	{
		for (CompoundField<?> field : fields)
		{
			field.serializeSafe(stream, object);
		}
	}


	@Override
	public T deserialize(ByteBuffer buffer) throws IOException
	{
		Object[] values = new Object[fields.length];
		int i = 0;
		for (CompoundField<?> field : fields)
		{
			values[i++] = field.deserialize(buffer);
		}

		if (constructor == null)
		{
			return null;
		}

		try
		{
			return constructor.newInstance(values);
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException e)
		{
			throw new IOException("Could not deserialize record", e);
		}
	}


	@Override
	public void transientInit(GenericSerializer genericSerializer)
	{
		Class<?>[] types = new Class[fields.length];
		int i = 0;
		for (CompoundField<?> field : fields)
		{
			field.initDeserializer(genericSerializer, false);
			types[i++] = field.getType();
		}

		try
		{
			constructor = getType().getConstructor(types);
		} catch (NoSuchMethodException e)
		{
			log.warn("Could not find public record constructor with serialized signature, substituting with null.", e);
		}
	}
}
