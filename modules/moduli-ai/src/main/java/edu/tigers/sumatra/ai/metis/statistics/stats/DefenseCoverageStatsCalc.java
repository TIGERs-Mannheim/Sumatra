/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.statistics.stats;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.botdistance.BotDistance;
import edu.tigers.sumatra.ai.metis.targetrater.AngleRange;
import edu.tigers.sumatra.ai.metis.targetrater.AngleRangeGenerator;
import edu.tigers.sumatra.ai.metis.targetrater.MovingObstacleGen;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.Vector2;
import lombok.RequiredArgsConstructor;

import java.util.function.Supplier;


/**
 * Calculate the average coverage angle on the own goal
 */
@RequiredArgsConstructor
public class DefenseCoverageStatsCalc extends AStatsCalc
{
	@Configurable(comment = "Max time horizon to consider for moving robots", defValue = "0.1")
	private static double maxHorizon = 0.1;

	static
	{
		ConfigRegistration.registerClass("metis", DefenseCoverageStatsCalc.class);
	}

	private final Supplier<BotDistance> opponentClosestToBall;

	private final MovingObstacleGen movingObstacleGen = new MovingObstacleGen();

	private MovingAverage overallAverage = new MovingAverage();
	private MovingAverage attackerNearBallAverage = new MovingAverage();

	private double uncoveredRangeDeg;


	@Override
	public void saveStatsToMatchStatistics(final MatchStats matchStatistics)
	{
		matchStatistics.putStatisticData(EMatchStatistics.DEFENSE_COVERAGE,
				new StatisticData(uncoveredRangeDeg));
		matchStatistics.putStatisticData(EMatchStatistics.DEFENSE_COVERAGE_OVERALL,
				new StatisticData(overallAverage.getCombinedValue()));
		matchStatistics.putStatisticData(EMatchStatistics.DEFENSE_COVERAGE_ATTACKER_NEAR_BALL,
				new StatisticData(attackerNearBallAverage.getCombinedValue()));
	}


	@Override
	public void onStatisticUpdate(final BaseAiFrame baseAiFrame)
	{
		AngleRangeGenerator angleRangeGenerator = AngleRangeGenerator.forGoal(Geometry.getGoalOur());
		movingObstacleGen.setMaxHorizon(maxHorizon);
		var start = baseAiFrame.getWorldFrame().getBall().getPos();
		var obstacles = movingObstacleGen
				.generateCircles(baseAiFrame.getWorldFrame().getTigerBotsVisible().values(), start, 0.0);

		double uncoveredRange = angleRangeGenerator.findUncoveredAngleRanges(start, obstacles).stream()
				.mapToDouble(AngleRange::getWidth).sum();

		uncoveredRangeDeg = AngleMath.rad2deg(uncoveredRange);
		int value = (int) Math.round(uncoveredRangeDeg);
		overallAverage.add(value);

		if (opponentClosestToBall.get().getDist() < Geometry.getBotRadius() + 100)
		{
			attackerNearBallAverage.add(value);
		}

		baseAiFrame.getShapes(EAiShapesLayer.DEFENSE_COVERAGE)
				.add(new DrawableAnnotation(Vector2.fromXY(-1000, 0), String.format("%.1f", uncoveredRangeDeg)));
	}
}
