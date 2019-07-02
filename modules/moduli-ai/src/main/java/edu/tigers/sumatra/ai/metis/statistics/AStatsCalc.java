/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.statistics;

import java.util.Map;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.TacticalField;


/**
 * Base class for statistics calculators.
 */
public abstract class AStatsCalc
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
	
	
	protected void incrementEntryForBotIDInMap(final Integer key, final Map<Integer, Integer> mapToIncrease)
	{
		mapToIncrease.merge(key, 1, (a, b) -> a + b);
	}
	
}
