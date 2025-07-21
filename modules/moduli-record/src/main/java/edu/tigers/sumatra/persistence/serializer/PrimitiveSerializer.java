/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.persistence.serializer;

import java.io.IOException;


public interface PrimitiveSerializer<T>
{
	void serialize(MappedDataOutputStream stream, T object) throws IOException;
}
