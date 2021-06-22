/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.statistics.stats;

import edu.tigers.sumatra.ai.BaseAiFrame;

import java.util.Map;


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
	 * @param baseAiFrame
	 */
	public abstract void onStatisticUpdate(final BaseAiFrame baseAiFrame);


	protected void incrementEntryForBotIDInMap(final Integer key, final Map<Integer, Integer> mapToIncrease)
	{
		mapToIncrease.merge(key, 1, Integer::sum);
	}

}
