/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.persistence.serializer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;


/**
 * Serializer for Strings to take advantage of compact UTF serialization.
 */
public class StringSerializer extends Serializer<String>
{
	public static String read(ByteBuffer buffer) throws IOException
	{
		byte[] array = new byte[PrimitiveDeserializer.readInt(buffer)];
		buffer.get(array);
		return new String(array, StandardCharsets.UTF_8);
	}


	public StringSerializer(GenericSerializer genericSerializer)
	{
		super(genericSerializer, String.class);
	}


	@Override
	public String deserialize(ByteBuffer buffer) throws IOException
	{
		return read(buffer);
	}


	@Override
	public void serialize(MappedDataOutputStream stream, String object) throws IOException
	{
		stream.write(object);
	}
}
