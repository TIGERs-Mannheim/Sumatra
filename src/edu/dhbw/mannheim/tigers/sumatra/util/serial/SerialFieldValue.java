/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 24.10.2013
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.util.serial;

import java.lang.reflect.Field;
import java.util.Arrays;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData.ESerialDataType;


/**
 * Single value command data field.
 * 
 * @author AndreR
 * 
 */
public class SerialFieldValue extends ASerialField
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private SerialDescription	embedded	= null;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 * @param field Reflection Field.
	 * @param type ESerialDataType
	 * @param offset byte array offset
	 * @throws SerialException
	 */
	public SerialFieldValue(Field field, ESerialDataType type, int offset) throws SerialException
	{
		super(field, type, offset);
		
		if (type == ESerialDataType.EMBEDDED)
		{
			embedded = new SerialDescription(field.getType());
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void decode(byte[] data, Object obj) throws SerialException
	{
		Object value = null;
		
		switch (type)
		{
			case UINT8:
				value = ACommand.byteArray2UByte(data, offset);
				break;
			case UINT16:
				value = ACommand.byteArray2UShort(data, offset);
				break;
			case UINT32:
				value = ACommand.byteArray2UInt(data, offset);
				break;
			case INT8:
				value = data[offset];
				break;
			case INT16:
				value = ACommand.byteArray2Short(data, offset);
				break;
			case INT32:
				value = ACommand.byteArray2Int(data, offset);
				break;
			case FLOAT16:
				value = ACommand.byteArray2HalfFloat(data, offset);
				break;
			case FLOAT32:
				value = ACommand.byteArray2Float(data, offset);
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
	
	
	@Override
	public void encode(byte[] data, Object obj) throws SerialException
	{
		try
		{
			switch (type)
			{
				case UINT8:
					ACommand.byte2ByteArray(data, offset, field.getInt(obj));
					break;
				case UINT16:
					ACommand.short2ByteArray(data, offset, field.getInt(obj));
					break;
				case UINT32:
					ACommand.int2ByteArray(data, offset, (int) field.getLong(obj));
					break;
				case INT8:
					ACommand.byte2ByteArray(data, offset, field.getInt(obj));
					break;
				case INT16:
					ACommand.short2ByteArray(data, offset, field.getInt(obj));
					break;
				case INT32:
					ACommand.int2ByteArray(data, offset, field.getInt(obj));
					break;
				case FLOAT16:
					ACommand.halfFloat2ByteArray(data, offset, field.getFloat(obj));
					break;
				case FLOAT32:
					ACommand.float2ByteArray(data, offset, field.getFloat(obj));
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
	public int getLength(Object obj) throws SerialException
	{
		if (type == ESerialDataType.EMBEDDED)
		{
			return embedded.getLength(obj);
		}
		
		return type.getLength();
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
