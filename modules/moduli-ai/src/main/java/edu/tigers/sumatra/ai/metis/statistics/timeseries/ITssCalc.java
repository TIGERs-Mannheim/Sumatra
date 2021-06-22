package edu.tigers.sumatra.ai.metis.statistics.timeseries;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.statistics.TimeSeriesStatsEntry;


/**
 * Interface for a time series stats calculator.
 */
public interface ITssCalc
{
	TimeSeriesStatsEntry createTimeSeriesStatsEntry(
			final BaseAiFrame aiFrame,
			final long timestamp);
}
