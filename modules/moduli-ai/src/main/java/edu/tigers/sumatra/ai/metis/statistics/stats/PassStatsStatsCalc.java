/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.statistics.stats;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.pass.PassStats;
import lombok.RequiredArgsConstructor;

import java.util.function.Supplier;


@RequiredArgsConstructor
public class PassStatsStatsCalc extends AStatsCalc
{
	private final Supplier<PassStats> passStats;


	@Override
	public void saveStatsToMatchStatistics(MatchStats matchStatistics)
	{
		matchStatistics.putStatisticData(EMatchStatistics.PASS_STATS_N_PASSES,
				new StatisticData(passStats.get().getNPasses()));
		matchStatistics.putStatisticData(EMatchStatistics.PASS_STATS_SUCCESSFUL_PASSES,
				new StatisticData(passStats.get().getSuccessfulPasses()));
		matchStatistics.putStatisticData(EMatchStatistics.PASS_STATS_PASS_LINE_REACHED,
				new StatisticData(passStats.get().getNumPassLineReachedOnTime()));
	}


	@Override
	public void onStatisticUpdate(BaseAiFrame baseAiFrame)
	{
		// No work necessary here
	}
}
