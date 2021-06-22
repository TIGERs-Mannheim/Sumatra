/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.engine.log;

import edu.tigers.sumatra.referee.gameevent.IGameEvent;


public class RefereeGameEventGameLogEntry extends GameEventGameLogEntry
{
	public RefereeGameEventGameLogEntry(
			final long timestamp,
			final GameTime gameTime,
			final IGameEvent gameEvent)
	{
		super(ELogEntryType.RECEIVED_GAME_EVENT, timestamp, gameTime, gameEvent);
	}


	@Override
	public String getToolTipText()
	{
		return "Received a new game event: " + gameEvent.getType();
	}
}
