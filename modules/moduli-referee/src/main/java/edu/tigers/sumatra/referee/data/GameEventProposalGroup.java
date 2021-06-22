/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.referee.data;

import java.util.List;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.referee.gameevent.IGameEvent;


/**
 * A proposed game event.
 */
@Persistent
public class GameEventProposalGroup
{
	private final List<IGameEvent> gameEvents;


	@SuppressWarnings("unused") // used by berkeley
	private GameEventProposalGroup()
	{
		gameEvents = null;
	}


	public GameEventProposalGroup(final List<IGameEvent> gameEvents)
	{
		this.gameEvents = gameEvents;
	}


	public List<IGameEvent> getGameEvents()
	{
		return gameEvents;
	}
}
