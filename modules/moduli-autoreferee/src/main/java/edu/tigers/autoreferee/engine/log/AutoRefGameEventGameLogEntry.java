package edu.tigers.autoreferee.engine.log;

import java.awt.Color;

import edu.tigers.sumatra.referee.gameevent.IGameEvent;


public class AutoRefGameEventGameLogEntry extends GameLogEntry
{
	private final IGameEvent gameEvent;


	public AutoRefGameEventGameLogEntry(
			final long timestamp,
			final GameTime gameTime,
			final IGameEvent gameEvent)
	{
		super(ELogEntryType.DETECTED_GAME_EVENT, timestamp, gameTime);
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
		return "The AutoReferee has registered the following game event " + gameEvent;
	}


	@Override
	public String toString()
	{
		return String.format("%d | %s | %s | %s", getTimestamp(), getGameTime(), getType(), gameEvent);
	}


	@Override
	public Color getForegroundColor()
	{
		switch (gameEvent.getType().getType())
		{
			case MINOR_OFFENSE:
				return new Color(250, 150, 31);
			case FOUL:
				return new Color(250, 14, 10);
			case UNSPORTING:
				return new Color(250, 12, 112);
			case MATCH_PROCEEDING:
			case BALL_LEFT_FIELD:
			case REPEATED:
			default:
				return super.getForegroundColor();
		}
	}
}
