/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.pass.rating;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.kicking.EBallReceiveMode;
import edu.tigers.sumatra.ai.metis.kicking.Kick;
import edu.tigers.sumatra.ai.metis.kicking.Pass;
import edu.tigers.sumatra.ball.trajectory.IBallTrajectory;
import edu.tigers.sumatra.drawable.ColorPickerFactory;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.DrawablePoint;
import edu.tigers.sumatra.drawable.IColorPicker;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.line.ILineSegment;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.movingrobot.AcceleratingRobotFactory;
import edu.tigers.sumatra.movingrobot.IMovingRobot;
import edu.tigers.sumatra.movingrobot.StoppingRobotFactory;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.wp.data.ITrackedBall;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.Setter;

import java.awt.Color;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * Rate pass interception by generating moving robot circles based on robot speed and acceleration
 * and intersect them with the ball travel line at different points in time.
 * The distance that the circle covers on the ball travel line is used for the rating.
 */
public class PassInterceptionMovingRobotRater extends APassRater
{
	@Configurable(defValue = "10.0", comment = "Max horizon [s] for MovingRobots. Caps the future prediction horizon.")
	private static double maxHorizon = 10.0;

	@Configurable(defValue = "0.08", comment = "Reaction time of opponent [s] for slow robots. Predicts constant velocity in this time.")
	private static double opponentBotReactionTimeSlowRobots = 0.08;

	@Configurable(defValue = "0.0", comment = "Reaction time of opponent [s] for fast robots. Predicts constant velocity in this time.")
	private static double opponentBotReactionTimeFastRobots = 0.0;

	@Configurable(defValue = "1.0", comment = "Horizon [s] for drawing MovingRobots. Only for debugging.")
	private static double drawingHorizon = 1.0;

	@Configurable(defValue = "1.5", comment = "Factor on relative distance. Larger -> less pessimistic")
	private static double scoringFactor = 1.5;

	@Configurable(defValue = "170", comment = "Step size [mm] over the pass trajectory (should be smaller than robot width (180mm))")
	private static double stepSize = 170.0;

	@Configurable(defValue = "30", comment = "Min distance [mm] between pass line and opponent robot (in addition to robot and ball radius")
	private static double minDistToRobot = 30.0;

	@Configurable(defValue = "0.1", comment = "Chip kick score penalty")
	private static double chipKickPenalty = 0.1;

	@Configurable(defValue = "3000.0", comment = "[mm] max dist to pass source to consider own bots")
	private static double maxDistToConsiderOwnBots = 3000.0;

	@Configurable(defValue = "1.0", comment = "Velocity [m/s] of slow robots for lower scoring factor")
	private static double slowRobotVel = 1.0;

	@Configurable(defValue = "1.0", comment = "Acceleration [m/s^2] of slow robots for lower scoring factor")
	private static double slowRobotAcc = 1.0;

	@Configurable(defValue = "2.5", comment = "Break Acceleration [m/s^2] of slow robots for lower scoring factor")
	private static double slowRobotBrkLimit = 2.5;

	@Configurable(defValue = "0.5", comment = "Max time [s] to subtract from tHorizon to penalize opponent behind receiver")
	private static double maxPenaltyTimeOffsetOpponentBehindReceiver = 0.5;

	@Configurable(defValue = "true", comment = "Consider pass preparation time as additional horizon time")
	private static boolean considerPassPreparationTime = true;

	static
	{
		ConfigRegistration.registerClass("metis", PassInterceptionMovingRobotRater.class);
	}

	private final Collection<ITrackedBot> consideredBots;
	private final Map<BotID, IMovingRobot> movingRobotsFast;
	private final Map<BotID, IMovingRobot> movingRobotsSlow;
	private final IColorPicker colorPicker = ColorPickerFactory.greenRedGradient();
	private final double distToRobot = Geometry.getBotRadius() + Geometry.getBallRadius() + minDistToRobot;

	@Setter
	private double scoringFactorOffset = 0.0;

	@Setter
	private double robotMovementLimitFactor = 1.0;


	public PassInterceptionMovingRobotRater(Collection<ITrackedBot> consideredBots)
	{
		this.consideredBots = consideredBots;

		movingRobotsFast = consideredBots.stream().collect(
				Collectors.toMap(ITrackedBot::getBotId, bot ->
						AcceleratingRobotFactory.create(
								bot.getPos(),
								bot.getVel(),
								bot.getRobotInfo().getBotParams().getMovementLimits().getVelMax() * robotMovementLimitFactor,
								bot.getRobotInfo().getBotParams().getMovementLimits().getAccMax() * robotMovementLimitFactor,
								distToRobot,
								opponentBotReactionTimeFastRobots
						)
				));
		movingRobotsSlow = consideredBots.stream().collect(
				Collectors.toMap(ITrackedBot::getBotId, tBot ->
						StoppingRobotFactory.create(
								tBot.getPos(),
								tBot.getVel(),
								slowRobotVel * robotMovementLimitFactor,
								slowRobotAcc * robotMovementLimitFactor,
								slowRobotBrkLimit * robotMovementLimitFactor,
								distToRobot,
								opponentBotReactionTimeSlowRobots
						)
				));
	}


	@Override
	public void drawShapes(List<IDrawableShape> shapes)
	{
		movingRobotsFast.values()
				.stream()
				.map(mr -> movingRobotShapes(mr, Color.darkGray))
				.flatMap(List::stream)
				.forEach(shapes::add);
		movingRobotsSlow.values()
				.stream()
				.map(mr -> movingRobotShapes(mr, Color.magenta))
				.flatMap(List::stream)
				.forEach(shapes::add);
	}


	private List<IDrawableShape> movingRobotShapes(IMovingRobot movingRobot, Color color)
	{
		ICircle movingHorizon = movingRobot.getMovingHorizon(drawingHorizon);
		return List.of(
				new DrawableCircle(movingHorizon, color),
				new DrawableLine(Lines.segmentFromPoints(movingRobot.getPos(), movingHorizon.center()), color)
		);
	}


	public double rateRollingBall(ITrackedBall ball, double consideredTimeHorizon)
	{
		var ballVel = ball.getVel3();
		var ballVelLength = ballVel.getLength();
		var endPoint = ball.getTrajectory().getPosByTime(consideredTimeHorizon).getXYVector();

		var kick = Kick.builder()
				.source(ball.getPos())
				.target(endPoint)
				.kickParams(SumatraMath.isZero(ball.getVel3().z()) ?
						KickParams.straight(ballVelLength) :
						KickParams.chip(ballVelLength))
				.kickVel(ballVel)
				.aimingTolerance(0)
				.build();
		var pass = new Pass(
				kick,
				BotID.noBot(),
				BotID.noBot(),
				0,
				consideredTimeHorizon,
				0,
				EBallReceiveMode.DONT_CARE
		);

		return rateInternal(pass, ball.getTrajectory());
	}


	@Override
	public double rate(Pass pass)
	{
		var origin = pass.getKick().getSource();
		var velMM = pass.getKick().getKickVel().multiplyNew(1000);
		var passTrajectory = Geometry.getBallFactory().createTrajectoryFromKickedBallWithoutSpin(origin, velMM);
		return rateInternal(pass, passTrajectory);
	}


	private double rateInternal(Pass pass, IBallTrajectory passTrajectory)
	{
		var posIterator = passTrajectory.getTravelLinesInterceptable().stream()
				.map(l -> l.getSteps(stepSize))
				.flatMap(Collection::stream)
				// Only the keeper can intercept passes in the opponent penalty area.
				// However, it is quite dangerous for the keeper to intercept the ball in the penalty area.
				// Therefore, we skip points in the opponent penArea.
				.filter(pos -> !Geometry.getPenaltyAreaTheir().isPointInShape(pos))
				.iterator();

		var passLine = Lines.segmentFromPoints(pass.getKick().getSource(), pass.getKick().getTarget());
		var bots = consideredBots.stream()
				// Ignore shooter and receiver
				.filter(bot -> !bot.getBotId().equals(pass.getShooter()) && !bot.getBotId().equals(pass.getReceiver()))
				// Ignore bots that are too far away
				.filter(bot -> considerBot(pass, bot, passLine))
				.toList();

		bots.forEach(bot -> draw(() -> new DrawableCircle(bot.getPos(), 100, Color.cyan)));

		double minScore = 1;
		while (posIterator.hasNext())
		{
			var pos = posIterator.next();
			var ballTravelTime = passTrajectory.getTimeByPos(pos);
			if (ballTravelTime > pass.getDuration())
			{
				// Only until the ball reaches the receiver
				break;
			}
			var score = ratePos(pass, pos, ballTravelTime, bots);
			draw(() -> new DrawablePoint(pos).setColor(colorPicker.getColor(score)));
			draw(() -> new DrawableAnnotation(pos, String.format("%.1f", ballTravelTime)).withOffsetX(50));
			if (score <= 0)
			{
				return 0;
			}
			if (score < minScore)
			{
				minScore = score;
			}
		}

		return minScore;
	}


	private boolean considerBot(Pass pass, ITrackedBot bot, ILineSegment passLine)
	{
		if (bot.getBotId().getTeamColor() == pass.getShooter().getTeamColor())
		{
			// own bots are only considered near source
			return pass.getKick().getSource().distanceTo(bot.getPos()) < maxDistToConsiderOwnBots;
		}

		IMovingRobot movingRobot = movingRobotsFast.get(bot.getBotId());
		double t = Math.min(maxHorizon, pass.getDuration() + pass.getPreparationTime());
		ICircle movingHorizon = movingRobot.getMovingHorizon(t);
		return passLine.distanceTo(movingHorizon.center()) < movingHorizon.radius() + minDistToRobot;
	}


	private double ratePos(Pass pass, IVector2 pos, double ballTravelTime, List<ITrackedBot> bots)
	{
		var scorePenalty = pass.isChip() ? chipKickPenalty : 0;
		var minScore = 1.0 - scorePenalty;
		for (var bot : bots)
		{
			double score = ratePosForBot(pass, pos, ballTravelTime, bot) - scorePenalty;
			if (score <= 0)
			{
				return 0;
			}
			if (score < minScore)
			{
				minScore = score;
			}
		}
		return minScore;
	}


	private double ratePosForBot(Pass pass, IVector2 pos, double ballTravelTime, ITrackedBot bot)
	{
		var tHorizon = Math.min(maxHorizon, ballTravelTime + pass.getPreparationTime());

		if (bot.getBotId().getTeamColor() == pass.getShooter().getTeamColor())
		{
			// own bot
			return bot.getPosByTime(tHorizon).distanceTo(pos) > distToRobot ? 1 : 0;
		}

		var opponentBehindReceiverPenaltyTime = getOpponentBehindReceiverPenaltyTime(pass, bot);

		double tAdditionalReaction = considerPassPreparationTime ? pass.getPreparationTime() : 0;
		return ratePosForOpponentBot(pos, bot, tHorizon - opponentBehindReceiverPenaltyTime + tAdditionalReaction, 0);
	}


	private double getOpponentBehindReceiverPenaltyTime(Pass pass, ITrackedBot bot)
	{
		var targetToBot = bot.getPos().subtractNew(pass.getKick().getTarget());
		var sourceToTarget = pass.getKick().getTarget().subtractNew(pass.getKick().getSource());
		return maxPenaltyTimeOffsetOpponentBehindReceiver * SumatraMath.relative(
				targetToBot.angleToAbs(sourceToTarget).orElse(0.0),
				AngleMath.DEG_090_IN_RAD,
				0
		);
	}


	private double ratePosForOpponentBot(IVector2 pos, ITrackedBot bot, double tHorizon, double tAdditionalReaction)
	{
		var movingHorizonFast = movingRobotsFast.get(bot.getBotId()).getMovingHorizon(tHorizon, tAdditionalReaction);
		var fastDist = movingHorizonFast.center().distanceTo(pos);
		if (fastDist > movingHorizonFast.radius())
		{
			return 1;
		}

		var movingHorizonSlow = movingRobotsSlow.get(bot.getBotId()).getMovingHorizon(tHorizon, tAdditionalReaction);
		var slowDist = movingHorizonSlow.center().distanceTo(pos);
		if (slowDist < movingHorizonSlow.radius())
		{
			return 0;
		}
		double range = movingHorizonFast.radius() - movingHorizonSlow.radius();
		if (range <= 0)
		{
			return slowDist > distToRobot ? 1 : 0;
		}
		return Math.min(1,
				((slowDist - movingHorizonSlow.radius()) / range) * Math.max(0, scoringFactor + scoringFactorOffset));
	}
}
