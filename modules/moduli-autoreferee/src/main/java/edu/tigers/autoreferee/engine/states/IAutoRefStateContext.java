/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoreferee.engine.states;

import edu.tigers.autoreferee.engine.AutoRefGlobalState;
import edu.tigers.autoreferee.engine.FollowUpAction;
import edu.tigers.autoreferee.engine.RefboxRemoteCommand;
import edu.tigers.autoreferee.engine.log.GameLog;


/**
 * @author "Lukas Magel"
 */
public interface IAutoRefStateContext
{
	/**
	 * @param cmd
	 * @return
	 */
	void sendCommand(RefboxRemoteCommand cmd);
	
	
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
	
	
	/**
	 * @return a globally available state for the autoRef
	 */
	AutoRefGlobalState getAutoRefGlobalState();
}
