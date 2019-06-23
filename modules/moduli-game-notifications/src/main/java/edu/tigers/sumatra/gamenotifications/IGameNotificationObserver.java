/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.gamenotifications;

/**
 * @author Sebastian Stein <sebastian-stein@gmx.de>
 */
@FunctionalInterface
public interface IGameNotificationObserver
{
	
	/**
	 * Handles every game event
	 * 
	 * @param e The game event
	 */
	void onGameEvent(AGameEvent e);
	
}
