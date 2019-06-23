/*
 * *********************************************************
 * I * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 05.04.2015
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv3;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ECommand;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData.ESerialDataType;


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
