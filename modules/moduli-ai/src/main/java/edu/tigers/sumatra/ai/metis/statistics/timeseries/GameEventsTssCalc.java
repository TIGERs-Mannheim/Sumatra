package edu.tigers.sumatra.ai.metis.statistics.timeseries;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.referee.gameevent.EGameEventType;
import edu.tigers.sumatra.referee.gameevent.IGameEvent;
import edu.tigers.sumatra.statistics.TimeSeriesStatsEntry;


/**
 * Time series stats calc for game events from game-controller.
 */
public class GameEventsTssCalc implements ITssCalc
{
	@Override
	public TimeSeriesStatsEntry createTimeSeriesStatsEntry(final BaseAiFrame aiFrame, final long timestamp)
	{
		TimeSeriesStatsEntry entry = new TimeSeriesStatsEntry("game.events", timestamp);

		for (EGameEventType gameEventType : EGameEventType.values())
		{
			long count = aiFrame.getRefereeMsg().getGameEvents().stream()
					.filter(e -> e.getType().getType() == gameEventType)
					.count();
			entry.addField(gameEventType.name().toLowerCase().replace('_', '.'), count);
		}

		for (IGameEvent gameEvent : aiFrame.getRefereeMsg().getGameEvents())
		{
			entry.addTag(gameEvent.getType().name().toLowerCase().replace('_', '.'), "1");
		}
		return entry;
	}
}
