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
 * Transceiver base observer interface.
 * 
 * @author AndreR
 */
public interface ITransceiverObserver
{
	/**
	 * Called after Sumatra gets a new command from the target
	 * 
	 * @param cmd New command.
	 */
	void onIncommingCommand(ACommand cmd);
	
	
	/**
	 * Called after Sumatra sends a command to the target
	 * 
	 * @param cmd Command
	 */
	void onOutgoingCommand(ACommand cmd);
}
