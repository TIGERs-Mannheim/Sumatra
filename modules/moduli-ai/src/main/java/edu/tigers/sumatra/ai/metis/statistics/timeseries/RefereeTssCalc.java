/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.statistics.timeseries;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.referee.data.GameState;
import edu.tigers.sumatra.referee.data.RefereeMsg;
import edu.tigers.sumatra.referee.data.TeamInfo;
import edu.tigers.sumatra.referee.gameevent.EGameEventType;
import edu.tigers.sumatra.referee.gameevent.IGameEvent;
import edu.tigers.sumatra.statistics.TimeSeriesStatsEntry;

import java.util.Comparator;
import java.util.Objects;


/**
 * Create time series stats from referee state.
 */
public class RefereeTssCalc implements ITssCalc
{
	@Override
	public TimeSeriesStatsEntry createTimeSeriesStatsEntry(final BaseAiFrame aiFrame, final long timestamp)
	{
		final TimeSeriesStatsEntry entry = new TimeSeriesStatsEntry("referee", timestamp);
		final GameState gamestate = aiFrame.getGameState();
		final RefereeMsg refereeMsg = aiFrame.getRefereeMsg();
		final ETeamColor ourTeam = aiFrame.getTeamColor();

		var fouls = refereeMsg.getGameEvents().stream()
				.filter(event -> event.getType().getType() == EGameEventType.FOUL)
				.toList();

		var mostRecentWe = fouls.stream()
				.filter(event -> event.getTeam() == ourTeam || event.getTeam() == ETeamColor.NEUTRAL)
				.max(Comparator.comparing(IGameEvent::getCreatedTimestamp))
				.map(IGameEvent::getType)
				.map(Objects::toString)
				.orElse("NONE");
		var mostRecentTheir = fouls.stream()
				.filter(event -> event.getTeam() != ourTeam)
				.max(Comparator.comparing(IGameEvent::getCreatedTimestamp))
				.map(IGameEvent::getType)
				.map(Objects::toString)
				.orElse("NONE");


		entry.addField("state.type", gamestate.getState().name());
		entry.addField("state.for", gamestate.getForTeam().name());
		entry.addField("command", refereeMsg.getCommand().name());
		entry.addField("goals.we", refereeMsg.getGoals().get(ourTeam));
		entry.addField("goals.they", refereeMsg.getGoals().get(ourTeam.opposite()));
		entry.addField("lastFoul.we", mostRecentWe);
		entry.addField("lastFoul.their", mostRecentTheir);

		for (ETeamColor teamColor : ETeamColor.yellowBlueValues())
		{
			final TeamInfo teamInfo = refereeMsg.getTeamInfo(teamColor);
			final String teamId = teamColor == ourTeam ? "we" : "their";
			entry.addField("ball.placement.failures." + teamId, teamInfo.getBallPlacementFailures());
			entry.addField("foul.counter." + teamId, teamInfo.getFoulCounter());
			entry.addField("cards.red." + teamId, teamInfo.getRedCards());
			entry.addField("cards.yellow." + teamId, teamInfo.getYellowCards());
		}
		return entry;
	}
}
