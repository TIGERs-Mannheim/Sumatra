/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.engine;

import edu.tigers.sumatra.referee.gameevent.IGameEvent;


public interface IAutoRefEngineObserver
{
	/**
	 * The autoRef engine has detected a new game event
	 *
	 * @param gameEvent
	 */
	void onNewGameEventDetected(IGameEvent gameEvent);
}
