/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 9, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoreferee.remote;

import edu.tigers.autoreferee.engine.RefCommand;


/**
 * @author "Lukas Magel"
 */
public interface IRefboxRemote
{
	
	/**
	 * @param command
	 * @return
	 */
	public ICommandResult sendCommand(RefCommand command);
	
	
	/**
	 * 
	 */
	public void stop();
}
