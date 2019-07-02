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
 * Data chunk request from bootloader.
 * 
 * @author AndreR
 */
public class TigerBootloaderRequestData extends ACommand
{
	@SerialData(type = ESerialDataType.UINT8)
	private int		procId;
	
	@SerialData(type = ESerialDataType.UINT32)
	private long	offset;
	
	@SerialData(type = ESerialDataType.UINT32)
	private long	size;
	
	
	/**
	 */
	protected TigerBootloaderRequestData()
	{
		super(ECommand.CMD_BOOTLOADER_REQUEST_DATA);
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
	 * @return the offset
	 */
	public long getOffset()
	{
		return offset;
	}
	
	
	/**
	 * @param offset the offset to set
	 */
	public void setOffset(final long offset)
	{
		this.offset = offset;
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
	
}
