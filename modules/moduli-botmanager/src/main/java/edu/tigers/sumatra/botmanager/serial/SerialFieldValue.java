/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 24.10.2013
 * Author(s): AndreR
 * *********************************************************
 */
package edu.tigers.sumatra.botmanager.serial;

import java.lang.reflect.Field;
import java.util.Arrays;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.botmanager.serial.SerialData.ESerialDataType;


/**
 * Single value command data field.
 * 
 * @author AndreR
 */
public class SerialFieldValue extends ASerialField
{
	private static final Logger log = Logger.getLogger(SerialFieldValue.class.getName());
	private final SerialDescription embedded;
	
	
	/**
	 * @param field Reflection Field.
	 * @param type ESerialDataType
	 * @param offset byte array offset
	 * @throws SerialException
	 */
	public SerialFieldValue(final Field field, final ESerialDataType type, final int offset) throws SerialException
	{
		super(field, type, offset);
		
		if (type == ESerialDataType.EMBEDDED)
		{
			embedded = new SerialDescription(field.getType());
		} else
		{
			embedded = null;
		}
	}
	
	
	@Override
	public void decode(final byte[] data, final Object obj) throws SerialException
	{
		Object value;
		
		switch (type)
		{
			case UINT8:
				value = SerialByteConverter.byteArray2UByte(data, offset);
				break;
			case UINT16:
				value = SerialByteConverter.byteArray2UShort(data, offset);
				break;
			case UINT32:
				value = SerialByteConverter.byteArray2UInt(data, offset);
				break;
			case INT8:
				value = data[offset];
				break;
			case INT16:
				value = SerialByteConverter.byteArray2Short(data, offset);
				break;
			case INT32:
				value = SerialByteConverter.byteArray2Int(data, offset);
				break;
			case FLOAT16:
				value = SerialByteConverter.byteArray2HalfFloat(data, offset);
				break;
			case FLOAT32:
				value = SerialByteConverter.byteArray2Float(data, offset);
				break;
			case EMBEDDED:
				value = embedded.decode(Arrays.copyOfRange(data, offset, data.length));
				break;
			default:
				throw new IllegalArgumentException("Invalid call to decode for type: " + type);
		}
		
		try
		{
			field.set(obj, value);
		} catch (Exception err)
		{
			throw new SerialException("Could not set field: " + field.getName(), err);
		}
	}
	
	
	private void validateRange(final Object obj) throws IllegalAccessException
	{
		switch (type)
		{
			case UINT8:
			case UINT16:
			case UINT32:
			case INT8:
			case INT16:
			case INT32:
				validateInt(obj);
				break;
			default:
				break;
		}
	}
	
	
	private void validateInt(final Object obj) throws IllegalAccessException
	{
		long value = field.getLong(obj);
		if ((value < type.getMin()) || (value > type.getMax()))
		{
			log.warn(type + " " + field.getDeclaringClass().getSimpleName() + "::" + field.getName()
					+ " value is out of bounds (" + value + ")");
		}
	}
	
	
	@Override
	public void encode(final byte[] data, final Object obj) throws SerialException
	{
		try
		{
			validateRange(obj);
			
			switch (type)
			{
				case INT8:
				case UINT8:
					SerialByteConverter.byte2ByteArray(data, offset, field.getInt(obj));
					break;
				case INT16:
				case UINT16:
					SerialByteConverter.short2ByteArray(data, offset, field.getInt(obj));
					break;
				case UINT32:
					SerialByteConverter.int2ByteArray(data, offset, (int) field.getLong(obj));
					break;
				case INT32:
					SerialByteConverter.int2ByteArray(data, offset, field.getInt(obj));
					break;
				case FLOAT16:
					SerialByteConverter.halfFloat2ByteArray(data, offset, (float) field.getDouble(obj));
					break;
				case FLOAT32:
					SerialByteConverter.float2ByteArray(data, offset, (float) field.getDouble(obj));
					break;
				case EMBEDDED:
					byte[] embeddedData = embedded.encode(field.get(obj));
					System.arraycopy(embeddedData, 0, data, offset, embeddedData.length);
					break;
				default:
					throw new IllegalArgumentException("Invalid call to encode for type: " + type);
			}
		}
		
		catch (Exception err)
		{
			throw new SerialException("Could not get field: " + field.getName(), err);
		}
	}
	
	
	@Override
	public int getLength(final Object obj) throws SerialException
	{
		if (type == ESerialDataType.EMBEDDED)
		{
			return embedded.getLength(obj);
		}
		
		return type.getLength();
	}
}
