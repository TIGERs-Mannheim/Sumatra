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

import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData.ESerialDataType;


/**
 * Serial data field base class.
 * 
 * @author AndreR
 * 
 */
public abstract class ASerialField
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	protected final Field				field;
	protected final ESerialDataType	type;
	protected final int					offset;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 * @param field
	 * @param type
	 * @param offset
	 */
	protected ASerialField(Field field, ESerialDataType type, int offset)
	{
		this.field = field;
		this.type = type;
		this.offset = offset;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Decode an Object from a byte array.
	 * 
	 * @param data
	 * @param obj
	 * @throws SerialException
	 */
	public abstract void decode(byte[] data, Object obj) throws SerialException;
	
	
	/**
	 * Encode an Object in a byteArray.
	 * 
	 * @param data
	 * @param obj
	 * @throws SerialException
	 */
	public abstract void encode(byte[] data, Object obj) throws SerialException;
	
	
	/**
	 * Get length of byte array for encoding given an object.
	 * 
	 * @param obj
	 * @return
	 * @throws SerialException
	 */
	public abstract int getLength(Object obj) throws SerialException;
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
