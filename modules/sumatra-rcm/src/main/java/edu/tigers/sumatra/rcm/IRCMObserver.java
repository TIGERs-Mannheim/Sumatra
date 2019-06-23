/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.rcm;

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
