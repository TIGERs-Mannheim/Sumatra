package edu.tigers.sumatra.ai.metis.statistics.timeseries;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.statistics.TimeSeriesStatsEntry;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;


/**
 * Time series stats calc for number of offensive, support and defense roles.
 */
@RequiredArgsConstructor
public class AiRoleNumberTssCalc implements ITssCalc
{
	private final Supplier<Map<EPlay, Set<BotID>>> desiredBotMap;


	@Override
	public TimeSeriesStatsEntry createTimeSeriesStatsEntry(final BaseAiFrame aiFrame, final long timestamp)
	{
		TimeSeriesStatsEntry entry = new TimeSeriesStatsEntry("ai.role.number", timestamp);
		for (EPlay play : EnumSet.of(EPlay.OFFENSIVE, EPlay.SUPPORT, EPlay.DEFENSIVE))
		{
			entry.addField(play.name().toLowerCase(), desiredBotMap.get().getOrDefault(play,
					Collections.emptySet()).size());
		}
		entry.addField("all", desiredBotMap.get().values().stream().mapToInt(Set::size).sum());
		return entry;
	}
}
