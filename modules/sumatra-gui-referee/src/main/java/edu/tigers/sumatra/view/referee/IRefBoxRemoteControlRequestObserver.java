/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.view.referee;

import edu.tigers.sumatra.RefboxRemoteControl.SSL_RefereeRemoteControlRequest;
import edu.tigers.sumatra.ids.BotID;


/**
 * @author AndreR <andre@ryll.cc>
 */
public interface IRefBoxRemoteControlRequestObserver
{
	/**
	 * New control request.
	 * 
	 * @param req
	 */
	void onNewControlRequest(SSL_RefereeRemoteControlRequest req);
	
	
	/**
	 * The goalie has changed
	 *
	 * @param keeperId
	 */
	void onGoalieChanged(BotID keeperId);
}
