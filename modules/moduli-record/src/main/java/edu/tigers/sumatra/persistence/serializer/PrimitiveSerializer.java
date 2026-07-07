package edu.tigers.sumatra.persistence.serializer;

import java.io.IOException;


public interface PrimitiveSerializer<T>
{
	void serialize(MappedDataOutputStream stream, T object) throws IOException;
}
