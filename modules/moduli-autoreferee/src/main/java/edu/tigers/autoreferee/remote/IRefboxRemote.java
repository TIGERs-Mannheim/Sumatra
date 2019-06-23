/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoreferee.remote;

import edu.tigers.autoreferee.engine.RefboxRemoteCommand;


/**
 * @author "Lukas Magel"
 */
public interface IRefboxRemote
{
	
	/**
	 * @param command
	 * @return
	 */
	void sendCommand(RefboxRemoteCommand command);
	
	
	/**
	 * 
	 */
	void stop();
}
