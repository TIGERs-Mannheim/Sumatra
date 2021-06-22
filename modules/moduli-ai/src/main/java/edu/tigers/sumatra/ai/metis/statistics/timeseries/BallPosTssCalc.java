package edu.tigers.sumatra.ai.metis.statistics.timeseries;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.statistics.TimeSeriesStatsEntry;
import edu.tigers.sumatra.wp.data.ITrackedBall;


/**
 * Time series stats calc for ball position.
 */
public class BallPosTssCalc implements ITssCalc
{
	@Override
	public TimeSeriesStatsEntry createTimeSeriesStatsEntry(final BaseAiFrame aiFrame, final long timestamp)
	{
		TimeSeriesStatsEntry entry = new TimeSeriesStatsEntry("ball.pos", timestamp);
		ITrackedBall ball = aiFrame.getWorldFrame().getBall();
		entry.addField("x", ball.getPos().x() / 1000.0);
		entry.addField("y", ball.getPos().y() / 1000.0);
		entry.addField("z", ball.getHeight() / 1000.0);
		return entry;
	}
}
