package edu.tigers.sumatra.ai.metis.statistics.timeseries;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ITacticalField;
import edu.tigers.sumatra.ai.metis.ballpossession.EBallPossession;
import edu.tigers.sumatra.statistics.TimeSeriesStatsEntry;


/**
 * Time series stats calc for ball possession.
 */
public class BallPossessionTssCalc implements ITssCalc
{
	@Override
	public TimeSeriesStatsEntry createTimeSeriesStatsEntry(final BaseAiFrame aiFrame, final ITacticalField tacticalField,
			final long timestamp)
	{
		TimeSeriesStatsEntry ballPossessionEntry = new TimeSeriesStatsEntry("ball.possession", timestamp);
		for (EBallPossession p : EBallPossession.values())
		{
			final EBallPossession ballPossession = tacticalField.getBallPossession().getEBallPossession();
			ballPossessionEntry.addField(p.name().toLowerCase(), p == ballPossession ? 1 : 0);
		}
		return ballPossessionEntry;
	}
}
