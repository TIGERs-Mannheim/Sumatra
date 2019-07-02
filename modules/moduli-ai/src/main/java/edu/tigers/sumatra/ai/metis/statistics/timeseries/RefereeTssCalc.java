package edu.tigers.sumatra.ai.metis.statistics.timeseries;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ITacticalField;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.referee.data.GameState;
import edu.tigers.sumatra.referee.data.RefereeMsg;
import edu.tigers.sumatra.referee.data.TeamInfo;
import edu.tigers.sumatra.statistics.TimeSeriesStatsEntry;


/**
 * Create time series stats from referee state.
 */
public class RefereeTssCalc implements ITssCalc
{
	@Override
	public TimeSeriesStatsEntry createTimeSeriesStatsEntry(final BaseAiFrame aiFrame, final ITacticalField tacticalField,
			final long timestamp)
	{
		final TimeSeriesStatsEntry entry = new TimeSeriesStatsEntry("referee", timestamp);
		final GameState gamestate = aiFrame.getGamestate();
		final RefereeMsg refereeMsg = aiFrame.getRefereeMsg();
		final ETeamColor ourTeam = aiFrame.getTeamColor();
		
		entry.addField("state.type", gamestate.getState().name());
		entry.addField("state.for", gamestate.getForTeam().name());
		entry.addField("command", refereeMsg.getCommand().name());
		entry.addField("goals.we", refereeMsg.getGoals().get(ourTeam));
		entry.addField("goals.they", refereeMsg.getGoals().get(ourTeam.opposite()));
		
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
