/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 24.10.2013
 * Author(s): AndreR
 * *********************************************************
 */
package edu.tigers.sumatra.botmanager.serial;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import edu.tigers.sumatra.botmanager.serial.SerialData.ESerialDataType;


/**
 * Parsed serial object description.
 * Decodes and encodes serial objects by using reflection.
 * 
 * @author AndreR
 */
public class SerialDescription
{
	private final List<ASerialField> cmdFields = new ArrayList<>();
	private final Class<?> clazz;
	private final Constructor<?> ctor;
	
	
	/**
	 * Create a serial description for encoding/decoding byte arrays.
	 * 
	 * @param clazz
	 * @throws SerialException
	 */
	public SerialDescription(final Class<?> clazz) throws SerialException
	{
		this.clazz = clazz;
		
		try
		{
			ctor = clazz.getDeclaredConstructor();
			ctor.setAccessible(true);
		} catch (Exception err)
		{
			throw new SerialException("Could not create instance of class: " + clazz.getName(), err);
		}
		
		
		loadFields(clazz);
	}
	
	
	private void loadFields(final Class<?> clazz) throws SerialException
	{
		Object instance = newInstance();
		List<Field> fields = getAllFields(clazz);
		int offset = 0;
		for (Field f : fields)
		{
			f.setAccessible(true);
			
			Optional<ASerialField> serialField = createSerialField(instance, offset, f);
			if (serialField.isPresent())
			{
				offset += serialField.get().getLength(instance);
				cmdFields.add(serialField.get());
				
				if (serialField.get().type == ESerialDataType.TAIL)
				{
					break;
				}
			}
		}
	}
	
	
	private List<Field> getAllFields(Class<?> clazz)
	{
		if (clazz.equals(Object.class))
		{
			return Collections.emptyList();
		}
		List<Field> fields = new ArrayList<>(Arrays.asList(clazz.getDeclaredFields()));
		fields.addAll(getAllFields(clazz.getSuperclass()));
		return fields;
	}
	
	
	private Optional<ASerialField> createSerialField(final Object instance, final int offset, final Field field)
			throws SerialException
	{
		SerialData annotation = field.getAnnotation(SerialData.class);
		if (annotation == null)
		{
			return Optional.empty();
		}
		
		if (field.getType().isArray())
		{
			return Optional.of(getSerialFieldForArray(offset, instance, field, annotation));
		}
		return Optional.of(new SerialFieldValue(field, annotation.type(), offset));
	}
	
	
	private ASerialField getSerialFieldForArray(final int offset, final Object instance, final Field field,
			final SerialData annotation) throws SerialException
	{
		if (annotation.type() == ESerialDataType.TAIL)
		{
			return new SerialFieldTail(field, offset);
		}
		
		int length = getArrayLength(instance, field);
		return new SerialFieldArray(field, annotation.type(), offset, length);
	}
	
	
	private int getArrayLength(final Object instance, final Field field) throws SerialException
	{
		try
		{
			if (field.get(instance) != null)
			{
				return Array.getLength(field.get(instance));
			}
			return 0;
		} catch (Exception err)
		{
			throw new SerialException("Could not get array length of serial field: " + field.getName(), err);
		}
	}
	
	
	/**
	 * Get the serialized length of this object.
	 * 
	 * @param obj
	 * @return
	 * @throws SerialException
	 */
	public int getLength(final Object obj) throws SerialException
	{
		int length = 0;
		
		for (ASerialField cmdField : cmdFields)
		{
			length += cmdField.getLength(obj);
		}
		
		return length;
	}
	
	
	/**
	 * Encode object on byte array.
	 * 
	 * @param obj
	 * @return
	 * @throws SerialException
	 */
	public byte[] encode(final Object obj) throws SerialException
	{
		int length = 0;
		
		for (ASerialField cmdField : cmdFields)
		{
			length += cmdField.getLength(obj);
		}
		
		byte[] data = new byte[length];
		
		for (ASerialField cmdField : cmdFields)
		{
			cmdField.encode(data, obj);
		}
		
		return data;
	}
	
	
	/**
	 * Decode an object from byte array.
	 * 
	 * @param data
	 * @return
	 * @throws SerialException
	 */
	public Object decode(final byte[] data) throws SerialException
	{
		Object obj = newInstance();
		
		int length = 0;
		
		for (ASerialField cmdField : cmdFields)
		{
			length += cmdField.getLength(obj);
		}
		
		if (data.length < length)
		{
			throw new SerialException(clazz.getName() + " object requires " + length + " bytes. Given are only: "
					+ data.length);
		}
		
		for (ASerialField cmdField : cmdFields)
		{
			cmdField.decode(data, obj);
		}
		
		return obj;
	}
	
	
	/**
	 * Create a new instance of this object.
	 * 
	 * @return
	 * @throws SerialException
	 */
	public Object newInstance() throws SerialException
	{
		try
		{
			return ctor.newInstance();
		} catch (Exception err)
		{
			throw new SerialException("Could not create new instance of: " + clazz.getName(), err);
		}
	}
}
