package edu.tigers.sumatra.persistence.serializer;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;


/**
 * Field wrapper allowing for either Reflection access (for records) or Unsafe access (for arbitrary objects).
 */
@Log4j2
public class CompoundField<T>
{
	private static final Map<String, Class<?>> primitiveNames = new HashMap<>();
	private static final Map<Class<?>, FieldSerializer<?>> primitiveFieldSerializers = new HashMap<>();


	private final String declaringClass;
	@Getter
	private final String name;
	private final String typeName;

	@Getter
	private transient Class<?> type;
	private transient Field field;
	private transient VarHandle handle;
	private transient MethodHandle finalSetter;
	private transient FieldSerializer<?> fieldSerializer;


	static
	{
		primitive(boolean.class, new FieldSerializer.BooleanFieldSerializer());
		primitive(char.class, new FieldSerializer.CharFieldSerializer());
		primitive(byte.class, new FieldSerializer.ByteFieldSerializer());
		primitive(short.class, new FieldSerializer.ShortFieldSerializer());
		primitive(int.class, new FieldSerializer.IntFieldSerializer());
		primitive(long.class, new FieldSerializer.LongFieldSerializer());
		primitive(float.class, new FieldSerializer.FloatFieldSerializer());
		primitive(double.class, new FieldSerializer.DoubleFieldSerializer());
	}


	@SuppressWarnings("unchecked")
	public static <T> FieldSerializer<T> getFieldSerializer(GenericSerializer genericSerializer, Class<T> type)
	{
		if (primitiveFieldSerializers.containsKey(type))
		{
			return (FieldSerializer<T>) primitiveFieldSerializers.get(type);
		}

		return (FieldSerializer<T>) genericSerializer;
	}


	private static <T> void primitive(Class<T> type, FieldSerializer<T> fieldSerializer)
	{
		primitiveNames.put(type.getName(), type);
		primitiveFieldSerializers.put(type, fieldSerializer);
	}


	public CompoundField(GenericSerializer genericSerializer, Field field, boolean unsafe)
	{
		declaringClass = field.getDeclaringClass().getName();
		name = field.getName();

		type = field.getType();
		typeName = type.getName();

		this.field = field;
		init(genericSerializer, unsafe);
	}


	public void initDeserializer(GenericSerializer genericSerializer, boolean unsafe)
	{
		type = primitiveNames.get(typeName);
		if (type == null)
		{
			type = Serializer.classForName(typeName);
		}

		findField(Serializer.classForName(declaringClass));
		init(genericSerializer, unsafe);
	}


	public void serializeSafe(MappedDataOutputStream stream, Object object) throws IOException
	{
		try
		{
			fieldSerializer.serializeSafe(field, stream, object);
		} catch (IllegalAccessException e)
		{
			throw new IOException("Could not get field", e);
		}
	}


	public void serializeUnsafe(MappedDataOutputStream stream, Object object) throws IOException
	{
		try
		{
			if (handle != null)
			{
				// VarHandle.get is supported on final instance fields too — only set is restricted.
				fieldSerializer.serializeUnsafe(handle, stream, object);
			} else
			{
				fieldSerializer.serializeSafe(field, stream, object);
			}
		} catch (IllegalAccessException e)
		{
			throw new IOException("Could not serialize " + this, e);
		}
	}


	@SuppressWarnings("unchecked")
	public T deserialize(ByteBuffer buffer) throws IOException
	{
		return (T) fieldSerializer.deserialize(buffer);
	}


	public void deserializeUnsafe(ByteBuffer buffer, Object object) throws IOException
	{
		if (field == null)  // Field does not exist anymore
		{
			deserialize(buffer);
			return;
		}

		try
		{
			if (finalSetter != null)
			{
				// VarHandle.set is unsupported on final fields; use a pre-bound MethodHandle setter
				// (from Lookup.unreflectSetter after setAccessible(true)) — the JDK-sanctioned
				// escape hatch with much lower per-call overhead than Field.setXxx reflection.
				fieldSerializer.deserializeFinal(finalSetter, buffer, object);
			} else if (handle != null)
			{
				fieldSerializer.deserializeUnsafe(handle, buffer, object);
			} else
			{
				fieldSerializer.deserializeSafe(field, buffer, object);
			}
		} catch (IllegalAccessException e)
		{
			throw new IOException("Could not deserialize " + this, e);
		}
	}


	// Accessibility bypass is necessary for private field (de-)serialization
	@SuppressWarnings("java:S3011")
	private void init(GenericSerializer genericSerializer, boolean unsafe)
	{
		fieldSerializer = getFieldSerializer(genericSerializer, type);

		if (field == null)  // Field does not exist anymore
		{
			return;
		}

		if (unsafe)
		{
			try
			{
				MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(
						field.getDeclaringClass(), MethodHandles.lookup());
				// VarHandle works for reads on every instance field (read-only on finals).
				handle = lookup.unreflectVarHandle(field);
				if (Modifier.isFinal(field.getModifiers()))
				{
					// VarHandle.set is unsupported on finals. Lookup.unreflectSetter on a
					// setAccessible(true) field returns a MethodHandle that bypasses the final
					// check — far cheaper per-call than Field.setXxx reflection.
					field.setAccessible(true);
					Class<?> ft = field.getType();
					MethodType target = MethodType.methodType(
							void.class, Object.class, ft.isPrimitive() ? ft : Object.class);
					finalSetter = lookup.unreflectSetter(field).asType(target);
				}
			} catch (IllegalAccessException e)
			{
				throw new IllegalStateException("Could not obtain accessor for " + this, e);
			}
		} else
		{
			field.setAccessible(true);
		}
	}


	private void findField(Class<?> declaringClass)
	{
		try
		{
			field = declaringClass.getDeclaredField(name);
			if (field.getType() == type)
			{
				return;
			}

		} catch (NoSuchFieldException e)
		{
			// Ignored
		}

		Class<?> superclass = declaringClass.getSuperclass();
		if (superclass == Object.class)
		{
			log.warn("Could not find field {}, skipping the field during deserialization.", this);
			field = null;
			return;
		}

		findField(superclass);
	}


	@Override
	public String toString()
	{
		return typeName + " " + declaringClass + "." + name;
	}
}
