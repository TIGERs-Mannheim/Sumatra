/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 08.08.2010
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.ITransceiver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;


/**
 * {@link ABot}-implementation that simply prints everything he is told to do to log
 * 
 * @author Gero
 * 
 */
public class SysoutBot extends ABot
{
	private final Logger	log	= Logger.getLogger(getClass());
	
	public SysoutBot(SubnodeConfiguration config)
	{
		super(config);
	}
	
	public SysoutBot(int id)
	{
		super(EBotType.SYSOUT, id);
	}
	
	@Override
	public void execute(ACommand cmd)
	{
		log.debug("Sysbot: " + cmd.toString());
	}

	@Override
	public void start()
	{
	}

	@Override
	public void stop()
	{
	}

	@Override
	public ITransceiver getTransceiver()
	{
		return null;
	}
}
