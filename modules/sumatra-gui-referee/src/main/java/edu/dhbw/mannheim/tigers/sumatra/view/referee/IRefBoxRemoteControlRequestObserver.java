/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - Tigers Mannheim
 */
package edu.dhbw.mannheim.tigers.sumatra.view.referee;

import edu.tigers.sumatra.RefboxRemoteControl.SSL_RefereeRemoteControlRequest;


/**
 * @author AndreR <andre@ryll.cc>
 */
@FunctionalInterface
public interface IRefBoxRemoteControlRequestObserver
{
	/**
	 * New control request.
	 * 
	 * @param req
	 */
	void onNewControlRequest(SSL_RefereeRemoteControlRequest req);
}
