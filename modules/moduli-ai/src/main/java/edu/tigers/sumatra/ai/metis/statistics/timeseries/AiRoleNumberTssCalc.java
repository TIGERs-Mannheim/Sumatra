package edu.tigers.sumatra.ai.metis.statistics.timeseries;

import java.util.EnumSet;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ITacticalField;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.statistics.TimeSeriesStatsEntry;


/**
 * Time series stats calc for number of offensive, support and defense roles.
 */
public class AiRoleNumberTssCalc implements ITssCalc
{
	@Override
	public TimeSeriesStatsEntry createTimeSeriesStatsEntry(final BaseAiFrame aiFrame, final ITacticalField tacticalField,
			final long timestamp)
	{
		TimeSeriesStatsEntry entry = new TimeSeriesStatsEntry("ai.role.number", timestamp);
		for (EPlay play : EnumSet.of(EPlay.OFFENSIVE, EPlay.SUPPORT, EPlay.DEFENSIVE))
		{
			entry.addField(play.name().toLowerCase(), tacticalField.getPlayNumbers().getOrDefault(play, 0));
		}
		entry.addField("all", tacticalField.getPlayNumbers().values().stream().mapToInt(i -> i).sum());
		return entry;
	}
}
