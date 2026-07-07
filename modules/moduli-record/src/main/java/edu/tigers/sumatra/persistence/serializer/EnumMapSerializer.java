package edu.tigers.sumatra.persistence.serializer;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.ByteBuffer;
import java.util.EnumMap;


/**
 * Serializer for java.util.EnumMap, as the EnumMap needs to be correctly initialized with the Enum class during instantiation.
 */
@SuppressWarnings("rawtypes")
public class EnumMapSerializer extends MapSerializer<EnumMap>
{
	private static final VarHandle KEY_TYPE_HANDLE = keyTypeHandle();


	private static VarHandle keyTypeHandle()
	{
		try
		{
			return MethodHandles
					.privateLookupIn(EnumMap.class, MethodHandles.lookup())
					.findVarHandle(EnumMap.class, "keyType", Class.class);
		} catch (NoSuchFieldException | IllegalAccessException e)
		{
			throw new IllegalStateException("Could not access EnumMap.keyType", e);
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
						.getOrCreateSerializer((Class) KEY_TYPE_HANDLE.get(object))
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
