/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.statistics;

import java.util.Map;

import edu.tigers.sumatra.ai.data.MatchStats;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ids.BotID;


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
	public abstract void saveStatsToMatchStatistics(MatchStats matchStatistics);
	
	
	/**
	 * This function is an observing method that listens to the StatisticsCalculator
	 * 
	 * @param newTacticalField
	 * @param baseAiFrame
	 */
	public abstract void onStatisticUpdate(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame);
	

	
	protected void incrementEntryForBotIDInMap(final BotID key, final Map<BotID, Integer> mapToIncrease)
	{
		mapToIncrease.merge(key, 1, (a, b) -> a + b);
	}
	
}
