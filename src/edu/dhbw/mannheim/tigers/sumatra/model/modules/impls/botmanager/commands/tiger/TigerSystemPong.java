/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 31.03.2011
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger;

import java.util.Arrays;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ECommand;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData.ESerialDataType;


/**
 * Pong!
 *
 * @author AndreR
 */
public class TigerSystemPong extends ACommand
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// ping identification (for roundtrip measurements)
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
	public TigerSystemPong()
	{
		super(ECommand.CMD_SYSTEM_PONG);
		
		id = 0;
	}
	
	
	/**
	 * @param id
	 */
	public TigerSystemPong(final int id)
	{
		super(ECommand.CMD_SYSTEM_PONG);
		
		this.id = id;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @return
	 */
	public boolean payloadValid()
	{
		if (payload == null)
		{
			return true;
		}
		byte[] sPayload = new byte[payload.length];
		for (int i = 0; i < payload.length; i++)
		{
			byte2ByteArray(sPayload, i, i == 0 ? 1 : i);
		}
		for (int i = 0; i < payload.length; i++)
		{
			if (payload[i] != sPayload[i])
			{
				return false;
			}
		}
		return true;
	}
	
	
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
	public void setId(final int id)
	{
		this.id = id;
	}
	
	
	/**
	 * @return the payload
	 */
	public final byte[] getPayload()
	{
		return Arrays.copyOf(payload, payload.length);
	}
}
