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
import java.util.List;

import org.apache.commons.lang.ArrayUtils;

import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.botmanager.serial.SerialData.ESerialDataType;


/**
 * Parsed serial object description.
 * Decodes and encodes serial objects by using reflection.
 * 
 * @author AndreR
 */
public class SerialDescription
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final List<ASerialField>	cmdFields	= new ArrayList<>();
	private final Class<?>				clazz;
	private final Constructor<?>		ctor;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Create a serial description for encoding/decoding byte arrays.
	 * 
	 * @param clazz
	 * @throws SerialException
	 */
	public SerialDescription(final Class<?> clazz) throws SerialException
	{
		this.clazz = clazz;
		
		int offset = 0;
		
		Object obj = null;
		
		try
		{
			ctor = clazz.getDeclaredConstructor();
			ctor.setAccessible(true);
			obj = ctor.newInstance();
		} catch (Exception err)
		{
			throw new SerialException("Could not create instance of class: " + clazz.getName(), err);
		}
		
		
		Field[] fields = clazz.getDeclaredFields();
		Class<?> superClass = clazz.getSuperclass();
		if (!superClass.equals(ACommand.class))
		{
			Field[] superFields = superClass.getDeclaredFields();
			fields = (Field[]) ArrayUtils.addAll(superFields, fields);
		}
		
		for (Field f : fields)
		{
			SerialData annotation = f.getAnnotation(SerialData.class);
			if (annotation == null)
			{
				continue;
			}
			
			f.setAccessible(true);
			Class<?> type = f.getType();
			
			if (type.isArray())
			{
				int length = 0;
				try
				{
					if (f.get(obj) != null)
					{
						length = Array.getLength(f.get(obj));
					}
				} catch (Exception err)
				{
					throw new SerialException("Could not get array length of serial field: " + f.getName(), err);
				}
				
				if (annotation.type() == ESerialDataType.TAIL)
				{
					cmdFields.add(new SerialFieldTail(f, offset));
					break;
				}
				
				ASerialField serialField = new SerialFieldArray(f, annotation.type(), offset, length);
				cmdFields.add(serialField);
				
				offset += serialField.getLength(obj);
				
			} else
			{
				ASerialField serialField = new SerialFieldValue(f, annotation.type(), offset);
				cmdFields.add(serialField);
				
				offset += serialField.getLength(obj);
			}
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
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
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
