/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.view.referee;

import edu.tigers.sumatra.referee.control.Event;


public interface IRefBoxRemoteControlRequestObserver
{
	/**
	 * New game controller event.
	 * 
	 * @param event
	 */
	void sendGameControllerEvent(Event event);
}
