package edu.tigers.sumatra.ai.metis.statistics.timeseries;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ballpossession.BallPossession;
import edu.tigers.sumatra.ai.metis.ballpossession.EBallPossession;
import edu.tigers.sumatra.statistics.TimeSeriesStatsEntry;
import lombok.RequiredArgsConstructor;

import java.util.function.Supplier;


/**
 * Time series stats calc for ball possession.
 */
@RequiredArgsConstructor
public class BallPossessionTssCalc implements ITssCalc
{
	private final Supplier<BallPossession> ballPossession;


	@Override
	public TimeSeriesStatsEntry createTimeSeriesStatsEntry(final BaseAiFrame aiFrame, final long timestamp)
	{
		TimeSeriesStatsEntry ballPossessionEntry = new TimeSeriesStatsEntry("ball.possession", timestamp);
		for (EBallPossession p : EBallPossession.values())
		{
			final EBallPossession eBallPossession = ballPossession.get().getEBallPossession();
			ballPossessionEntry.addField(p.name().toLowerCase(), p == eBallPossession ? 1 : 0);
		}
		return ballPossessionEntry;
	}
}
