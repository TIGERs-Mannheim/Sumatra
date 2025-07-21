/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.persistence.serializer;

import java.io.IOException;
import java.nio.ByteBuffer;


public interface PrimitiveDeserializer<T>
{
	T deserialize(ByteBuffer buffer) throws IOException;

	static int readInt(ByteBuffer buffer) throws IOException
	{
		int value = 0;
		for (int i = 0; i < 5; i++)
		{
			byte b = buffer.get();
			value |= (b & 0x7F) << i * 7;

			if ((b & 0x80) != 128)
			{
				return value;
			}
		}

		throw new IOException("Oversized VarInt " + value);
	}
}
