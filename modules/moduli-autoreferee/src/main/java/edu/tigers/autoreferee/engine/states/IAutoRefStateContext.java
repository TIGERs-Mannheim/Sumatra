/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 2, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoreferee.engine.states;

import edu.tigers.autoreferee.engine.FollowUpAction;
import edu.tigers.autoreferee.engine.RefboxRemoteCommand;
import edu.tigers.autoreferee.engine.log.GameLog;
import edu.tigers.autoreferee.remote.ICommandResult;


/**
 * @author "Lukas Magel"
 */
public interface IAutoRefStateContext
{
	/**
	 * @param cmd
	 * @return
	 */
	ICommandResult sendCommand(RefboxRemoteCommand cmd);
	
	
	/**
	 * @return
	 */
	FollowUpAction getFollowUpAction();
	
	
	/**
	 * @param action
	 */
	void setFollowUpAction(FollowUpAction action);
	
	
	/**
	 * @return
	 */
	boolean doProceed();
	
	
	/**
	 * @return
	 */
	GameLog getGameLog();
	
}
