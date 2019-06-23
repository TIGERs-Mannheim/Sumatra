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
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.CommandConstants;


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
	private long	id					= 0;
	private int		payloadLength	= 0;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/** */
	public BaseStationPing()
	{
	}
	
	
	/**
	 * 
	 * @param id
	 * @param payloadLength
	 */
	public BaseStationPing(long id, int payloadLength)
	{
		this.id = id;
		this.payloadLength = payloadLength;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void setData(byte[] data)
	{
		id = byteArray2UInt(data, 0);
		payloadLength = data.length - 4;
	}
	
	
	@Override
	public byte[] getData()
	{
		final byte data[] = new byte[getDataLength()];
		
		int2ByteArray(data, 0, (int) id);
		
		return data;
	}
	
	
	@Override
	public int getCommand()
	{
		return CommandConstants.CMD_BASE_PING;
	}
	
	
	@Override
	public int getDataLength()
	{
		return payloadLength + 4;
	}
	
	
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
		return payloadLength;
	}
	
	
	/**
	 * @param payloadLength the payloadLength to set
	 */
	public void setPayloadLength(int payloadLength)
	{
		this.payloadLength = payloadLength;
	}
}
