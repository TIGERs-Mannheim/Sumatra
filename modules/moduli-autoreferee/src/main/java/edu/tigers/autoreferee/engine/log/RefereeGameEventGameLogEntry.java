package edu.tigers.autoreferee.engine.log;

import edu.tigers.sumatra.referee.gameevent.IGameEvent;


public class RefereeGameEventGameLogEntry extends GameLogEntry
{
	private final IGameEvent gameEvent;


	public RefereeGameEventGameLogEntry(
			final long timestamp,
			final GameTime gameTime,
			final IGameEvent gameEvent)
	{
		super(ELogEntryType.RECEIVED_GAME_EVENT, timestamp, gameTime);
		this.gameEvent = gameEvent;
	}


	@Override
	public String workGameLogEntry()
	{
		return gameEvent.getType().name() + " - " + gameEvent.toString();
	}


	@Override
	public String getToolTipText()
	{
		return "Received a new referee message with command " + gameEvent.getType();
	}


	@Override
	public String toString()
	{
		return String.format("%d | %s | %s | %s", getTimestamp(), getGameTime(), getType(), gameEvent);
	}
}
