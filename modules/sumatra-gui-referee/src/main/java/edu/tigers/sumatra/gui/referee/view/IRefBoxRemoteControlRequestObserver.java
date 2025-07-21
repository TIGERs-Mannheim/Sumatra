/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.gui.referee.view;

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
