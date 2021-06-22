/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.view.referee;

import edu.tigers.sumatra.referee.proto.SslGcApi;


public interface IRefBoxRemoteControlRequestObserver
{
	/**
	 * New game controller event.
	 *
	 * @param event
	 */
	void sendGameControllerEvent(SslGcApi.Input event);
}
