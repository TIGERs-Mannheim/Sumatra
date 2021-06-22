/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.engine.log;

import edu.tigers.sumatra.referee.proto.SslGcRefereeMessage;


public class RefereeCommandGameLogEntry extends GameLogEntry
{
	private final SslGcRefereeMessage.Referee.Command command;


	public RefereeCommandGameLogEntry(
			final long timestamp,
			final GameTime gameTime,
			final SslGcRefereeMessage.Referee.Command command)
	{
		super(ELogEntryType.RECEIVED_REFEREE_MSG, timestamp, gameTime);
		this.command = command;
	}


	@Override
	public String workGameLogEntry()
	{
		return command.name();
	}


	@Override
	public String getToolTipText()
	{
		return "Received a new referee message with command " + command;
	}


	@Override
	public String toString()
	{
		return String.format("%d | %s | %s | %s", getTimestamp(), getGameTime(), getType(), command);
	}
}
