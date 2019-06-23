/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 01.03.2011
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.udp;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;


/**
 * Observer for UDP receiver.
 * 
 * @author AndreR
 * 
 */
public interface IReceiverUDPObserver
{
	/**
	 * 
	 * @param cmd
	 */
	void onNewCommand(ACommand cmd);
}
