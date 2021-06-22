/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.statistics.timeseries;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.botdistance.BotDistance;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.statistics.TimeSeriesStatsEntry;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.function.Supplier;


/**
 * Time series stats calc for ball to other object distances.
 */
@RequiredArgsConstructor
public class BallDistTssCalc implements ITssCalc
{
	private final Supplier<List<BotDistance>> opponentsToBallDist;
	private final Supplier<List<BotDistance>> tigersToBallDist;


	@Override
	public TimeSeriesStatsEntry createTimeSeriesStatsEntry(final BaseAiFrame aiFrame, final long timestamp)
	{
		TimeSeriesStatsEntry entry = new TimeSeriesStatsEntry("ball.dist", timestamp);
		if (!tigersToBallDist.get().isEmpty())
		{
			entry.addField("bot.we", tigersToBallDist.get().get(0).getDist() / 1000.0);
		}
		if (!opponentsToBallDist.get().isEmpty())
		{
			entry.addField("bot.they", opponentsToBallDist.get().get(0).getDist() / 1000.0);
		}
		IVector2 ballPos = aiFrame.getWorldFrame().getBall().getPos();
		entry.addField("goal.we", Geometry.getGoalOur().getLineSegment().distanceTo(ballPos));
		entry.addField("goal.they", Geometry.getGoalTheir().getLineSegment().distanceTo(ballPos));
		return entry;
	}
}
