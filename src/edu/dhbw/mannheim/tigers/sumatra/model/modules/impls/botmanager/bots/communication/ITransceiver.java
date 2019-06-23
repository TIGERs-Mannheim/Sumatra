/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 04.03.2011
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;


/**
 * Transceiver base interface.
 * 
 * @author AndreR
 * 
 */
public interface ITransceiver
{
	/**
	 * 
	 * @param cmd
	 */
	void enqueueCommand(ACommand cmd);
	
	
	/**
	 * 
	 * @return
	 */
	Statistics getReceiverStats();
	
	
	/**
	 * 
	 * @return
	 */
	Statistics getTransmitterStats();
}
