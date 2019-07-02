/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 17.05.2015
 * Author(s): AndreR
 * *********************************************************
 */
package edu.tigers.sumatra.botmanager.commands.tigerv3;

import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.botmanager.commands.ECommand;


/**
 * Query a list of all bot config files.
 * 
 * @author AndreR
 */
public class TigerConfigQueryFileList extends ACommand
{
	/** Constructor. */
	public TigerConfigQueryFileList()
	{
		super(ECommand.CMD_CONFIG_QUERY_FILE_LIST, true);
	}
}
