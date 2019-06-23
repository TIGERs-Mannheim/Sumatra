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
import java.lang.reflect.Field;

import edu.tigers.sumatra.botmanager.serial.SerialData.ESerialDataType;


/**
 * Tail command data field.
 * 
 * @author AndreR
 */
public class SerialFieldTail extends ASerialField
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param field Reflection Field
	 * @param offset byte array offset
	 */
	public SerialFieldTail(final Field field, final int offset)
	{
		super(field, ESerialDataType.TAIL, offset);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
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
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
