/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 27, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoreferee.engine.log;

import java.time.Instant;

import edu.tigers.autoreferee.engine.FollowUpAction;
import edu.tigers.autoreferee.engine.RefboxRemoteCommand;
import edu.tigers.autoreferee.engine.events.IGameEvent;
import edu.tigers.autoreferee.engine.log.GameLogEntry.ELogEntryType;
import edu.tigers.sumatra.referee.data.GameState;
import edu.tigers.sumatra.referee.data.RefereeMsg;


/**
 * @author "Lukas Magel"
 */
public class GameLogEntryBuilder
{
	private ELogEntryType			type;
	/** frame timestamp in nanoseconds */
	private Long						timestamp;
	/** Game time left in the stage when the event was created */
	private GameTime					gameTime;
	/** The time instant this entry was created in */
	private Instant					instant;
	
	private IGameEvent				gameEvent;
	private boolean					acceptedByEngine;
	
	private GameState					gamestate;
	private RefereeMsg				refereeMsg;
	private FollowUpAction			followUpAction;
	private RefboxRemoteCommand	command;
	
	
	private void setType(final ELogEntryType type)
	{
		this.type = type;
	}
	
	
	/**
	 * @param timestamp
	 */
	public void setTimestamp(final long timestamp)
	{
		this.timestamp = timestamp;
	}
	
	
	/**
	 * @param gameTime
	 */
	public void setGameTime(final GameTime gameTime)
	{
		this.gameTime = gameTime;
	}
	
	
	/**
	 * @param instant
	 */
	public void setInstant(final Instant instant)
	{
		this.instant = instant;
	}
	
	
	/**
	 * @param gamestate
	 */
	public void setGamestate(final GameState gamestate)
	{
		this.gamestate = gamestate;
		setType(ELogEntryType.GAME_STATE);
	}
	
	
	/**
	 * @param gameEvent
	 * @param acceptedByEngine If the game state was accepted by the autoref engine
	 */
	public void setGameEvent(final IGameEvent gameEvent, final boolean acceptedByEngine)
	{
		this.gameEvent = gameEvent;
		this.acceptedByEngine = acceptedByEngine;
		setType(ELogEntryType.GAME_EVENT);
	}
	
	
	/**
	 * @param refereeMsg
	 */
	public void setRefereeMsg(final RefereeMsg refereeMsg)
	{
		this.refereeMsg = refereeMsg;
		setType(ELogEntryType.REFEREE_MSG);
	}
	
	
	/**
	 * @param followUpAction
	 */
	public void setFollowUpAction(final FollowUpAction followUpAction)
	{
		this.followUpAction = followUpAction;
		setType(ELogEntryType.FOLLOW_UP);
	}
	
	
	/**
	 * @param command
	 */
	public void setCommand(final RefboxRemoteCommand command)
	{
		this.command = command;
		setType(ELogEntryType.COMMAND);
	}
	
	
	/**
	 * @return
	 */
	public GameLogEntry toEntry()
	{
		if ((type == null) || (timestamp == null) || (gameTime == null) || (instant == null))
		{
			throw new NullPointerException("Not all required fields have been set");
		}
		
		return new GameLogEntry(timestamp, gameTime, instant, type, gamestate, gameEvent, acceptedByEngine, refereeMsg,
				followUpAction, command);
	}
}
