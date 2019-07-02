/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 05.04.2015
 * Author(s): AndreR
 * *********************************************************
 */
package edu.tigers.sumatra.botmanager.commands.tigerv3;

import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.botmanager.commands.ECommand;
import edu.tigers.sumatra.botmanager.serial.SerialData;
import edu.tigers.sumatra.botmanager.serial.SerialData.ESerialDataType;


/**
 * Send CRC32 checksum of specified data chunk to bootloader
 * 
 * @author AndreR
 */
public class TigerBootloaderCrc extends ACommand
{
	@SerialData(type = ESerialDataType.UINT8)
	private int		procId;
	
	@SerialData(type = ESerialDataType.UINT32)
	private long	startAddr;
	
	@SerialData(type = ESerialDataType.UINT32)
	private long	endAddr;
	
	@SerialData(type = ESerialDataType.UINT32)
	private long	crc;
	
	
	/**
	 * Constructor.
	 */
	public TigerBootloaderCrc()
	{
		super(ECommand.CMD_BOOTLOADER_CRC);
	}
	
	
	/**
	 * @return the procId
	 */
	public int getProcId()
	{
		return procId;
	}
	
	
	/**
	 * @param procId the procId to set
	 */
	public void setProcId(final int procId)
	{
		this.procId = procId;
	}
	
	
	/**
	 * @return the startAddr
	 */
	public long getStartAddr()
	{
		return startAddr;
	}
	
	
	/**
	 * @param startAddr the startAddr to set
	 */
	public void setStartAddr(final long startAddr)
	{
		this.startAddr = startAddr;
	}
	
	
	/**
	 * @return the endAddr
	 */
	public long getEndAddr()
	{
		return endAddr;
	}
	
	
	/**
	 * @param endAddr the endAddr to set
	 */
	public void setEndAddr(final long endAddr)
	{
		this.endAddr = endAddr;
	}
	
	
	/**
	 * @return the crc
	 */
	public long getCrc()
	{
		return crc;
	}
	
	
	/**
	 * @param crc the crc to set
	 */
	public void setCrc(final long crc)
	{
		this.crc = crc;
	}
	
}
