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
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.CommandConstants;


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
	
	private long				offset					= 0;
	private byte[]				payload					= new byte[BOOTLOADER_DATA_SIZE];
	private long				crc						= 0;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public TigerBootloaderData()
	{
	}
	
	
	/**
	 * 
	 * @param srcData
	 * @param dstOffset
	 * @param length
	 */
	public TigerBootloaderData(byte[] srcData, int length, int dstOffset)
	{
		if (length > BOOTLOADER_DATA_SIZE)
		{
			length = BOOTLOADER_DATA_SIZE;
		}
		System.arraycopy(srcData, 0, payload, 0, length);
		
		offset = dstOffset;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void setData(byte[] data)
	{
		offset = byteArray2UInt(data, 0);
		System.arraycopy(data, 4, payload, 0, BOOTLOADER_DATA_SIZE);
		crc = byteArray2UInt(data, BOOTLOADER_DATA_SIZE + 4);
	}
	
	
	@Override
	public byte[] getData()
	{
		final byte data[] = new byte[getDataLength()];
		
		CRC32 crc32 = new CRC32();
		
		crc32.reset();
		crc32.update(payload);
		crc = crc32.getValue();
		
		int2ByteArray(data, 0, (int) offset);
		System.arraycopy(payload, 0, data, 4, BOOTLOADER_DATA_SIZE);
		int2ByteArray(data, BOOTLOADER_DATA_SIZE + 4, (int) crc);
		
		return data;
	}
	
	
	@Override
	public int getCommand()
	{
		return CommandConstants.CMD_BOOTLOADER_DATA;
	}
	
	
	@Override
	public int getDataLength()
	{
		return BOOTLOADER_DATA_SIZE + 8;
	}
	
	
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
}