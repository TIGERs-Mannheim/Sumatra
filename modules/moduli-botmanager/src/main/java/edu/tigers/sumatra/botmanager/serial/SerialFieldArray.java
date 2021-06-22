/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botmanager.serial;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.tigers.sumatra.botmanager.serial.SerialData.ESerialDataType;


/**
 * Array serial data field.
 *
 * @author AndreR
 */
public class SerialFieldArray extends ASerialField
{
	private static final Logger log = LogManager.getLogger(SerialFieldArray.class.getName());
	private final int length;
	private final SerialDescription embedded;


	/**
	 * @param field Reflection Field
	 * @param type ESerialDataType
	 * @param offset byte array offset
	 * @param length array length
	 * @throws SerialException
	 */
	public SerialFieldArray(final Field field, final ESerialDataType type, final int offset, final int length)
			throws SerialException
	{
		super(field, type, offset);

		this.length = length;

		if (type == ESerialDataType.EMBEDDED)
		{
			embedded = new SerialDescription(field.getType().getComponentType());
		} else
		{
			embedded = null;
		}
	}


	@Override
	public void decode(final byte[] data, final Object obj) throws SerialException
	{
		Object value;

		int localOffset = offset;
		Object array;

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
					value = SerialByteConverter.byteArray2UByte(data, localOffset);
					break;
				case UINT16:
					value = SerialByteConverter.byteArray2UShort(data, localOffset);
					break;
				case UINT32:
					value = SerialByteConverter.byteArray2UInt(data, localOffset);
					break;
				case INT8:
					value = data[localOffset];
					break;
				case INT16:
					value = SerialByteConverter.byteArray2Short(data, localOffset);
					break;
				case INT32:
					value = SerialByteConverter.byteArray2Int(data, localOffset);
					break;
				case FLOAT16:
					value = SerialByteConverter.byteArray2HalfFloat(data, localOffset);
					break;
				case FLOAT32:
					value = SerialByteConverter.byteArray2Float(data, localOffset);
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


	private void validateRange(final Object array, final int pos)
	{
		switch (type)
		{
			case UINT8:
			case UINT16:
			case UINT32:
			case INT8:
			case INT16:
			case INT32:
				validateInt(array, pos);
				break;
			default:
				break;
		}
	}


	private void validateInt(final Object array, final int pos)
	{
		long value = Array.getLong(array, pos);
		if ((value < type.getMin()) || (value > type.getMax()))
		{
			log.warn(type + " " + field.getDeclaringClass().getSimpleName() + "::" + field.getName() + "[" + pos
					+ "]" + " value is out of bounds (" + value + ")", new IllegalArgumentException());
		}
	}


	@Override
	public void encode(final byte[] data, final Object obj) throws SerialException
	{
		byte[] embeddedData = new byte[1];
		int localOffset = offset;
		Object array;

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
				validateRange(array, i);

				switch (type)
				{
					case INT8:
					case UINT8:
						SerialByteConverter.byte2ByteArray(data, localOffset, Array.getInt(array, i));
						break;
					case INT16:
					case UINT16:
						SerialByteConverter.short2ByteArray(data, localOffset, Array.getInt(array, i));
						break;
					case UINT32:
						SerialByteConverter.int2ByteArray(data, localOffset, (int) Array.getLong(array, i));
						break;
					case INT32:
						SerialByteConverter.int2ByteArray(data, localOffset, Array.getInt(array, i));
						break;
					case FLOAT16:
						SerialByteConverter.halfFloat2ByteArray(data, localOffset, Array.getDouble(array, i));
						break;
					case FLOAT32:
						SerialByteConverter.float2ByteArray(data, localOffset, Array.getDouble(array, i));
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
	public int getLength(final Object obj) throws SerialException
	{
		if (type == ESerialDataType.EMBEDDED)
		{
			return embedded.getLength(obj) * length;
		}

		return type.getLength() * length;
	}
}
