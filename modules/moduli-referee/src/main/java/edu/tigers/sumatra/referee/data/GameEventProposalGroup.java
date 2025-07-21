/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.referee.data;

import edu.tigers.sumatra.referee.gameevent.IGameEvent;

import java.util.List;


/**
 * A proposed game event.
 */
public class GameEventProposalGroup
{
	private final List<IGameEvent> gameEvents;


	public GameEventProposalGroup(final List<IGameEvent> gameEvents)
	{
		this.gameEvents = gameEvents;
	}


	public List<IGameEvent> getGameEvents()
	{
		return gameEvents;
	}
}
