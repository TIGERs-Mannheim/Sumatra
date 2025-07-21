/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.persistence.serializer;


import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * Serializer for arbitrary object type T.
 * The serializer uses unsafe methods to circumvent module reflection restrictions.
 */
@Log4j2
public class ObjectSerializer<T> extends Serializer<T>
{
	private static final int IGNORED_MODIFIERS = Modifier.STATIC | Modifier.TRANSIENT;


	private CompoundField<?>[] fields;


	private static void gatherCompoundFields(
			GenericSerializer genericSerializer, Class<?> type, List<CompoundField<?>> serializers, boolean unsafe
	)
	{
		if (type == Object.class)
		{
			return;
		}

		for (Field field : type.getDeclaredFields())
		{
			if ((field.getModifiers() & IGNORED_MODIFIERS) != 0)
			{
				continue;
			}

			serializers.add(new CompoundField<>(genericSerializer, field, unsafe));
		}

		gatherCompoundFields(genericSerializer, type.getSuperclass(), serializers, unsafe);
	}


	@SuppressWarnings("rawtypes")
	public static CompoundField[] getFields(GenericSerializer genericSerializer, Class<?> type, boolean unsafe)
	{
		List<CompoundField<?>> compoundFields = new ArrayList<>();
		gatherCompoundFields(genericSerializer, type, compoundFields, unsafe);
		return compoundFields.toArray(CompoundField[]::new);
	}


	public ObjectSerializer(GenericSerializer genericSerializer, Class<T> type)
	{
		super(genericSerializer, type);
		fields = getFields(genericSerializer, type, true);
	}


	@Override
	public void serialize(MappedDataOutputStream stream, T object) throws IOException
	{
		for (CompoundField<?> field : fields)
		{
			field.serializeUnsafe(stream, object);
		}
	}


	@Override
	public T deserialize(ByteBuffer buffer) throws IOException
	{
		T instance = allocate();

		for (CompoundField<?> field : fields)
		{
			field.deserializeUnsafe(buffer, instance);
		}

		return instance;
	}


	@Override
	public void transientInit(GenericSerializer genericSerializer)
	{
		super.transientInit(genericSerializer);

		for (CompoundField<?> field : fields)
		{
			field.initDeserializer(genericSerializer, true);
		}

		Set<String> fieldNames = Arrays.stream(fields).map(CompoundField::getName).collect(Collectors.toSet());
		for (CompoundField<?> field : getFields(genericSerializer, getType(), true))
		{
			if (!fieldNames.contains(field.getName()))
			{
				log.warn("Field {} did not exist during serialization. Expect issues.", field);
			}
		}
	}
}
