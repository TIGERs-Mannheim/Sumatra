package edu.tigers.sumatra.ai.metis.statistics.timeseries;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ITacticalField;
import edu.tigers.sumatra.statistics.TimeSeriesStatsEntry;
import edu.tigers.sumatra.wp.data.ITrackedBall;


/**
 * Time series stats calc for ball velocity.
 */
public class BallVelTssCalc implements ITssCalc
{
	@Override
	public TimeSeriesStatsEntry createTimeSeriesStatsEntry(final BaseAiFrame aiFrame, final ITacticalField tacticalField,
			final long timestamp)
	{
		TimeSeriesStatsEntry entry = new TimeSeriesStatsEntry("ball.vel", timestamp);
		ITrackedBall ball = aiFrame.getWorldFrame().getBall();
		entry.addField("x", ball.getVel().x());
		entry.addField("y", ball.getVel().y());
		entry.addField("z", ball.getVel3().z());
		entry.addField("abs", ball.getVel3().getLength());
		return entry;
	}
}
