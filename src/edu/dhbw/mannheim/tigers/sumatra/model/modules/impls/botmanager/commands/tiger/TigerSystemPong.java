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
 * Pong!
 * 
 * @author AndreR
 * 
 */
public class TigerSystemPong extends ACommand
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private int id;	// ping identification (for roundtrip measurements)
	

	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public TigerSystemPong()
	{
		id = 0;
	}

	public TigerSystemPong(int id)
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
		byte data[] = new byte[getDataLength()];
		
		int2ByteArray(data, 0, id);
		
		return data;
	}
	
	@Override
	public int getCommand()
	{
		return CommandConstants.CMD_SYSTEM_PONG;
	}
	
	@Override
	public int getDataLength()
	{
		return 4;
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
