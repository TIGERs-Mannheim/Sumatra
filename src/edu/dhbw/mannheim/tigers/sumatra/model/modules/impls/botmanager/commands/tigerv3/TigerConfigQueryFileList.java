/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 17.05.2015
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv3;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ECommand;


/**
 * Query a list of all bot config files.
 * 
 * @author AndreR
 */
public class TigerConfigQueryFileList extends ACommand
{
	/** */
	public TigerConfigQueryFileList()
	{
		super(ECommand.CMD_CONFIG_QUERY_FILE_LIST, true);
	}
}
