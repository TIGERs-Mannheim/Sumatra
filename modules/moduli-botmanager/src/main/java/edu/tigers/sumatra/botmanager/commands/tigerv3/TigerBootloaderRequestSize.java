/*
 * *********************************************************
 * I * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
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
 * File size request from bootloader.
 * 
 * @author AndreR
 */
public class TigerBootloaderRequestSize extends ACommand
{
	@SerialData(type = ESerialDataType.UINT8)
	private int	procId;
	
	
	/** */
	protected TigerBootloaderRequestSize()
	{
		super(ECommand.CMD_BOOTLOADER_REQUEST_SIZE);
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
	
}
