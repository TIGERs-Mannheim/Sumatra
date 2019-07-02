package edu.tigers.sumatra.ai.metis.statistics.timeseries;

import java.util.Map;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ITacticalField;
import edu.tigers.sumatra.ai.metis.statistics.EMatchStatistics;
import edu.tigers.sumatra.ai.metis.statistics.StatisticData;
import edu.tigers.sumatra.statistics.TimeSeriesStatsEntry;


/**
 * Time series stats for match statistics.
 */
public class StatisticsTssCalc implements ITssCalc
{
	@Override
	public TimeSeriesStatsEntry createTimeSeriesStatsEntry(final BaseAiFrame aiFrame, final ITacticalField tacticalField,
			final long timestamp)
	{
		TimeSeriesStatsEntry entry = new TimeSeriesStatsEntry("statistics", timestamp);
		for (Map.Entry<EMatchStatistics, StatisticData> mapEntry : tacticalField.getMatchStatistics().getStatistics()
				.entrySet())
		{
			entry.addField(mapEntry.getKey().name().toLowerCase(), mapEntry.getValue().getGeneralStatistics());
		}
		return entry;
	}
}
