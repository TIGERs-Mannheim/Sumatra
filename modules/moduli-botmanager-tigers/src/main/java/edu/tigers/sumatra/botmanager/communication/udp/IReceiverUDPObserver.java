/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botmanager.communication.udp;

import edu.tigers.sumatra.botmanager.commands.ACommand;


/**
 * Observer for UDP receiver.
 */
@FunctionalInterface
public interface IReceiverUDPObserver
{
	/**
	 * @param cmd
	 */
	void onNewCommand(ACommand cmd);
}
