package edu.tigers.sumatra.botmanager.serial;

import edu.tigers.sumatra.botmanager.serial.SerialData.ESerialDataType;

import java.lang.reflect.Field;


/**
 * Serial data field base class.
 *
 * @author AndreR
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
	 * @param field
	 * @param type
	 * @param offset
	 */
	protected ASerialField(final Field field, final ESerialDataType type, final int offset)
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
