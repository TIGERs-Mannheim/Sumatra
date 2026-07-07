package edu.tigers.sumatra.botmanager.serial;

import edu.tigers.sumatra.botmanager.serial.SerialData.ESerialDataType;

import java.lang.reflect.Array;
import java.lang.reflect.Field;


/**
 * Tail command data field.
 *
 * @author AndreR
 */
public class SerialFieldTail extends ASerialField
{
	/**
	 * @param field Reflection Field
	 * @param offset byte array offset
	 */
	public SerialFieldTail(final Field field, final int offset)
	{
		super(field, ESerialDataType.TAIL, offset);
	}


	@Override
	public void decode(final byte[] data, final Object obj) throws SerialException
	{
		int length = data.length - offset;
		byte[] tail = new byte[length];

		System.arraycopy(data, offset, tail, 0, length);

		try
		{
			field.set(obj, tail);
		} catch (Exception err)
		{
			throw new SerialException("Could not set tail: " + field.getName(), err);
		}
	}


	@Override
	public void encode(final byte[] data, final Object obj) throws SerialException
	{
		byte[] tail;

		try
		{
			tail = (byte[]) field.get(obj);
		} catch (Exception err)
		{
			throw new SerialException("Could not set tail: " + field.getName(), err);
		}

		if (tail != null)
		{
			System.arraycopy(tail, 0, data, offset, tail.length);
		}
	}


	@Override
	public int getLength(final Object obj) throws SerialException
	{
		try
		{
			if (field.get(obj) == null)
			{
				return 0;
			}

			return Array.getLength(field.get(obj));
		} catch (Exception err)
		{
			throw new SerialException("Could not get tail " + field.getName() + " or it is not an array", err);
		}
	}
}
