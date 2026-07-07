package edu.tigers.sumatra.persistence.serializer;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;


/**
 * Serializer abstraction for Object and Record field access depending on primitive type
 * (for generic objects see GenericSerializer).
 * <p>
 * Reducing this boilerplate code with functional abstraction leads to primitives being boxed into Objects,
 * causing allocations (significant performance hit).
 * <p>
 * Field access for arbitrary objects goes through a {@link VarHandle} obtained from
 * {@link java.lang.invoke.MethodHandles#privateLookupIn(Class, java.lang.invoke.MethodHandles.Lookup)}.
 * Final instance fields cannot be written through a VarHandle ({@code VarHandle.set} is unsupported on
 * finals), so writes use a {@link MethodHandle} from {@code Lookup.unreflectSetter} after
 * {@code Field.setAccessible(true)} — the JDK's documented escape hatch for serialization libraries.
 * Reads on final fields keep using the read-only VarHandle, which the JIT can specialise.
 * <p>
 * java:S3011: Intentionally bypassing access control checks
 */
@SuppressWarnings("java:S3011")
public interface FieldSerializer<T> extends PrimitiveDeserializer<T>
{


	void serializeSafe(Field field, MappedDataOutputStream stream, Object object)
			throws IOException, IllegalAccessException;

	void serializeUnsafe(VarHandle handle, MappedDataOutputStream stream, Object object) throws IOException;

	void serializeArray(int index, MappedDataOutputStream stream, Object object) throws IOException;

	void deserializeSafe(Field field, ByteBuffer buffer, Object object)
			throws IOException, IllegalAccessException;

	void deserializeUnsafe(VarHandle handle, ByteBuffer buffer, Object object) throws IOException;

	/**
	 * Write to a final instance field via a pre-bound {@link MethodHandle} setter (obtained via
	 * {@code Lookup.unreflectSetter} on a {@code setAccessible(true)} field). The setter has been
	 * {@code asType}'d to {@code (Object, fieldType)void} for primitives, or {@code (Object, Object)void}
	 * for reference types, so the call site passes the receiver as Object.
	 */
	void deserializeFinal(MethodHandle setter, ByteBuffer buffer, Object object) throws IOException;

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
		public void serializeUnsafe(VarHandle handle, MappedDataOutputStream stream, Object object) throws IOException
		{
			stream.write((boolean) handle.get(object));
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
		public void deserializeSafe(Field field, ByteBuffer buffer, Object object) throws IllegalAccessException
		{
			field.setBoolean(object, buffer.get() != 0);
		}


		@Override
		public void deserializeUnsafe(VarHandle handle, ByteBuffer buffer, Object object)
		{
			handle.set(object, buffer.get() != 0);
		}


		@Override
		public void deserializeFinal(MethodHandle setter, ByteBuffer buffer, Object object) throws IOException
		{
			boolean val = buffer.get() != 0;
			try
			{
				setter.invokeExact(object, val);
			} catch (Throwable t)
			{
				throw new IOException("Failed to set final boolean field", t);
			}
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
		public void serializeUnsafe(VarHandle handle, MappedDataOutputStream stream, Object object) throws IOException
		{
			stream.write((char) handle.get(object));
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
		public void deserializeSafe(Field field, ByteBuffer buffer, Object object)
				throws IOException, IllegalAccessException
		{
			field.setChar(object, (char) PrimitiveDeserializer.readInt(buffer));
		}


		@Override
		public void deserializeUnsafe(VarHandle handle, ByteBuffer buffer, Object object) throws IOException
		{
			handle.set(object, (char) PrimitiveDeserializer.readInt(buffer));
		}


		@Override
		public void deserializeFinal(MethodHandle setter, ByteBuffer buffer, Object object) throws IOException
		{
			char val = (char) PrimitiveDeserializer.readInt(buffer);
			try
			{
				setter.invokeExact(object, val);
			} catch (Throwable t)
			{
				throw new IOException("Failed to set final char field", t);
			}
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
		public void serializeUnsafe(VarHandle handle, MappedDataOutputStream stream, Object object) throws IOException
		{
			stream.write((byte) handle.get(object));
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
		public void deserializeSafe(Field field, ByteBuffer buffer, Object object) throws IllegalAccessException
		{
			field.setByte(object, buffer.get());
		}


		@Override
		public void deserializeUnsafe(VarHandle handle, ByteBuffer buffer, Object object)
		{
			handle.set(object, buffer.get());
		}


		@Override
		public void deserializeFinal(MethodHandle setter, ByteBuffer buffer, Object object) throws IOException
		{
			byte val = buffer.get();
			try
			{
				setter.invokeExact(object, val);
			} catch (Throwable t)
			{
				throw new IOException("Failed to set final byte field", t);
			}
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
		public void serializeUnsafe(VarHandle handle, MappedDataOutputStream stream, Object object) throws IOException
		{
			stream.write((short) handle.get(object));
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
		public void deserializeSafe(Field field, ByteBuffer buffer, Object object)
				throws IOException, IllegalAccessException
		{
			field.setShort(object, (short) PrimitiveDeserializer.readInt(buffer));
		}


		@Override
		public void deserializeUnsafe(VarHandle handle, ByteBuffer buffer, Object object) throws IOException
		{
			handle.set(object, (short) PrimitiveDeserializer.readInt(buffer));
		}


		@Override
		public void deserializeFinal(MethodHandle setter, ByteBuffer buffer, Object object) throws IOException
		{
			short val = (short) PrimitiveDeserializer.readInt(buffer);
			try
			{
				setter.invokeExact(object, val);
			} catch (Throwable t)
			{
				throw new IOException("Failed to set final short field", t);
			}
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
		public void serializeUnsafe(VarHandle handle, MappedDataOutputStream stream, Object object) throws IOException
		{
			stream.write((int) handle.get(object));
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
		public void deserializeSafe(Field field, ByteBuffer buffer, Object object)
				throws IOException, IllegalAccessException
		{
			field.setInt(object, PrimitiveDeserializer.readInt(buffer));
		}


		@Override
		public void deserializeUnsafe(VarHandle handle, ByteBuffer buffer, Object object) throws IOException
		{
			handle.set(object, PrimitiveDeserializer.readInt(buffer));
		}


		@Override
		public void deserializeFinal(MethodHandle setter, ByteBuffer buffer, Object object) throws IOException
		{
			int val = PrimitiveDeserializer.readInt(buffer);
			try
			{
				setter.invokeExact(object, val);
			} catch (Throwable t)
			{
				throw new IOException("Failed to set final int field", t);
			}
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
		public void serializeUnsafe(VarHandle handle, MappedDataOutputStream stream, Object object) throws IOException
		{
			stream.write((long) handle.get(object));
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
		public void deserializeSafe(Field field, ByteBuffer buffer, Object object) throws IllegalAccessException
		{
			field.setLong(object, buffer.getLong());
		}


		@Override
		public void deserializeUnsafe(VarHandle handle, ByteBuffer buffer, Object object)
		{
			handle.set(object, buffer.getLong());
		}


		@Override
		public void deserializeFinal(MethodHandle setter, ByteBuffer buffer, Object object) throws IOException
		{
			long val = buffer.getLong();
			try
			{
				setter.invokeExact(object, val);
			} catch (Throwable t)
			{
				throw new IOException("Failed to set final long field", t);
			}
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
		public void serializeUnsafe(VarHandle handle, MappedDataOutputStream stream, Object object) throws IOException
		{
			stream.write((float) handle.get(object));
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
		public void deserializeSafe(Field field, ByteBuffer buffer, Object object) throws IllegalAccessException
		{
			field.setFloat(object, Float.intBitsToFloat(buffer.getInt()));
		}


		@Override
		public void deserializeUnsafe(VarHandle handle, ByteBuffer buffer, Object object)
		{
			handle.set(object, Float.intBitsToFloat(buffer.getInt()));
		}


		@Override
		public void deserializeFinal(MethodHandle setter, ByteBuffer buffer, Object object) throws IOException
		{
			float val = Float.intBitsToFloat(buffer.getInt());
			try
			{
				setter.invokeExact(object, val);
			} catch (Throwable t)
			{
				throw new IOException("Failed to set final float field", t);
			}
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
		public void serializeUnsafe(VarHandle handle, MappedDataOutputStream stream, Object object) throws IOException
		{
			stream.write((double) handle.get(object));
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
		public void deserializeSafe(Field field, ByteBuffer buffer, Object object) throws IllegalAccessException
		{
			field.setDouble(object, Float.intBitsToFloat(buffer.getInt()));
		}


		@Override
		public void deserializeUnsafe(VarHandle handle, ByteBuffer buffer, Object object)
		{
			handle.set(object, (double) Float.intBitsToFloat(buffer.getInt()));
		}


		@Override
		public void deserializeFinal(MethodHandle setter, ByteBuffer buffer, Object object) throws IOException
		{
			double val = Float.intBitsToFloat(buffer.getInt());
			try
			{
				setter.invokeExact(object, val);
			} catch (Throwable t)
			{
				throw new IOException("Failed to set final double field", t);
			}
		}


		@Override
		public void deserializeArray(int index, ByteBuffer buffer, Object array)
		{
			Array.setDouble(array, index, Float.intBitsToFloat(buffer.getInt()));
		}
	}
}
