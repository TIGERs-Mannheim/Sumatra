/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 04.03.2011
 * Author(s): AndreR
 * *********************************************************
 */
package edu.tigers.sumatra.botmanager.bots.communication;

import edu.tigers.sumatra.botmanager.commands.ACommand;


/**
 * Transceiver base interface.
 * 
 * @author AndreR
 */
public interface ITransceiver
{
	/**
	 * @param cmd
	 */
	void enqueueCommand(ACommand cmd);
	
	
	/**
	 * @return
	 */
	Statistics getReceiverStats();
	
	
	/**
	 * @return
	 */
	Statistics getTransmitterStats();
}
