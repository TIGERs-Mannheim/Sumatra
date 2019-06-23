/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 04.06.2013
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2;

import java.util.zip.CRC32;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ECommand;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData.ESerialDataType;


/**
 * Bootloader data :)
 * 
 * @author AndreR
 * 
 */
public class TigerBootloaderData extends ACommand
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/** */
	public static final int	BOOTLOADER_DATA_SIZE	= 256;
	
	@SerialData(type = ESerialDataType.UINT32)
	private long				offset					= 0;
	@SerialData(type = ESerialDataType.INT8)
	private byte[]				payload					= new byte[BOOTLOADER_DATA_SIZE];
	@SerialData(type = ESerialDataType.UINT32)
	private long				crc						= 0;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public TigerBootloaderData()
	{
		super(ECommand.CMD_BOOTLOADER_DATA);
	}
	
	
	/**
	 * 
	 * @param srcData
	 * @param dstOffset
	 * @param length
	 */
	public TigerBootloaderData(byte[] srcData, int length, int dstOffset)
	{
		super(ECommand.CMD_BOOTLOADER_DATA);
		
		if (length > BOOTLOADER_DATA_SIZE)
		{
			length = BOOTLOADER_DATA_SIZE;
		}
		System.arraycopy(srcData, 0, payload, 0, length);
		
		CRC32 crc32 = new CRC32();
		
		crc32.reset();
		crc32.update(payload);
		crc = crc32.getValue();
		
		offset = dstOffset;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @return the offset
	 */
	public long getOffset()
	{
		return offset;
	}
	
	
	/**
	 * @param offset the offset to set
	 */
	public void setOffset(long offset)
	{
		this.offset = offset;
	}
	
	
	/**
	 * @return the payload
	 */
	public byte[] getPayload()
	{
		return payload;
	}
	
	
	/**
	 * @param payload the payload to set
	 */
	public void setPayload(byte[] payload)
	{
		CRC32 crc32 = new CRC32();
		
		crc32.reset();
		crc32.update(payload);
		crc = crc32.getValue();
		
		this.payload = payload;
	}
}