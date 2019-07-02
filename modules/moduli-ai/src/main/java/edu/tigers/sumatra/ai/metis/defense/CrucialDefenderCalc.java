package edu.tigers.sumatra.ai.metis.defense;

import java.awt.Color;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.botdistance.BotDistance;
import edu.tigers.sumatra.drawable.DrawableArc;
import edu.tigers.sumatra.drawable.animated.AnimatedCircle;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.Hysteresis;
import edu.tigers.sumatra.math.circle.Arc;
import edu.tigers.sumatra.math.circle.IArc;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * Calculates crucial defenders.
 */
public class CrucialDefenderCalc extends ACalculator
{
	@Configurable(comment = "Radius, in which an opponent next to a ball is considered very dangerous regardless of tiger bots", defValue = "200.0")
	private static double dangerousRadius = 200.0;

	@Configurable(comment = "Hysteresis for bot to ball distance comparison", defValue = "200.0")
	private static double opponentToBallHysteresis = 200.0;

	@Configurable(comment = "Lower minimum distance from ball to goal center to actually assign crucial defenders", defValue = "4000.0")
	private static double minDistanceToGoalCenterLower = 4000.0;
	@Configurable(comment = "Upper minimum distance from ball to goal center to actually assign crucial defenders", defValue = "5000.0")
	private static double minDistanceToGoalCenterUpper = 5000.0;


	private final DesiredDefendersCalcUtil util = new DesiredDefendersCalcUtil();
	private final Hysteresis minDistanceToGoalHysteresis = new Hysteresis(minDistanceToGoalCenterLower,
			minDistanceToGoalCenterUpper);

	private boolean enemyCloseToBall = false;


	@Override
	protected void doCalc()
	{
		util.update(getAiFrame());
		minDistanceToGoalHysteresis.setLowerThreshold(minDistanceToGoalCenterLower);
		minDistanceToGoalHysteresis.setUpperThreshold(minDistanceToGoalCenterUpper);
		minDistanceToGoalHysteresis.update(Geometry.getGoalOur().getCenter().distanceTo(getBall().getPos()));

		drawMinDistanceToGoal();

		if (!getNewTacticalField().getGameState().isStandardSituationForThem() // force crucial defenders for standards
				&& (!isEnemyCloseToBall() || minDistanceToGoalHysteresis.isUpper()))
		{
			return;
		}

		final List<BotID> defenderCandidates = crucialDefenderCandidates();
		final Set<BotID> crucialDefenders = util.nextBestDefenders(
				getNewTacticalField().getDefenseBallThreat(),
				defenderCandidates,
				getNewTacticalField().getNumDefenderForBall());

		getNewTacticalField().setCrucialDefender(crucialDefenders);
		drawCrucialDefenders(crucialDefenders);
	}


	private void drawMinDistanceToGoal()
	{
		double radius = minDistanceToGoalHysteresis.isLower() ? minDistanceToGoalCenterUpper
				: minDistanceToGoalCenterLower;
		IArc arc = Arc.createArc(Geometry.getGoalOur().getCenter(), radius, -AngleMath.DEG_090_IN_RAD,
				AngleMath.DEG_180_IN_RAD);
		final DrawableArc drawableArc = new DrawableArc(arc, new Color(255, 0, 0, 80));
		drawableArc.setFill(true);
		getNewTacticalField().getDrawableShapes().get(EAiShapesLayer.DEFENSE_CRUCIAL_DEFENDERS).add(drawableArc);
	}


	private List<BotID> crucialDefenderCandidates()
	{
		return getAiFrame().getWorldFrame().getTigerBotsAvailable().keySet().stream()
				.filter(bot -> !getAiFrame().getKeeperId().equals(bot))
				.filter(bot -> !getNewTacticalField().getCrucialOffender().contains(bot))
				.filter(bot -> !getNewTacticalField().getBotInterchange().getDesiredInterchangeBots().contains(bot))
				.collect(Collectors.toList());
	}


	private boolean isEnemyCloseToBall()
	{
		final List<BotDistance> tigersToBallDist = getNewTacticalField().getTigersToBallDist();
		final List<BotDistance> enemiesToBallDist = getNewTacticalField().getEnemiesToBallDist();
		if (enemiesToBallDist.isEmpty() || tigersToBallDist.isEmpty())
		{
			return false;
		}
		double hysteresis = enemyCloseToBall ? opponentToBallHysteresis : 0;
		double minOur = tigersToBallDist.get(0).getDist();
		double minTheir = enemiesToBallDist.get(0).getDist();
		enemyCloseToBall = minTheir < minOur + hysteresis || minTheir < dangerousRadius + hysteresis;
		return enemyCloseToBall;
	}


	private void drawCrucialDefenders(final Set<BotID> desiredDefenders)
	{
		for (BotID id : desiredDefenders)
		{
			ITrackedBot bot = getWFrame().getBot(id);
			getNewTacticalField().getDrawableShapes().get(EAiShapesLayer.DEFENSE_CRUCIAL_DEFENDERS).add(
					AnimatedCircle.aFilledCircleWithShrinkingSize(bot.getPos(), 100, 150, 1.0f, new Color(125, 255, 50),
							new Color(125, 255, 50, 100)));
		}
	}
}
