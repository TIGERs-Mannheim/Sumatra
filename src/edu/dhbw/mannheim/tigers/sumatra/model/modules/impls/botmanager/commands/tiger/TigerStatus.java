/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 08.08.2010
 * Author(s):
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.CommandConstants;

/**
 * Dummy command for simulator.
 * Should be replaced by TigerSystemStatusMovement in Sim ASAP.
 * 
 * @deprecated Replaced by TigerSystemStatusMovement.
 * 
 * @author AndreR
 *
 */
public class TigerStatus extends ACommand
{
	public TigerStatus()
	{
	}

	@Override
	public void setData(byte[] data)
	{
	}

	@Override
	public byte[] getData()
	{
		return new byte[1];
	}
	
	@Override
	public int getCommand()
	{
		return CommandConstants.CMD_SYSTEM_STATUS;
	}
	
	@Override
	public int getDataLength()
	{
		return 0;
	}

}
