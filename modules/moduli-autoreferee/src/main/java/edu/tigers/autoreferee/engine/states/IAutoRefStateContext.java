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
import edu.tigers.autoreferee.engine.RefCommand;
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
	public ICommandResult sendCommand(RefCommand cmd);
	
	
	/**
	 * @return
	 */
	public FollowUpAction getFollowUpAction();
	
	
	/**
	 * @param action
	 */
	public void setFollowUpAction(FollowUpAction action);
	
	
	/**
	 * @return
	 */
	public boolean doProceed();
	
	
	/**
	 * @return
	 */
	public GameLog getGameLog();
	
}
