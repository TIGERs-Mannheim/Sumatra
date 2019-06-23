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
	void enqueueCommand(ACommand cmd);

	Statistics	getReceiverStats();
	Statistics	getTransmitterStats();
}
