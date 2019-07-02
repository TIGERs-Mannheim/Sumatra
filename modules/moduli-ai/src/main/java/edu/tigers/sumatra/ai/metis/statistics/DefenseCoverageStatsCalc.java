package edu.tigers.sumatra.ai.metis.statistics;

import java.util.HashMap;
import java.util.Map;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.ai.metis.targetrater.AngleRange;
import edu.tigers.sumatra.ai.metis.targetrater.AngleRangeGenerator;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.pathfinder.obstacles.MovingRobot;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * Calculate the average coverage angle on the own goal
 */
public class DefenseCoverageStatsCalc extends AStatsCalc
{
	@Configurable(comment = "Max time horizon to consider for moving robots", defValue = "0.1")
	private static double maxHorizon = 0.1;

	static
	{
		ConfigRegistration.registerClass("metis", DefenseCoverageStatsCalc.class);
	}

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
	public void onStatisticUpdate(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		AngleRangeGenerator angleRangeGenerator = createAngleRangeGenerator(baseAiFrame);

		double uncoveredRange = angleRangeGenerator.findUncoveredAngleRanges().stream()
				.mapToDouble(AngleRange::getAngleWidth).sum();

		uncoveredRangeDeg = AngleMath.rad2deg(uncoveredRange);
		int value = (int) Math.round(uncoveredRangeDeg);
		overallAverage.add(value);

		if (newTacticalField.getEnemiesToBallDist().stream().anyMatch(d -> d.getDist() < Geometry.getBotRadius() + 100))
		{
			attackerNearBallAverage.add(value);
		}

		newTacticalField.getDrawableShapes().get(EAiShapesLayer.DEFENSE_COVERAGE)
				.add(new DrawableAnnotation(Vector2.fromXY(-1000, 0), String.format("%.1f", uncoveredRangeDeg)));
	}


	private AngleRangeGenerator createAngleRangeGenerator(final BaseAiFrame baseAiFrame)
	{
		AngleRangeGenerator angleRangeGenerator = new AngleRangeGenerator();
		angleRangeGenerator.setExtendTriangle(true);
		angleRangeGenerator.setEndLeft(Geometry.getGoalOur().getLeftPost());
		angleRangeGenerator.setEndRight(Geometry.getGoalOur().getRightPost());
		angleRangeGenerator.setStart(baseAiFrame.getWorldFrame().getBall().getPos());
		angleRangeGenerator.setKickSpeed(RuleConstraints.getMaxBallSpeed());
		angleRangeGenerator.setBallConsultant(baseAiFrame.getWorldFrame().getBall().getStraightConsultant());

		Map<BotID, MovingRobot> movingRobots = new HashMap<>();
		for (ITrackedBot bot : baseAiFrame.getWorldFrame().getTigerBotsVisible().values())
		{
			MovingRobot movingRobot = new MovingRobot(bot, maxHorizon, Geometry.getBotRadius() + Geometry.getBallRadius());
			movingRobots.put(bot.getBotId(), movingRobot);
		}
		angleRangeGenerator.setMovingRobots(movingRobots);
		return angleRangeGenerator;
	}
}
