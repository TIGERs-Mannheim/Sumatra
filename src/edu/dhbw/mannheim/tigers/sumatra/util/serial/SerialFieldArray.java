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

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Arrays;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData.ESerialDataType;


/**
 * Array serial data field.
 * 
 * @author AndreR
 * 
 */
public class SerialFieldArray extends ASerialField
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final int				length;
	private SerialDescription	embedded	= null;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 * @param field Reflection Field
	 * @param type ESerialDataType
	 * @param offset byte array offset
	 * @param length array length
	 * @throws SerialException
	 */
	public SerialFieldArray(Field field, ESerialDataType type, int offset, int length) throws SerialException
	{
		super(field, type, offset);
		
		this.length = length;
		
		if (type == ESerialDataType.EMBEDDED)
		{
			embedded = new SerialDescription(field.getType().getComponentType());
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void decode(byte[] data, Object obj) throws SerialException
	{
		Object value = null;
		
		int localOffset = offset;
		Object array = null;
		
		try
		{
			array = field.get(obj);
		} catch (Exception err)
		{
			throw new SerialException("Could not get array Field: " + field.getName(), err);
		}
		
		for (int i = 0; i < length; i++)
		{
			switch (type)
			{
				case UINT8:
					value = ACommand.byteArray2UByte(data, localOffset);
					break;
				case UINT16:
					value = ACommand.byteArray2UShort(data, localOffset);
					break;
				case UINT32:
					value = ACommand.byteArray2UInt(data, localOffset);
					break;
				case INT8:
					value = data[localOffset];
					break;
				case INT16:
					value = ACommand.byteArray2Short(data, localOffset);
					break;
				case INT32:
					value = ACommand.byteArray2Int(data, localOffset);
					break;
				case FLOAT16:
					value = ACommand.byteArray2HalfFloat(data, localOffset);
					break;
				case FLOAT32:
					value = ACommand.byteArray2Float(data, localOffset);
					break;
				case EMBEDDED:
					value = embedded.decode(Arrays.copyOfRange(data, localOffset, data.length));
					break;
				default:
					throw new SerialException("Invalid call to decode for type: " + type);
			}
			
			if (type == ESerialDataType.EMBEDDED)
			{
				localOffset += embedded.getLength(value);
			} else
			{
				localOffset += type.getLength();
			}
			
			try
			{
				Array.set(array, i, value);
			} catch (Exception err)
			{
				throw new SerialException("Could not set array on field: " + field.getName(), err);
			}
		}
	}
	
	
	@Override
	public void encode(byte[] data, Object obj) throws SerialException
	{
		byte[] embeddedData = new byte[1];
		int localOffset = offset;
		Object array = null;
		
		try
		{
			array = field.get(obj);
		} catch (Exception err)
		{
			throw new SerialException("Could not get array Field: " + field.getName(), err);
		}
		
		try
		{
			for (int i = 0; i < length; i++)
			{
				switch (type)
				{
					case UINT8:
						ACommand.byte2ByteArray(data, localOffset, Array.getInt(array, i));
						break;
					case UINT16:
						ACommand.short2ByteArray(data, localOffset, Array.getInt(array, i));
						break;
					case UINT32:
						ACommand.int2ByteArray(data, localOffset, (int) Array.getLong(array, i));
						break;
					case INT8:
						ACommand.byte2ByteArray(data, localOffset, Array.getInt(array, i));
						break;
					case INT16:
						ACommand.short2ByteArray(data, localOffset, Array.getInt(array, i));
						break;
					case INT32:
						ACommand.int2ByteArray(data, localOffset, Array.getInt(array, i));
						break;
					case FLOAT16:
						ACommand.halfFloat2ByteArray(data, localOffset, Array.getFloat(array, i));
						break;
					case FLOAT32:
						ACommand.float2ByteArray(data, localOffset, Array.getFloat(array, i));
						break;
					case EMBEDDED:
						embeddedData = embedded.encode(Array.get(array, i));
						System.arraycopy(embeddedData, 0, data, localOffset, embeddedData.length);
						break;
					default:
						throw new IllegalArgumentException("Invalid call to encode for type: " + type);
				}
				
				if (type == ESerialDataType.EMBEDDED)
				{
					localOffset += embeddedData.length;
				} else
				{
					localOffset += type.getLength();
				}
			}
		} catch (Exception err)
		{
			throw new SerialException("Could not get array field on: " + field.getName(), err);
		}
	}
	
	
	@Override
	public int getLength(Object obj) throws SerialException
	{
		if (type == ESerialDataType.EMBEDDED)
		{
			return embedded.getLength(obj) * length;
		}
		
		return type.getLength() * length;
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
