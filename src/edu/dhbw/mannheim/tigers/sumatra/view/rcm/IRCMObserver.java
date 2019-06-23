/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Aug 31, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.rcm;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm.ActionSender;


/**
 * Observer for RCM gui
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface IRCMObserver
{
	/**
	 * @param activeState
	 */
	void onStartStopButtonPressed(final boolean activeState);
	
	
	/**
	 * @param keepConnections
	 */
	void onReconnect(boolean keepConnections);
	
	
	/**
	 * @param keepConnections
	 */
	void setUpController(final boolean keepConnections);
	
	
	/**
	 * Request to switch the current bot
	 * 
	 * @param actionSender
	 */
	void onNextBot(ActionSender actionSender);
	
	
	/**
	 * Request to switch the current bot
	 * 
	 * @param actionSender
	 */
	void onPrevBot(ActionSender actionSender);
	
	
	/**
	 * @param actionSender
	 */
	void onBotUnassigned(ActionSender actionSender);
}
