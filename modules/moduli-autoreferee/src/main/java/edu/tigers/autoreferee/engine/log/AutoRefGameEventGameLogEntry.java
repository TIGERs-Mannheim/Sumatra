/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.engine.log;

import edu.tigers.sumatra.referee.gameevent.IGameEvent;


public class AutoRefGameEventGameLogEntry extends GameEventGameLogEntry
{
	public AutoRefGameEventGameLogEntry(
			final long timestamp,
			final GameTime gameTime,
			final IGameEvent gameEvent)
	{
		super(ELogEntryType.DETECTED_GAME_EVENT, timestamp, gameTime, gameEvent);
	}


	@Override
	public String getToolTipText()
	{
		return "The AutoReferee has registered the following game event " + gameEvent.getType();
	}
}
