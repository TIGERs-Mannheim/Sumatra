package edu.tigers.sumatra.persistence.serializer;

import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystems;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;


/**
 * Serializer for arbitrary object types.
 * Prefixes each object prior to serialization with an integer id to determine types during deserialization.
 * For objects with special fields (e.g. direct memory, reflection, data in transient fields)
 * custom serializers need to be written prior to serialization.
 * <p>
 * Stores the class structure metadata in a separate metadata file (with the statically typed metadataSerializer).
 * Id 0 corresponds to the special case of a null object.
 * <p>
 * java:S3011: Intentionally bypassing access control checks
 */
@Log4j2
@SuppressWarnings({ "rawtypes", "java:S3011" })
public class GenericSerializer implements PrimitiveSerializer, FieldSerializer<Object>, AutoCloseable
{
	private static final int METADATA_VERSION = 1;
	private static final GenericSerializer metadataSerializer = new GenericSerializer();

	private final Map<Class<?>, Serializer<?>> serializers = new HashMap<>();
	private final Map<Integer, Serializer<?>> deserializers = new HashMap<>();
	private final MappedDataOutputStream metadataStream;

	private int idCounter = 0;

	static
	{
		// Preregister ALL types necessary for bootstrapping the deserialization of serializer metadata
		for (Class<?> type : new Class[] { ArraySerializer.class, CollectionSerializer.class, CompoundField.class,
				EnumSerializer.class, MapSerializer.class, ObjectSerializer.class, RecordSerializer.class,
				CompoundField[].class, String[].class, String.class, StringSerializer.class,
				EnumMapSerializer.class })
		{
			metadataSerializer.getOrCreateSerializer(type).transientInit(metadataSerializer);
		}

		try
		{
			metadataSerializer.close();
		} catch (IOException e)
		{
			log.error("Could not close metadata serializer", e);
		}
	}


	public int getSerializerId()
	{
		return ++idCounter;
	}


	/**
	 * Constructor for the (statically bootstrapped) metadata serializer.
	 */
	private GenericSerializer()
	{
		try
		{
			metadataStream = new MappedDataOutputStream(
					FileSystems.getDefault()
							.getPath(System.getProperty("os.name").startsWith("Windows") ? "NUL" : "/dev/null"),
					MemorySegment.ofArray(new byte[4096])
			);
		} catch (IOException e)
		{
			throw new IllegalStateException("Could not open null file channel", e);
		}
	}


	public GenericSerializer(Path metadata) throws IOException
	{
		int version = 0;

		// Read metadata for deserialization setup.
		try (FileChannel channel = FileChannel.open(metadata, StandardOpenOption.READ))
		{
			ByteBuffer buffer = ByteBuffer.allocate((int) channel.size());
			channel.read(buffer);
			buffer.position(0);
			version = PrimitiveDeserializer.readInt(buffer);
			if (version != METADATA_VERSION)
			{
				throw new IOException("Cannot deserialize metadata of version " + version);
			}

			while (buffer.hasRemaining())
			{
				Serializer<?> serializer = (Serializer<?>) metadataSerializer.deserialize(buffer);
				// GenericSerializer has not been .closed() during serialization, leading to the metadata file not being truncated correctly
				if (serializer == null)
				{
					break;
				}

				serializer.transientInit(this);
				serializers.put(serializer.getType(), serializer);
				deserializers.put(serializer.getId(), serializer);
				idCounter = Math.max(idCounter, serializer.getId() + 1);
			}
		} catch (NoSuchFileException e)
		{
			// Generic serializer has been opened for writing: Metadata file does not exist yet
		}

		metadataStream = new MappedDataOutputStream(metadata);

		if (version == 0)
		{
			metadataStream.write(METADATA_VERSION);
		}
	}


	@Override
	public void serialize(MappedDataOutputStream stream, Object object) throws IOException
	{
		if (object == null)
		{
			stream.write(0);
		} else
		{
			serializeNotNull(stream, object);
		}
	}


	@SuppressWarnings("unchecked")
	private <T> void serializeNotNull(MappedDataOutputStream stream, T object) throws IOException
	{
		Serializer<T> serializer = ((Serializer<T>) getOrCreateSerializer(object.getClass()));

		stream.write(serializer.getId());
		serializer.serialize(stream, object);
	}


	@Override
	public Object deserialize(ByteBuffer buffer) throws IOException
	{
		int id = PrimitiveDeserializer.readInt(buffer);
		if (id == 0)
		{
			return null;
		}

		Serializer<?> serializer = deserializers.get(id);
		if (serializer == null)
		{
			throw new IOException("Corrupted data: unknown serializer " + id);
		}

		return serializer.deserialize(buffer);
	}


	@SuppressWarnings("unchecked")
	public <T> Serializer<T> getSerializer(int id)
	{
		return (Serializer<T>) deserializers.get(id);
	}


	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T> Serializer<T> getOrCreateSerializer(Class<T> type)
	{
		Serializer<T> serializer = (Serializer<T>) serializers.get(type);
		if (serializer != null)
		{
			return serializer;
		}

		synchronized (serializers)
		{
			//SingletonList, SingletonMap, UnmodifiableCollection...
			boolean collectionsClass = type.getName().startsWith("java.util.Collections")
					|| type.getName().startsWith("java.util.ImmutableCollections");

			if (type.isEnum())
			{
				serializer = new EnumSerializer(this, type);
			} else if (type.isRecord())
			{
				serializer = new RecordSerializer(this, type);
			} else if (type.isArray())
			{
				serializer = new ArraySerializer(this, type);
			} else if (Map.class.isAssignableFrom(type) && !collectionsClass)
			{
				if (type == EnumMap.class)
				{
					serializer = (Serializer<T>) new EnumMapSerializer(this);
				} else
				{
					serializer = new MapSerializer(this, type);
				}
			} else if (Collection.class.isAssignableFrom(type) && !collectionsClass)
			{
				serializer = new CollectionSerializer(this, type);
			} else if (type == String.class)
			{
				serializer = (Serializer<T>) new StringSerializer(this);
			} else
			{
				serializer = new ObjectSerializer<>(this, type);
			}

			serializers.put(type, serializer);
			deserializers.put(serializer.getId(), serializer);
			try
			{
				metadataSerializer.serialize(metadataStream, serializer);
			} catch (IOException e)
			{
				log.error("Could not serialize serializer, deserialization likely corrupted", e);
			}
		}

		return serializer;
	}


	@Override
	public void serializeSafe(Field field, MappedDataOutputStream stream, Object object)
			throws IOException, IllegalAccessException
	{
		serialize(stream, field.get(object));
	}


	@Override
	public void serializeUnsafe(VarHandle handle, MappedDataOutputStream stream, Object object) throws IOException
	{
		serialize(stream, handle.get(object));
	}


	@Override
	public void serializeArray(int index, MappedDataOutputStream stream, Object object) throws IOException
	{
		serialize(stream, Array.get(object, index));
	}


	@Override
	public void deserializeSafe(Field field, ByteBuffer buffer, Object object)
			throws IOException, IllegalAccessException
	{
		field.set(object, deserialize(buffer));
	}


	@Override
	public void deserializeUnsafe(VarHandle handle, ByteBuffer buffer, Object object) throws IOException
	{
		handle.set(object, deserialize(buffer));
	}


	@Override
	public void deserializeFinal(MethodHandle setter, ByteBuffer buffer, Object object) throws IOException
	{
		Object val = deserialize(buffer);
		try
		{
			setter.invokeExact(object, val);
		} catch (Throwable t)
		{
			throw new IOException("Failed to set final reference field", t);
		}
	}


	@Override
	public void deserializeArray(int index, ByteBuffer buffer, Object array) throws IOException
	{
		Array.set(array, index, deserialize(buffer));
	}


	@Override
	public void close() throws IOException
	{
		metadataStream.close();
	}
}
