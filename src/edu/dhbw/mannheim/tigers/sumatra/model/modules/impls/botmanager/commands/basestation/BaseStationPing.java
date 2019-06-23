/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 09.06.2013
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.basestation;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ECommand;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData.ESerialDataType;


/**
 * Base Station ping with varying data payload.
 * 
 * @author AndreR
 * 
 */
public class BaseStationPing extends ACommand
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	@SerialData(type = ESerialDataType.UINT32)
	private long	id			= 0;
	@SerialData(type = ESerialDataType.TAIL)
	private byte[]	payload	= null;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/** */
	public BaseStationPing()
	{
		super(ECommand.CMD_BASE_PING);
	}
	
	
	/**
	 * 
	 * @param id
	 * @param payloadLength
	 */
	public BaseStationPing(long id, int payloadLength)
	{
		super(ECommand.CMD_BASE_PING);
		
		this.id = id;
		setPayloadLength(payloadLength);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return the id
	 */
	public long getId()
	{
		return id;
	}
	
	
	/**
	 * @param id the id to set
	 */
	public void setId(long id)
	{
		this.id = id;
	}
	
	
	/**
	 * @return the payloadLength
	 */
	public int getPayloadLength()
	{
		if (payload == null)
		{
			return 0;
		}
		
		return payload.length;
	}
	
	
	/**
	 * @param payloadLength the payloadLength to set
	 */
	public void setPayloadLength(int payloadLength)
	{
		payload = new byte[payloadLength];
	}
}
