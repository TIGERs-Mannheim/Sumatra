package edu.tigers.autoreferee.engine.log;

import edu.tigers.sumatra.referee.data.GameState;


public class GameStateGameLogEntry extends GameLogEntry
{
	private final GameState gameState;


	public GameStateGameLogEntry(
			final long timestamp,
			final GameTime gameTime,
			final GameState gamestate)
	{
		super(ELogEntryType.GAME_STATE, timestamp, gameTime);
		this.gameState = gamestate;
	}


	@Override
	public String workGameLogEntry()
	{
		return gameState.toString();
	}


	@Override
	public String getToolTipText()
	{
		return "The game state has changed to " + gameState;
	}


	@Override
	public String toString()
	{
		return String.format("%d | %s | %s | %s", getTimestamp(), getGameTime(), getType(), gameState);
	}
}
