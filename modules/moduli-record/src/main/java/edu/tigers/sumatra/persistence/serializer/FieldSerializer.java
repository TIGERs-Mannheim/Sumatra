/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.persistence.serializer;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;


/**
 * Serializer abstraction for Object and Record field access depending on primitive type
 * (for generic objects see GenericSerializer).
 *
 * Reducing this boilerplate code with functional abstraction leads to primitives being boxed into Objects,
 * causing allocations (significant performance hit).
 */
// Usage of Unsafe is unavoidable for classes without zero-args constructor and module protected classes
@SuppressWarnings("java:S1191")
public interface FieldSerializer<T> extends PrimitiveDeserializer<T>
{
	sun.misc.Unsafe UNSAFE = getUnsafe();


	@SuppressWarnings("java:S3011")  // The accessibility bypass is necessary to access Unsafe.theUnsafe
	static sun.misc.Unsafe getUnsafe()
	{
		try
		{
			Field unsafeField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
			unsafeField.setAccessible(true);
			return (sun.misc.Unsafe) unsafeField.get(null);
		} catch (NoSuchFieldException | IllegalAccessException e)
		{
			throw new IllegalStateException("Could not access sun.misc.Unsafe instance", e);
		}
	}


	void serializeSafe(Field field, MappedDataOutputStream stream, Object object)
			throws IOException, IllegalAccessException;

	void serializeUnsafe(long offset, MappedDataOutputStream stream, Object object) throws IOException;

	void serializeArray(int index, MappedDataOutputStream stream, Object object) throws IOException;

	void deserializeUnsafe(long offset, ByteBuffer buffer, Object object) throws IOException;

	void deserializeArray(int index, ByteBuffer buffer, Object array) throws IOException;

	class BooleanFieldSerializer implements FieldSerializer<Boolean>
	{

		@Override
		public void serializeSafe(Field field, MappedDataOutputStream stream, Object object)
				throws IOException, IllegalAccessException
		{
			stream.write(field.getBoolean(object));
		}


		@Override
		public void serializeUnsafe(long offset, MappedDataOutputStream stream, Object object) throws IOException
		{
			stream.write(UNSAFE.getBoolean(object, offset));
		}


		@Override
		public void serializeArray(int index, MappedDataOutputStream stream, Object object) throws IOException
		{
			stream.write(Array.getBoolean(object, index));
		}


		@Override
		public Boolean deserialize(ByteBuffer buffer)
		{
			return buffer.get() != 0;
		}


		@Override
		public void deserializeUnsafe(long offset, ByteBuffer buffer, Object object)
		{
			UNSAFE.putBoolean(object, offset, buffer.get() != 0);
		}


		@Override
		public void deserializeArray(int index, ByteBuffer buffer, Object array)
		{
			Array.setBoolean(array, index, buffer.get() != 0);
		}
	}

	class CharFieldSerializer implements FieldSerializer<Character>
	{

		@Override
		public void serializeSafe(Field field, MappedDataOutputStream stream, Object object)
				throws IOException, IllegalAccessException
		{
			stream.write(field.getChar(object));
		}


		@Override
		public void serializeUnsafe(long offset, MappedDataOutputStream stream, Object object) throws IOException
		{
			stream.write(UNSAFE.getChar(object, offset));
		}


		@Override
		public void serializeArray(int index, MappedDataOutputStream stream, Object object) throws IOException
		{
			stream.write(Array.getChar(object, index));
		}


		@Override
		public Character deserialize(ByteBuffer buffer) throws IOException
		{
			return (char) PrimitiveDeserializer.readInt(buffer);
		}


		@Override
		public void deserializeUnsafe(long offset, ByteBuffer buffer, Object object) throws IOException
		{
			UNSAFE.putChar(object, offset, (char) PrimitiveDeserializer.readInt(buffer));
		}


		@Override
		public void deserializeArray(int index, ByteBuffer buffer, Object array) throws IOException
		{
			Array.setChar(array, index, (char) PrimitiveDeserializer.readInt(buffer));
		}
	}

	class ByteFieldSerializer implements FieldSerializer<Byte>
	{

		@Override
		public void serializeSafe(Field field, MappedDataOutputStream stream, Object object)
				throws IOException, IllegalAccessException
		{
			stream.write(field.getByte(object));
		}


		@Override
		public void serializeUnsafe(long offset, MappedDataOutputStream stream, Object object) throws IOException
		{
			stream.write(UNSAFE.getByte(object, offset));
		}


		@Override
		public void serializeArray(int index, MappedDataOutputStream stream, Object object) throws IOException
		{
			stream.write(Array.getByte(object, index));
		}


		@Override
		public Byte deserialize(ByteBuffer buffer)
		{
			return buffer.get();
		}


		@Override
		public void deserializeUnsafe(long offset, ByteBuffer buffer, Object object)
		{
			UNSAFE.putByte(object, offset, buffer.get());
		}


		@Override
		public void deserializeArray(int index, ByteBuffer buffer, Object array)
		{
			Array.setByte(array, index, buffer.get());
		}
	}

	class ShortFieldSerializer implements FieldSerializer<Short>
	{
		@Override
		public void serializeSafe(Field field, MappedDataOutputStream stream, Object object)
				throws IOException, IllegalAccessException
		{
			stream.write(field.getShort(object));
		}


		@Override
		public void serializeUnsafe(long offset, MappedDataOutputStream stream, Object object) throws IOException
		{
			stream.write(UNSAFE.getShort(object, offset));
		}


		@Override
		public void serializeArray(int index, MappedDataOutputStream stream, Object object) throws IOException
		{
			stream.write(Array.getShort(object, index));
		}


		@Override
		public Short deserialize(ByteBuffer buffer) throws IOException
		{
			return (short) PrimitiveDeserializer.readInt(buffer);
		}


		@Override
		public void deserializeUnsafe(long offset, ByteBuffer buffer, Object object) throws IOException
		{
			UNSAFE.putShort(object, offset, (short) PrimitiveDeserializer.readInt(buffer));
		}


		@Override
		public void deserializeArray(int index, ByteBuffer buffer, Object array) throws IOException
		{
			Array.setShort(array, index, (short) PrimitiveDeserializer.readInt(buffer));
		}
	}

	class IntFieldSerializer implements FieldSerializer<Integer>
	{

		@Override
		public void serializeSafe(Field field, MappedDataOutputStream stream, Object object)
				throws IOException, IllegalAccessException
		{
			stream.write(field.getInt(object));
		}


		@Override
		public void serializeUnsafe(long offset, MappedDataOutputStream stream, Object object) throws IOException
		{
			stream.write(UNSAFE.getInt(object, offset));
		}


		@Override
		public void serializeArray(int index, MappedDataOutputStream stream, Object object) throws IOException
		{
			stream.write(Array.getInt(object, index));
		}


		@Override
		public Integer deserialize(ByteBuffer buffer) throws IOException
		{
			return PrimitiveDeserializer.readInt(buffer);
		}


		@Override
		public void deserializeUnsafe(long offset, ByteBuffer buffer, Object object) throws IOException
		{
			UNSAFE.putInt(object, offset, PrimitiveDeserializer.readInt(buffer));
		}


		@Override
		public void deserializeArray(int index, ByteBuffer buffer, Object array) throws IOException
		{
			Array.setInt(array, index, PrimitiveDeserializer.readInt(buffer));
		}
	}

	class LongFieldSerializer implements FieldSerializer<Long>
	{

		@Override
		public void serializeSafe(Field field, MappedDataOutputStream stream, Object object)
				throws IOException, IllegalAccessException
		{
			stream.write(field.getLong(object));
		}


		@Override
		public void serializeUnsafe(long offset, MappedDataOutputStream stream, Object object) throws IOException
		{
			stream.write(UNSAFE.getLong(object, offset));
		}


		@Override
		public void serializeArray(int index, MappedDataOutputStream stream, Object object) throws IOException
		{
			stream.write(Array.getLong(object, index));
		}


		@Override
		public Long deserialize(ByteBuffer buffer)
		{
			return buffer.getLong();
		}


		@Override
		public void deserializeUnsafe(long offset, ByteBuffer buffer, Object object)
		{
			UNSAFE.putLong(object, offset, buffer.getLong());
		}


		@Override
		public void deserializeArray(int index, ByteBuffer buffer, Object array)
		{
			Array.setLong(array, index, buffer.getLong());
		}
	}

	class FloatFieldSerializer implements FieldSerializer<Float>
	{

		@Override
		public void serializeSafe(Field field, MappedDataOutputStream stream, Object object)
				throws IOException, IllegalAccessException
		{
			stream.write(field.getFloat(object));
		}


		@Override
		public void serializeUnsafe(long offset, MappedDataOutputStream stream, Object object) throws IOException
		{
			stream.write(UNSAFE.getFloat(object, offset));
		}


		@Override
		public void serializeArray(int index, MappedDataOutputStream stream, Object object) throws IOException
		{
			stream.write(Array.getFloat(object, index));
		}


		@Override
		public Float deserialize(ByteBuffer buffer)
		{
			return Float.intBitsToFloat(buffer.getInt());
		}


		@Override
		public void deserializeUnsafe(long offset, ByteBuffer buffer, Object object)
		{
			UNSAFE.putFloat(object, offset, Float.intBitsToFloat(buffer.getInt()));
		}


		@Override
		public void deserializeArray(int index, ByteBuffer buffer, Object array)
		{
			Array.setFloat(array, index, Float.intBitsToFloat(buffer.getInt()));
		}
	}

	class DoubleFieldSerializer implements FieldSerializer<Double>
	{

		@Override
		public void serializeSafe(Field field, MappedDataOutputStream stream, Object object)
				throws IOException, IllegalAccessException
		{
			stream.write(field.getDouble(object));
		}


		@Override
		public void serializeUnsafe(long offset, MappedDataOutputStream stream, Object object) throws IOException
		{
			stream.write(UNSAFE.getDouble(object, offset));
		}


		@Override
		public void serializeArray(int index, MappedDataOutputStream stream, Object object) throws IOException
		{
			stream.write(Array.getDouble(object, index));
		}


		@Override
		public Double deserialize(ByteBuffer buffer)
		{
			return (double) Float.intBitsToFloat(buffer.getInt());
		}


		@Override
		public void deserializeUnsafe(long offset, ByteBuffer buffer, Object object)
		{
			UNSAFE.putDouble(object, offset, Float.intBitsToFloat(buffer.getInt()));
		}


		@Override
		public void deserializeArray(int index, ByteBuffer buffer, Object array)
		{
			Array.setDouble(array, index, Float.intBitsToFloat(buffer.getInt()));
		}
	}
}
