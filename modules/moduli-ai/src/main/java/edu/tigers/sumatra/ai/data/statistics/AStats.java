/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 12, 2016
 * Author(s): Phillipp Mevenkamp <phillippmevenkamp@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.data.statistics;

import java.util.Map;

import edu.tigers.sumatra.ai.data.MatchStatistics;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * @author Phillipp Mevenkamp <phillippmevenkamp@gmail.com>
 */
public abstract class AStats
{
	/**
	 * This function will save the calculated statistics to the current MatchStatistics Instance
	 * 
	 * @param matchStatistics - The current MatchStatistics instance
	 */
	public abstract void saveStatsToMatchStatistics(MatchStatistics matchStatistics);
	
	
	/**
	 * This function is an observing method that listens to the StatisticsCalculator
	 * 
	 * @param newTacticalField
	 * @param baseAiFrame
	 */
	public abstract void onStatisticUpdate(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame);
	
	
	protected int getBotHardwareID(final BotID tigerToGet, final BaseAiFrame baseAiFrame)
	{
		ITrackedBot tBot = baseAiFrame.getWorldFrame().getBot(tigerToGet);
		int tigerHwId = -1;
		if (tBot != null)
		{
			tigerHwId = tBot.getBot().getHardwareId();
		}
		
		return tigerHwId;
	}
	
	
	protected void incrementEntryForBotIDInMap(final BotID key, final Map<BotID, Integer> mapToIncrease)
	{
		Integer entryAtBotID = mapToIncrease.get(key);
		
		if (entryAtBotID == null)
		{
			mapToIncrease.put(key, 1);
		} else
		{
			mapToIncrease.put(key, entryAtBotID + 1);
		}
	}
	
}
