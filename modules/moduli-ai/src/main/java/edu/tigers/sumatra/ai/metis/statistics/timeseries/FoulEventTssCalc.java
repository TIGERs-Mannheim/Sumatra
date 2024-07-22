/*
 * Copyright (c) 2009 - 2024, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.statistics.timeseries;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.referee.gameevent.EGameEventType;
import edu.tigers.sumatra.referee.gameevent.IGameEvent;
import edu.tigers.sumatra.statistics.TimeSeriesStatsEntry;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public class FoulEventTssCalc implements ITssCalc
{
	private List<IGameEvent> publishedEvents = new ArrayList<>();


	@Override
	public TimeSeriesStatsEntry createTimeSeriesStatsEntry(BaseAiFrame aiFrame, long timestamp)
	{
		var refMsg = aiFrame.getRefereeMsg();

		publishedEvents.removeIf(event -> !refMsg.getGameEvents().contains(event));
		var newFouls = refMsg.getGameEvents().stream()
				.filter(event -> event.getType().getType() == EGameEventType.FOUL)
				.filter(event -> !publishedEvents.contains(event))
				.sorted(Comparator.comparingLong(IGameEvent::getCreatedTimestamp))
				.toList();

		if (!newFouls.isEmpty())
		{
			var entry = new TimeSeriesStatsEntry("foulEvents", timestamp);
			var event = newFouls.getFirst();
			publishedEvents.add(event);
			if (event.getTeam() == ETeamColor.NEUTRAL)
			{
				entry.addTag(ETeamColor.BLUE.toString(), event.getType().toString());
				entry.addTag(ETeamColor.YELLOW.toString(), event.getType().toString());
			} else
			{
				entry.addField(event.getTeam().toString(), event.getType().toString());
			}
			return entry;
		}
		return null;
	}


	@Override
	public boolean executeOnlyDuringRunning()
	{
		return false;
	}
}
