/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 04.06.2013
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ECommand;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData.ESerialDataType;


/**
 * Bootloader data :)
 * 
 * @author AndreR
 */
public class TigerBootloaderData extends ACommand
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	@SerialData(type = ESerialDataType.UINT8)
	private int		procId	= 0;
	@SerialData(type = ESerialDataType.UINT32)
	private long	offset	= 0;
	
	@SerialData(type = ESerialDataType.TAIL)
	private byte[]	payload;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public TigerBootloaderData()
	{
		super(ECommand.CMD_BOOTLOADER_DATA);
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
	public void setOffset(final long offset)
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
	 * @param payloadIn the payload to set
	 */
	public void setPayload(final byte[] payloadIn)
	{
		payload = new byte[payloadIn.length];
		
		System.arraycopy(payloadIn, 0, payload, 0, payloadIn.length);
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