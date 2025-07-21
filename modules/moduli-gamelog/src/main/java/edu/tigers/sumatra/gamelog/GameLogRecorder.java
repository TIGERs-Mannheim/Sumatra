/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.gamelog;


import edu.tigers.sumatra.moduli.AModule;


public class GameLogRecorder extends AModule
{
	private final GameLogWriter writer = new GameLogWriter();
	private String matchType = "";
	private String matchStage = "";
	private String teamYellow = "";
	private String teamBlue = "";


	/**
	 * @param match  type of match
	 * @param stage  stage of match
	 * @param yellow name of yellow team
	 * @param blue   name of blue team
	 */
	public void setMatchInfo(String match, String stage, String yellow, String blue)
	{
		matchType = match;
		matchStage = stage;
		teamYellow = yellow;
		teamBlue = blue;
	}


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
			writer.open(matchType, matchStage, teamYellow, teamBlue);
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
