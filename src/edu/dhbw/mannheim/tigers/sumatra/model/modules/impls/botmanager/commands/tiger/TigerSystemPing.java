/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 31.03.2011
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ECommand;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData.ESerialDataType;


/**
 * Ping!
 * 
 * @author AndreR
 * 
 */
public class TigerSystemPing extends ACommand
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/** ping identification (for roundtrip measurements) */
	@SerialData(type = ESerialDataType.INT32)
	private int		id;
	@SerialData(type = ESerialDataType.TAIL)
	private byte[]	payload;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * 
	 */
	public TigerSystemPing()
	{
		super(ECommand.CMD_SYSTEM_PING);
		
		id = 0;
	}
	
	
	/**
	 * 
	 * @param id
	 */
	public TigerSystemPing(int id)
	{
		super(ECommand.CMD_SYSTEM_PING);
		
		this.id = id;
	}
	
	
	/**
	 * Send ping with random payload of specified size
	 * 
	 * @param id
	 * @param payloadSize
	 */
	public TigerSystemPing(int id, int payloadSize)
	{
		super(ECommand.CMD_SYSTEM_PING);
		
		this.id = id;
		
		payload = new byte[payloadSize];
		for (int i = 0; i < payloadSize; i++)
		{
			byte2ByteArray(payload, i, i == 0 ? 1 : i);
			// byte2ByteArray(payload, i, (int) ((Math.random() * 254.0) + 1.0));
		}
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
	public int getId()
	{
		return id;
	}
	
	
	/**
	 * @param id the id to set
	 */
	public void setId(int id)
	{
		this.id = id;
	}
}
