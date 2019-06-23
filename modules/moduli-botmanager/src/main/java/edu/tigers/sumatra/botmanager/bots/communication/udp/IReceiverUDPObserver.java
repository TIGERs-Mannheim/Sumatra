/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 01.03.2011
 * Author(s): AndreR
 * *********************************************************
 */
package edu.tigers.sumatra.botmanager.bots.communication.udp;

import edu.tigers.sumatra.botmanager.commands.ACommand;


/**
 * Observer for UDP receiver.
 * 
 * @author AndreR
 */
@FunctionalInterface
public interface IReceiverUDPObserver
{
	/**
	 * @param cmd
	 */
	void onNewCommand(ACommand cmd);
}
