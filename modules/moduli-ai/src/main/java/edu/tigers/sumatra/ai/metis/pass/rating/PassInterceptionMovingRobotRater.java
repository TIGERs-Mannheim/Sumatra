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
import edu.tigers.sumatra.math.StatisticsMath;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.line.ILineSegment;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.movingrobot.IMovingRobot;
import edu.tigers.sumatra.movingrobot.MovingRobotFactory;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.wp.data.ITrackedBall;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.exception.TooManyIterationsException;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.MaxIter;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.univariate.BrentOptimizer;
import org.apache.commons.math3.optim.univariate.SearchInterval;
import org.apache.commons.math3.optim.univariate.UnivariateObjectiveFunction;
import org.apache.commons.math3.optim.univariate.UnivariateOptimizer;
import org.apache.commons.math3.optim.univariate.UnivariatePointValuePair;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Rate pass interception by generating moving robot circles based on robot speed and acceleration
 * and intersect them with the ball travel line at different points in time.
 * The distance that the circle covers on the ball travel line is used for the rating.
 */
@Log4j2
public class PassInterceptionMovingRobotRater extends APassRater
{
	@Configurable(defValue = "0.08", comment = "Reaction time of opponent [s] for slow robots. Predicts constant velocity in this time.")
	private static double opponentBotReactionTimeSlowRobots = 0.08;

	@Configurable(defValue = "0.0", comment = "Reaction time of opponent [s] for fast robots. Predicts constant velocity in this time.")
	private static double opponentBotReactionTimeFastRobots = 0.0;

	@Configurable(defValue = "0.5;1.0", comment = "Horizons [s] for drawing MovingRobots. Only for debugging.")
	private static Double[] drawingHorizons = new Double[] { 0.5, 1.0 };

	@Configurable(defValue = "0;1", comment = "Draw movingRobots for these bot ids only")
	private static Integer[] drawShapesForBotIds = new Integer[] { 0, 1 };

	@Configurable(defValue = "1.0", comment = "Factor on relative distance. Larger -> less pessimistic")
	private static double scoringFactor = 1.0;

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

	@Configurable(defValue = "true", comment = "Consider pass preparation time as additional horizon time")
	private static boolean considerPassPreparationTime = true;

	@Configurable(defValue = "SMALLEST", comment = "Determine how the sums of each bot are combined")
	private static RateCombinationType rateCombinationType = RateCombinationType.SMALLEST;

	static
	{
		ConfigRegistration.registerClass("metis", PassInterceptionMovingRobotRater.class);
	}

	private final Collection<ITrackedBot> consideredBots;
	private final List<BotID> opponentMan2ManMarkers;
	private final Map<BotID, IMovingRobot> movingRobotsFast;
	private final Map<BotID, IMovingRobot> movingRobotsSlow;
	private final IColorPicker colorPicker = ColorPickerFactory.greenRedGradient();
	private final double distToRobot = Geometry.getBotRadius() + Geometry.getBallRadius() + minDistToRobot;

	@Setter
	private double scoringFactorOffset = 0.0;

	@Setter
	private double robotMovementLimitFactor = 1.0;


	public PassInterceptionMovingRobotRater(Collection<ITrackedBot> consideredBots, List<BotID> opponentMan2ManMarkers)
	{
		this.opponentMan2ManMarkers = opponentMan2ManMarkers;
		this.consideredBots = consideredBots;

		movingRobotsFast = consideredBots.stream().collect(
				Collectors.toMap(
						ITrackedBot::getBotId, bot ->
								MovingRobotFactory.acceleratingRobot(
										bot.getPos(),
										bot.getVel(),
										bot.getRobotInfo().getBotParams().getMovementLimits().getVelMax()
												* robotMovementLimitFactor,
										bot.getRobotInfo().getBotParams().getMovementLimits().getAccMax()
												* robotMovementLimitFactor,
										distToRobot,
										opponentBotReactionTimeFastRobots
								)
				));
		movingRobotsSlow = consideredBots.stream().collect(
				Collectors.toMap(
						ITrackedBot::getBotId, tBot ->
								MovingRobotFactory.stoppingRobot(
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


	private List<IDrawableShape> drawShapes(IBallTrajectory passTrajectory)
	{
		List<IDrawableShape> shapes = new ArrayList<>();
		int colorIdx = 0;
		for (double drawingHorizon : drawingHorizons)
		{
			Color colorFast = ColorPickerFactory.getDistinctColor(colorIdx++);
			Color colorSlow = ColorPickerFactory.getDistinctColor(colorIdx++);
			movingRobotsFast.entrySet()
					.stream()
					.filter(e -> Arrays.stream(drawShapesForBotIds).toList().contains(e.getKey().getNumber()))
					.map(Map.Entry::getValue)
					.map(mr -> movingRobotShapes(mr, passTrajectory, colorFast, drawingHorizon))
					.flatMap(List::stream)
					.forEach(shapes::add);
			movingRobotsSlow.entrySet()
					.stream()
					.filter(e -> Arrays.stream(drawShapesForBotIds).toList().contains(e.getKey().getNumber()))
					.map(Map.Entry::getValue)
					.map(mr -> movingRobotShapes(mr, passTrajectory, colorSlow, drawingHorizon))
					.flatMap(List::stream)
					.forEach(shapes::add);
		}
		return shapes;
	}


	private List<IDrawableShape> movingRobotShapes(
			IMovingRobot movingRobot,
			IBallTrajectory passTrajectory,
			Color color,
			double drawingHorizon
	)
	{
		ICircle movingHorizon = movingRobot.getMovingHorizon(drawingHorizon);
		IVector2 pos = passTrajectory.getPosByTime(drawingHorizon).getXYVector();
		return List.of(
				new DrawableCircle(movingHorizon, color),
				new DrawableLine(Lines.segmentFromPoints(pos, movingHorizon.center()), color)
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
		drawMany(() -> drawShapes(passTrajectory));
		return SumatraMath.cap(rateInternal(pass, passTrajectory), 0, 1);
	}


	private Stream<ILineSegment> removePenArea(ILineSegment segment)
	{
		// ensure that start and end are inside the field
		// this simplifies the following calculation with the penArea.
		var adaptedSegment = Lines.segmentFromPoints(
				Geometry.getField().nearestPointInside(segment.getPathStart(), segment.getPathEnd()),
				Geometry.getField().nearestPointInside(segment.getPathEnd(), segment.getPathStart())
		);
		var penArea = Geometry.getPenaltyAreaTheir();

		boolean startInside = penArea.isPointInShape(adaptedSegment.getPathStart());
		boolean endInside = penArea.isPointInShape(adaptedSegment.getPathEnd());
		if (startInside && endInside)
		{
			// segment is completely inside penArea
			return Stream.empty();
		}

		var intersections = penArea.intersectPerimeterPath(adaptedSegment);
		if (intersections.isEmpty())
		{
			// segment is completely outside penArea
			return Stream.of(adaptedSegment);
		}

		List<ILineSegment> segments = new ArrayList<>();
		if (!startInside)
		{
			var intersection = adaptedSegment.getPathStart().nearestTo(intersections);
			segments.add(Lines.segmentFromPoints(adaptedSegment.getPathStart(), intersection));
		}

		if (!endInside)
		{
			var intersection = adaptedSegment.getPathEnd().nearestTo(intersections);
			segments.add(Lines.segmentFromPoints(intersection, adaptedSegment.getPathEnd()));
		}

		return segments.stream();
	}


	private List<TimeRange> getTimeRanges(IBallTrajectory passTrajectory, Pass pass)
	{
		double timeOffset = (Geometry.getBotRadius() / 1000) / pass.getReceivingSpeed();
		return passTrajectory.getTravelLinesInterceptableByRobot().stream()
				.flatMap(this::removePenArea)
				.map(segment -> new TimeRange(
						passTrajectory.getTimeByPos(segment.getPathStart()),
						Math.min(passTrajectory.getTimeByPos(segment.getPathEnd()), pass.getDuration())
				))
				.filter(range -> range.min() < pass.getDuration() - timeOffset)
				.filter(range -> Math.abs(range.max() - range.min()) > 0.01)
				.toList();
	}


	private double rateInternal(Pass pass, IBallTrajectory passTrajectory)
	{
		List<TimeRange> timeRanges = getTimeRanges(passTrajectory, pass);
		var passLine = Lines.segmentFromPoints(pass.getKick().getSource(), pass.getKick().getTarget());
		var bots = consideredBots.stream()
				// Ignore shooter and receiver
				.filter(bot -> !bot.getBotId().equals(pass.getShooter()) && !bot.getBotId().equals(pass.getReceiver()))
				// Ignore bots that are too far away
				.filter(bot -> considerBot(pass, bot, passLine))
				// Nearest bots first
				.sorted(Comparator.comparingDouble(bot -> bot.getPos().distanceTo(pass.getKick().getSource())))
				.toList();

		List<Double> scores = new ArrayList<>();
		for (ITrackedBot bot : bots)
		{
			IVector2 closestPointOnPassLine = passLine.closestPointOnPath(bot.getPos());
			double timeToClosestPoint = passTrajectory.getTimeByPos(closestPointOnPassLine);
			for (TimeRange timeRange : timeRanges)
			{
				var score = new InterceptionOptimizer(passTrajectory, pass, bot)
						.minimizeScore(timeRange, timeToClosestPoint);
				if (score <= 0)
				{
					return 0;
				}
				scores.add(score);
			}
		}

		if (scores.isEmpty())
		{
			return 1;
		}

		var scorePenalty = pass.isChip() ? chipKickPenalty : 0;

		var score = switch (rateCombinationType)
		{
			case SMALLEST -> scores.stream().mapToDouble(s -> s).min().orElse(1.0);
			case RECIPROCAL_SUM -> 1 / scores.stream().mapToDouble(s -> s).map(s -> 1 / s).sum();
			case COUNTER_PROBABILITY -> StatisticsMath.anyOccurs(scores);
		};

		return score - scorePenalty;
	}


	private boolean considerBot(Pass pass, ITrackedBot bot, ILineSegment passLine)
	{
		if (bot.getBotId().getTeamColor() == pass.getShooter().getTeamColor())
		{
			// own bots are only considered near source
			return pass.getKick().getSource().distanceTo(bot.getPos()) < maxDistToConsiderOwnBots;
		}

		IMovingRobot movingRobot = movingRobotsFast.get(bot.getBotId());
		double preparationTime = considerPassPreparationTime ? pass.getPreparationTime() : 0;
		double t = pass.getDuration() + preparationTime;
		ICircle movingHorizon = movingRobot.getMovingHorizon(t);
		return passLine.distanceTo(movingHorizon.center()) < movingHorizon.radius() + minDistToRobot;
	}


	private enum RateCombinationType
	{
		SMALLEST,
		RECIPROCAL_SUM,
		COUNTER_PROBABILITY,
	}

	private record TimeRange(double min, double max)
	{
	}

	private class InterceptionOptimizer
	{
		private final IBallTrajectory ballTrajectory;
		private final Pass pass;
		private final ITrackedBot bot;
		private final UnivariateOptimizer optimizer;


		public InterceptionOptimizer(IBallTrajectory ballTrajectory, Pass pass, ITrackedBot bot)
		{
			this.ballTrajectory = ballTrajectory;
			this.pass = pass;
			this.bot = bot;
			this.optimizer = new BrentOptimizer(0.001, 0.01, this::converged);
		}


		boolean converged(int iteration, UnivariatePointValuePair previous, UnivariatePointValuePair current)
		{
			var pointSize = 25.0;
			var ballTravelTime = previous.getPoint();
			var score = previous.getValue();
			var pos = ballTrajectory.getPosByTime(ballTravelTime).getXYVector();
			draw(() -> new DrawablePoint(pos).withSize(pointSize).setColor(colorPicker.getColor(score)));
			draw(() -> new DrawableAnnotation(pos, String.format("%d-%d", bot.getBotId().getNumber(), iteration))
					.withCenterHorizontally(true)
					.withFontHeight(12.5));
			draw(() -> new DrawableAnnotation(pos, String.format("%.1f -> %.1f", ballTravelTime, score))
					.withFontHeight(pointSize / 2)
					.withOffsetX(pointSize));
			return previous.getValue() <= 0;
		}


		private double objectiveFunction(double ballTravelTime)
		{
			var pos = ballTrajectory.getPosByTime(ballTravelTime).getXYVector();
			return ratePosForBot(pass, pos, ballTravelTime, bot);
		}


		private double ratePosForBot(Pass pass, IVector2 pos, double ballTravelTime, ITrackedBot bot)
		{
			if (bot.getBotId().getTeamColor() == pass.getShooter().getTeamColor())
			{
				// own bot
				return bot.getPosByTime(ballTravelTime).distanceTo(pos) > distToRobot ? 1 : 0;
			}

			double preparationTime = considerPassPreparationTime ? pass.getPreparationTime() : 0;

			double t;
			double additionalReactionTime;
			if (opponentMan2ManMarkers.contains(bot.getBotId()))
			{
				t = ballTravelTime + preparationTime;
				additionalReactionTime = 0;
			} else
			{
				t = ballTravelTime;
				additionalReactionTime = preparationTime;
			}
			return ratePosForOpponentBot(pos, bot, t, additionalReactionTime);
		}


		private double ratePosForOpponentBot(
				IVector2 pos, ITrackedBot bot, double tHorizon,
				double additionalReactionTime
		)
		{
			var movingHorizonFast = movingRobotsFast.get(bot.getBotId())
					.getMovingHorizon(tHorizon, additionalReactionTime);
			var movingHorizonSlow = movingRobotsSlow.get(bot.getBotId())
					.getMovingHorizon(tHorizon, additionalReactionTime);

			var centerPos = movingHorizonSlow.center().addNew(movingHorizonFast.center()).multiply(0.5);
			var distToPos = centerPos.distanceTo(pos);
			var range = movingHorizonFast.radius() - movingHorizonSlow.radius();
			if (range <= 0)
			{
				// inside robot?
				return distToPos > distToRobot ? 1 : 0;
			}
			double baseScore = (distToPos - movingHorizonSlow.radius()) / range;
			double dynamicFactor = Math.max(0, scoringFactor + scoringFactorOffset);
			return baseScore * dynamicFactor;
		}


		private double minimizeScore(TimeRange timeRange, double initialTime)
		{
			double init = SumatraMath.cap(initialTime, timeRange.min(), timeRange.max());
			try
			{
				UnivariatePointValuePair result = optimizer.optimize(
						GoalType.MINIMIZE,
						new MaxEval(100),
						new MaxIter(100),
						new SearchInterval(timeRange.min(), timeRange.max(), init),
						new UnivariateObjectiveFunction(this::objectiveFunction)
				);
				return result.getValue();
			} catch (TooManyIterationsException | TooManyEvaluationsException e)
			{
				log.debug("Could not find a solution for pass interception", e);
				return 0;
			}
		}
	}
}
