/* 
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 03.03.2011
 * Author(s): AndreR
 *
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.CommandConstants;

/**
 * System announcement from a bot.
 * Usually issued after a bot is switched on. Kind of notification for the
 * botmanager to send a SYSTEM_SET_IDENTITY command.
 * 
 * @author AndreR
 * 
 */
public class TigerSystemAnnouncement extends ACommand
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final Logger LOG = Logger.getLogger(TigerSystemAnnouncement.class);
	
	private int cpuId[] = new int[3];
	

	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public TigerSystemAnnouncement()
	{
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Set CPU ID.
	 * Expected format is a 24 digit HEX number.
	 * 
	 * @param id CPU ID
	 */
	public void setCpuId(String id)
	{
		if(id.length() != 24)
		{
			LOG.error("Invalid CPU ID: " + id);
			return;
		}
		
		try
		{
			cpuId[2] = Integer.parseInt(id.substring(0, 8), 16);
			cpuId[1] = Integer.parseInt(id.substring(8, 16), 16);
			cpuId[0] = Integer.parseInt(id.substring(16, 24), 16);
		}
		
		catch(NumberFormatException e)
		{
			LOG.error("Invalid CPU ID: " + id);
		}
	}
	
	public String getCpuId()
	{
		String id;
		
		id = String.format("%08X", cpuId[2]);
		id += String.format("%08X", cpuId[1]);
		id += String.format("%08X", cpuId[0]);
		
		return id;
	}

	@Override
	public void setData(byte[] data)
	{
		cpuId[0] = byteArray2Int(data, 0);
		cpuId[1] = byteArray2Int(data, 4);
		cpuId[2] = byteArray2Int(data, 8);
	}

	@Override
	public byte[] getData()
	{
		byte data[] = new byte[getDataLength()];
		
		int2ByteArray(data, 0, cpuId[0]);
		int2ByteArray(data, 4, cpuId[1]);
		int2ByteArray(data, 8, cpuId[2]);

		return data;
	}
	
	@Override
	public int getCommand()
	{
		return CommandConstants.CMD_SYSTEM_ANNOUNCEMENT;
	}
	
	@Override
	public int getDataLength()
	{
		return 12;
	}
}
