package edu.tigers.sumatra.ai.metis.statistics.timeseries;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ITacticalField;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.statistics.TimeSeriesStatsEntry;


/**
 * Time series stats calc for ball to other object distances.
 */
public class BallDistTssCalc implements ITssCalc
{
	@Override
	public TimeSeriesStatsEntry createTimeSeriesStatsEntry(final BaseAiFrame aiFrame, final ITacticalField tacticalField,
			final long timestamp)
	{
		TimeSeriesStatsEntry entry = new TimeSeriesStatsEntry("ball.dist", timestamp);
		if (!tacticalField.getTigersToBallDist().isEmpty())
		{
			entry.addField("bot.we", tacticalField.getTigersToBallDist().get(0).getDist() / 1000.0);
		}
		if (!tacticalField.getEnemiesToBallDist().isEmpty())
		{
			entry.addField("bot.they", tacticalField.getEnemiesToBallDist().get(0).getDist() / 1000.0);
		}
		IVector2 ballPos = aiFrame.getWorldFrame().getBall().getPos();
		entry.addField("goal.we", Geometry.getGoalOur().getLineSegment().distanceTo(ballPos));
		entry.addField("goal.they", Geometry.getGoalTheir().getLineSegment().distanceTo(ballPos));
		return entry;
	}
}
