/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.gamelog;

import edu.tigers.moduli.AModule;


public class GameLogRecorder extends AModule
{
	private final GameLogWriter writer = new GameLogWriter(GameLogType.LOG_FILE);


	@Override
	public void stopModule()
	{
		if (writer.isOpen())
		{
			writer.close();
		}
	}


	public void setRecording(boolean enable)
	{
		if (enable && !writer.isOpen())
		{
			writer.open();
		} else if (!enable && writer.isOpen())
		{
			writer.close();
		}
	}


	public void writeMessage(final GameLogMessage msg)
	{
		writer.write(msg);
	}
}
