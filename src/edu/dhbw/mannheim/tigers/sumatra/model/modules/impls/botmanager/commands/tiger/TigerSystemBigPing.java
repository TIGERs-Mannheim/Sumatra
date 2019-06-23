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
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.CommandConstants;


/**
 * Ping!
 * 
 * @author AndreR
 * 
 */
public class TigerSystemBigPing extends ACommand
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/** ping identification (for roundtrip measurements) */
	private int	id;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 */
	public TigerSystemBigPing()
	{
		id = 0;
	}
	
	
	/**
	 * 
	 * @param id
	 */
	public TigerSystemBigPing(int id)
	{
		this.id = id;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void setData(byte[] data)
	{
		id = byteArray2Int(data, 0);
	}
	
	
	@Override
	public byte[] getData()
	{
		final byte data[] = new byte[getDataLength()];
		
		int2ByteArray(data, 0, id);
		
		return data;
	}
	
	
	@Override
	public int getCommand()
	{
		return CommandConstants.CMD_SYSTEM_BIG_PING;
	}
	
	
	@Override
	public int getDataLength()
	{
		return 200;
	}
	
	
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
