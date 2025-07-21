/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.persistence.serializer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.EnumMap;


/**
 * Serializer for java.util.EnumMap, as the EnumMap needs to be correctly initialized with the Enum class during instantiation.
 */
@SuppressWarnings("rawtypes")
public class EnumMapSerializer extends MapSerializer<EnumMap>
{

	private static final long KEY_TYPE_OFFSET = getKeyTypeOffset();


	@SuppressWarnings("deprecation")
	private static long getKeyTypeOffset()
	{
		try
		{
			// As EnumMap is inside the sealed java.base module, Unsafe is necessary for access.
			return FieldSerializer.UNSAFE.objectFieldOffset(EnumMap.class.getDeclaredField("keyType"));
		} catch (NoSuchFieldException e)
		{
			throw new IllegalStateException("Could not get keyType field of EnumMap", e);
		}
	}


	public EnumMapSerializer(GenericSerializer genericSerializer)
	{
		super(genericSerializer, EnumMap.class);
	}


	@Override
	@SuppressWarnings("unchecked")
	public void serialize(MappedDataOutputStream stream, EnumMap object) throws IOException
	{
		stream.write(
				genericSerializer
						.getOrCreateSerializer((Class) FieldSerializer.UNSAFE.getObject(object, KEY_TYPE_OFFSET))
						.getId()
		);
		super.serialize(stream, object);
	}


	@Override
	@SuppressWarnings("unchecked")
	public EnumMap deserialize(ByteBuffer buffer) throws IOException
	{
		EnumMap instance = new EnumMap(genericSerializer.getSerializer(PrimitiveDeserializer.readInt(buffer)).getType());
		fill(buffer, instance);
		return instance;
	}
}
