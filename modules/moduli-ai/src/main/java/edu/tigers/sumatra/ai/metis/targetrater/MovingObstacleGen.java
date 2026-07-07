package edu.tigers.sumatra.ai.metis.targetrater;

import edu.tigers.sumatra.ball.trajectory.IFlatBallConsultant;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.movingrobot.IMovingRobot;
import edu.tigers.sumatra.movingrobot.MovingRobotFactory;
import edu.tigers.sumatra.movingrobot.MovingRobotParams;
import edu.tigers.sumatra.pathfinder.TrajectoryGenerator;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.Setter;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * Generate circles from using {@link IMovingRobot}s.
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
	private Map<BotID, IVector2> customMovementDestinations = Map.of();

	@Setter
	private double currentToMaxSpeedFactorForCustomMovementDestinations = 1.1;


	public List<ICircle> generateCircles(
			Collection<ITrackedBot> bots, IVector2 start,
			Map<BotID, Double> reactionTimeBotHasBeforeKick
	)
	{
		var ballConsultant = Geometry.getBallFactory().createFlatConsultant();
		return bots.stream().map(e -> getMovingHorizon(start, reactionTimeBotHasBeforeKick, ballConsultant, e))
				.toList();
	}


	public List<ICircle> generateCircles(Collection<ITrackedBot> bots, IVector2 start, double timeToKick)
	{
		return generateCircles(
				bots, start,
				bots.stream().collect(Collectors.toMap(ITrackedBot::getBotId, e -> timeToKick))
		);
	}


	private ICircle getMovingHorizon(
			IVector2 start, Map<BotID, Double> reactionTimeBotHasBeforeKick,
			IFlatBallConsultant ballConsultant, ITrackedBot bot
	)
	{
		var timeBeforeKick = reactionTimeBotHasBeforeKick.get(bot.getBotId());
		var deadTimeAfterKick = timeForBotToReact;
		var reactionTimestamp = timeBeforeKick + deadTimeAfterKick;
		IVector2 reactionPos;
		IVector2 reactionVel;

		if(bot.getCurrentTrajectory().isPresent() || !customMovementDestinations.containsKey(bot.getBotId()))
		{
			reactionPos = bot.getPosByTime(reactionTimestamp);
			reactionVel = bot.getVelByTime(reactionTimestamp);
		}
		else {
			var current = bot.getMoveConstraints();
			var newVelMax = SumatraMath.cap(
					bot.getVel().getLength() * currentToMaxSpeedFactorForCustomMovementDestinations,
					0.1,
					current.getVelMax()
			);
			var moveConstraints = current.withVelMax(newVelMax);

			var traj = TrajectoryGenerator.generatePositionTrajectory(
					bot,
					customMovementDestinations.get(bot.getBotId()),
					moveConstraints
			);
			reactionPos = traj.getPositionMM(reactionTimestamp);
			reactionVel = traj.getVelocity(reactionTimestamp);
		}


		var movingRobot = MovingRobotFactory.acceleratingRobot(
				MovingRobotParams.builder()
						.position(reactionPos)
						.velocity(reactionVel)
						.vLimit(bot.getRobotInfo().getBotParams().getMovementLimits().getVelMax())
						.aLimit(bot.getRobotInfo().getBotParams().getMovementLimits().getAccMax())
						.brkLimit(bot.getRobotInfo().getBotParams().getMovementLimits().getAccMax())
						.radius(Geometry.getBotRadius() + Geometry.getBallRadius())
						.reactionTime(0)
						.build()
		);

		var reactionDistance = reactionPos.distanceTo(start);
		var reactionTimeAfterKick = Math.max(
				0,
				noneOptimalDriveFactor * ballConsultant.getTimeForKick(reactionDistance, kickSpeed)
						+ timeBeforeReactionUsageFactor * reactionTimestamp
						- timeForBotToReact
		);
		double horizon = reactionTimeAfterKick;
		var t = bot.getCurrentTrajectory().isPresent() ? 0 : Math.min(maxHorizon, horizon);

		return movingRobot.getMovingHorizon(t);
	}
}
