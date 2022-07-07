/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.targetrater;

import edu.tigers.sumatra.ball.trajectory.IFlatBallConsultant;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.pathfinder.obstacles.MovingRobot;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.Setter;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * Generate circles from using {@link MovingRobot}s.
 */
public class MovingObstacleGen
{
	@Setter
	private double kickSpeed = RuleConstraints.getMaxBallSpeed();
	@Setter
	private double timeForBotToReact = 0.0;
	@Setter
	private double maxHorizon = Double.MAX_VALUE;
	@Setter
	private EHorizonCalculation horizonCalculation = EHorizonCalculation.DEFAULT;

	private static final HorizonCubicReductionCalculator cubicReductionCalculator = new HorizonCubicReductionCalculator();



	public List<ICircle> generateCircles(Collection<ITrackedBot> bots, IVector2 start,
			Map<BotID, Double> timeThatOpponentBotWillReactBeforeKick)
	{
		var ballConsultant = Geometry.getBallFactory().createFlatConsultant();
		return bots.stream().map(e -> getMovingHorizon(start, timeThatOpponentBotWillReactBeforeKick, ballConsultant, e))
				.collect(Collectors.toList());
	}


	public List<ICircle> generateCircles(Collection<ITrackedBot> bots, IVector2 start, double timeToKick)
	{
		return generateCircles(bots, start,
				bots.stream().collect(Collectors.toMap(ITrackedBot::getBotId, e -> timeToKick)));
	}


	private ICircle getMovingHorizon(IVector2 start, Map<BotID, Double> timeThatOpponentBotWillReactBeforeKick,
			IFlatBallConsultant ballConsultant, ITrackedBot bot)
	{
		var movingRobot = MovingRobot.fromTrackedBot(bot, maxHorizon, Geometry.getBotRadius() + Geometry.getBallRadius());
		double distBotToStart = movingRobot.getPos().distanceTo(start);
		double timeBallToBot =
				ballConsultant.getTimeForKick(distBotToStart, kickSpeed) + timeThatOpponentBotWillReactBeforeKick.get(
						bot.getBotId());
		double x = Math.max(0, timeBallToBot - timeForBotToReact);
		double horizon = switch (horizonCalculation)
				{
					case DEFAULT -> x;
					case CUBIC_REDUCTION -> cubicReductionCalculator.reduceHorizon(x);
				};
		return movingRobot.getMovingHorizon(horizon);
	}
}
