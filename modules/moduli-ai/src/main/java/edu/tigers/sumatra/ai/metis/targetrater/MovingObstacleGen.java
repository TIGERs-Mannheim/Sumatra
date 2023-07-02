/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
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
	private double noneOptimalDriveFactor = 1.0;

	@Setter
	private double timeBeforeReactionUsageFactor = 0.0;

	@Setter
	private double maxHorizon = Double.MAX_VALUE;

	@Setter
	private EHorizonCalculation horizonCalculation = EHorizonCalculation.DEFAULT;

	private static final HorizonCubicReductionCalculator cubicReductionCalculator = new HorizonCubicReductionCalculator();


	public List<ICircle> generateCircles(Collection<ITrackedBot> bots, IVector2 start,
			Map<BotID, Double> reactionTimeBotHasBeforeKick)
	{
		var ballConsultant = Geometry.getBallFactory().createFlatConsultant();
		return bots.stream().map(e -> getMovingHorizon(start, reactionTimeBotHasBeforeKick, ballConsultant, e))
				.toList();
	}


	public List<ICircle> generateCircles(Collection<ITrackedBot> bots, IVector2 start, double timeToKick)
	{
		return generateCircles(bots, start,
				bots.stream().collect(Collectors.toMap(ITrackedBot::getBotId, e -> timeToKick)));
	}


	private ICircle getMovingHorizon(IVector2 start, Map<BotID, Double> reactionTimeBotHasBeforeKick,
			IFlatBallConsultant ballConsultant, ITrackedBot bot)
	{
		var timeBeforeKick = reactionTimeBotHasBeforeKick.get(bot.getBotId());
		var deadTimeAfterKick = timeForBotToReact;
		var reactionTimestamp = timeBeforeKick + deadTimeAfterKick;
		var reactionPos = bot.getPosByTime(reactionTimestamp);
		var reactionVel = bot.getVelByTime(reactionTimestamp);


		var movingRobot = new MovingRobot(
				reactionPos,
				reactionVel,
				bot.getRobotInfo().getBotParams().getMovementLimits().getVelMax(),
				bot.getRobotInfo().getBotParams().getMovementLimits().getAccMax(),
				maxHorizon,
				Geometry.getBotRadius() + Geometry.getBallRadius()
		);

		var reactionDistance = reactionPos.distanceTo(start);
		var reactionTimeAfterKick = Math.max(0,
				noneOptimalDriveFactor * ballConsultant.getTimeForKick(reactionDistance, kickSpeed)
						+ timeBeforeReactionUsageFactor * reactionTimestamp
						- timeForBotToReact);
		var horizon = switch (horizonCalculation)
				{
					case DEFAULT -> reactionTimeAfterKick;
					case CUBIC_REDUCTION -> cubicReductionCalculator.reduceHorizon(reactionTimeAfterKick);
				};
		return movingRobot.getMovingHorizon(horizon);
	}
}
