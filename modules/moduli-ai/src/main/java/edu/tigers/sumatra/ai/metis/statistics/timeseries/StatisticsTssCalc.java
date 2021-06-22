package edu.tigers.sumatra.ai.metis.statistics.timeseries;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.statistics.stats.EMatchStatistics;
import edu.tigers.sumatra.ai.metis.statistics.stats.MatchStats;
import edu.tigers.sumatra.ai.metis.statistics.stats.StatisticData;
import edu.tigers.sumatra.statistics.TimeSeriesStatsEntry;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.function.Supplier;


/**
 * Time series stats for match statistics.
 */
@RequiredArgsConstructor
public class StatisticsTssCalc implements ITssCalc
{
	private final Supplier<MatchStats> matchStats;


	@Override
	public TimeSeriesStatsEntry createTimeSeriesStatsEntry(final BaseAiFrame aiFrame, final long timestamp)
	{
		TimeSeriesStatsEntry entry = new TimeSeriesStatsEntry("statistics", timestamp);
		for (Map.Entry<EMatchStatistics, StatisticData> mapEntry : matchStats.get().getStatistics().entrySet())
		{
			entry.addField(mapEntry.getKey().name().toLowerCase(), mapEntry.getValue().getGeneralStatistics());
		}
		return entry;
	}
}
