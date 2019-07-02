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
 * Send program file size to bootloader.
 * 
 * @author AndreR
 */
public class TigerBootloaderSize extends ACommand
{
	@SerialData(type = ESerialDataType.UINT8)
	private int		procId;
	
	@SerialData(type = ESerialDataType.UINT32)
	private long	size;
	
	
	/** Constructor. */
	public TigerBootloaderSize()
	{
		super(ECommand.CMD_BOOTLOADER_SIZE);
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
	 * @return the size
	 */
	public long getSize()
	{
		return size;
	}
	
	
	/**
	 * @param size the size to set
	 */
	public void setSize(final long size)
	{
		this.size = size;
	}
	
	
	/** Set size to maximum. */
	public void setInvalidSize()
	{
		size = 0xFFFFFFFFL;
	}
}
