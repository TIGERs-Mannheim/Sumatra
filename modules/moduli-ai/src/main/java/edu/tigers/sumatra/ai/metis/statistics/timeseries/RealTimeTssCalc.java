/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.statistics.timeseries;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.statistics.TimeSeriesStatsEntry;


public class RealTimeTssCalc implements ITssCalc
{
	@Override
	public TimeSeriesStatsEntry createTimeSeriesStatsEntry(BaseAiFrame aiFrame, long timestamp)
	{
		TimeSeriesStatsEntry entry = new TimeSeriesStatsEntry("realtime", timestamp);
		entry.addField("system", aiFrame.getWorldFrameWrapper().getUnixTimestamp());
		entry.addField("jvm", System.nanoTime());
		return entry;
	}
}
