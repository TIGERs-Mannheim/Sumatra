/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.persistence.serializer;

import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.Arrays;


/**
 * Serializer for Enums of arbitrary type T by remembering the names in the metadata and serializing the ordinal values.
 */
@Log4j2
public class EnumSerializer<T extends Enum<?>> extends Serializer<T>
{
	private final String[] names;

	private transient T[] fields;


	public EnumSerializer(GenericSerializer genericSerializer, Class<T> type)
	{
		super(genericSerializer, type);
		fields = type.getEnumConstants();
		names = Arrays.stream(fields).map(Enum::name).toArray(String[]::new);
	}


	@Override
	public void serialize(MappedDataOutputStream stream, T object) throws IOException
	{
		stream.write(object.ordinal());
	}


	@SuppressWarnings("unchecked")
	@Override
	public void transientInit(GenericSerializer genericSerializer)
	{
		Class<T> type = getType();
		T[] currentFields = type.getEnumConstants();

		fields = (T[]) Array.newInstance(type, names.length);
		for (int i = 0; i < names.length; i++)
		{
			for (T field : currentFields)
			{
				if (field.name().equals(names[i]))
				{
					fields[i] = field;
					break;
				}
			}

			if (fields[i] == null)
			{
				log.warn(
						"Could not find enum constant {}.{}, substituting with {}. Expect issues.",
						type.getName(), names[i], currentFields[0]
				);
				fields[i] = currentFields[0];
			}
		}
	}


	@Override
	public T deserialize(ByteBuffer buffer) throws IOException
	{
		return fields[PrimitiveDeserializer.readInt(buffer)];
	}
}
