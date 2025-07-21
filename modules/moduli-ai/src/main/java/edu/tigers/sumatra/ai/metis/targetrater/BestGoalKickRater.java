/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.targetrater;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.kicking.KickFactory;
import edu.tigers.sumatra.ai.metis.pass.KickOrigin;
import edu.tigers.sumatra.drawable.ColorPickerFactory;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.IColorPicker;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.ERotationDirection;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.line.ILineSegment;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;
import lombok.Getter;
import lombok.Setter;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;


/**
 * Calculates the best goal kick target on the goal for from the point where the ball is or will be when received or redirected.
 * The result is a target in the opponents goal and a probability a shot to this target will score a goal.
 */
public class BestGoalKickRater
{
	private static final DecimalFormat DF = new DecimalFormat("0.00");

	@Configurable(comment = "The maximum reasonable angle [rad] for redirects", defValue = "1.2")
	private static double maximumReasonableRedirectAngle = 1.2;

	@Configurable(defValue = "10")
	private static int maxIterationsForPrediction = 10;

	@Configurable(defValue = "1.0")
	private static double rotationTimeFactor = 1.0;

	@Configurable(comment = "[0-1] Defender might not be able to drive perfectly as soon as kick detected (1 -> perfect driving)", defValue = "0.9")
	private static double noneOptimalDriveFactor = 0.9;

	@Configurable(comment = "[0-1] shorten the goal by this factor", defValue = "0.05")
	private static double shortenGoalFactor = 0.05;

	@Configurable(defValue = "1.3", comment = "Rotation time multiplier")
	private static double rotationTimeMultiplier = 1.3;

	static
	{
		ConfigRegistration.registerClass("metis", BestGoalKickRater.class);
	}

	private final IColorPicker colorPicker = ColorPickerFactory.greenRedGradient();
	private final KickFactory kickFactory = new KickFactory();
	private AngleRangeRater angleRangeRater;
	private WorldFrame worldFrame;

	@Setter
	private double waitTime = 0.0;


	@Getter
	private List<IDrawableShape> shapes = new ArrayList<>();
	private Map<BotID, GoalKick> previousBestGoalKickPerBot;


	public void update(WorldFrame worldFrame)
	{
		update(worldFrame, Map.of());
	}


	public void update(WorldFrame worldFrame, Map<BotID, GoalKick> bestGoalKickPerBot)
	{
		shapes.clear();
		this.previousBestGoalKickPerBot = bestGoalKickPerBot;
		this.worldFrame = worldFrame;
		kickFactory.update(worldFrame);

		// we create a shortened goal line to prevent kicks very close to the goal post.
		var goalStart = Geometry.getGoalTheir().getLineSegment().getPathStart();
		var goalEnd = Geometry.getGoalTheir().getLineSegment().getPathEnd();
		var startToEnd = goalEnd.subtractNew(goalStart);
		var shortenedStart = goalStart.addNew(startToEnd.multiplyNew(shortenGoalFactor / 2.0));
		var shortenedEnd = goalEnd.addNew(startToEnd.multiplyNew(-shortenGoalFactor / 2.0));
		var shortenedGoalLineSegment = Lines.segmentFromPoints(shortenedStart, shortenedEnd);
		angleRangeRater = AngleRangeRater.forLineSegment(shortenedGoalLineSegment);

		angleRangeRater.setNoneOptimalDriveFactor(noneOptimalDriveFactor);
	}


	public BestGoalKicks rateKickOrigin(final KickOrigin kickOrigin)
	{
		angleRangeRater.setObstacles(
				Stream.concat(
						worldFrame.getOpponentBots().values().stream(),
						worldFrame.getTigerBotsAvailable().values().stream()
								.filter(e -> e.getBotId() != kickOrigin.getShooter())
				).toList());
		var goalKicks = rateKickOriginComplex(kickOrigin);
		goalKicks.goalKick().ifPresent(this::drawBestKick);
		return goalKicks;
	}


	private void drawBestKick(final GoalKick goalKick)
	{
		final var target = goalKick.getRatedTarget();
		final var kickOrigin = goalKick.getKickOrigin();

		ILineSegment line = Lines.segmentFromPoints(kickOrigin.getPos(), target.getTarget());
		shapes.add(new DrawableLine(line, colorPicker.getColor(target.getScore())));
		shapes.add(new DrawableAnnotation(line.getPathEnd(), "Complex" + " -> " + DF.format(target.getScore()))
				.withOffset(Vector2f.fromX(100)));

	}


	private BestGoalKicks rateKickOriginComplex(final KickOrigin kickOrigin)
	{
		var data = generateBestGoalKickRaterData(kickOrigin);

		List<GoalKick> goalKicks = new ArrayList<>();
		List<GoalKick> redirectGoalKicks = new ArrayList<>();

		var goalKickCandidatesClockwise = simulateRotation(ERotationDirection.CLOCKWISE, data);
		goalKicks.addAll(goalKickCandidatesClockwise.directGoalKicks);
		redirectGoalKicks.addAll(goalKickCandidatesClockwise.redirectGoalKicks);

		var goalKickCandidatesCounterClockwise = simulateRotation(ERotationDirection.COUNTER_CLOCKWISE, data);
		goalKicks.addAll(goalKickCandidatesCounterClockwise.directGoalKicks);
		redirectGoalKicks.addAll(goalKickCandidatesCounterClockwise.redirectGoalKicks);

		var redirectGoalKicksFiltered = redirectGoalKicks.stream()
				.filter(e -> calcRotationTime(data, e.getRatedTarget()) * rotationTimeMultiplier < data.kickOrigin.getImpactTime())
				.toList();

		shapes.addAll(angleRangeRater.createDebugShapes());

		shapes
				.add(new DrawableAnnotation(
						kickOrigin.getPos(),
						String.format("Found %d GoalKickTargets", goalKicks.size())
				).withOffset(Vector2f.fromY(200)));
		var closestPerfectGoalKick = goalKicks.stream()
				.filter(goalKick -> goalKick.getRatedTarget().getScore() >= 1.0)
				.min(Comparator.comparingDouble(goalKick -> calcRotationTime(data, goalKick.getRatedTarget())));

		var closestPerfectRedirectGoalKick = redirectGoalKicksFiltered.stream()
				.filter(goalKick -> goalKick.getRatedTarget().getScore() >= 1.0)
				.min(Comparator.comparingDouble(goalKick -> calcRotationTime(data, goalKick.getRatedTarget())));

		var bestGoalKick = closestPerfectGoalKick.or(
				() -> goalKicks.stream().max(Comparator.comparingDouble(goalKick -> goalKick.getRatedTarget().getScore()))
		);

		var bestRedirectGoalKick = closestPerfectRedirectGoalKick.or(
				() -> redirectGoalKicksFiltered.stream()
						.max(Comparator.comparingDouble(goalKick -> goalKick.getRatedTarget().getScore()))
		);

		return new BestGoalKicks(bestGoalKick, bestRedirectGoalKick);
	}


	private GoalKick generateGoalKick(
			final KickOrigin kickOrigin, final IRatedTarget ratedTarget,
			final double aimingTolerance
	)
	{
		kickFactory.setAimingTolerance(aimingTolerance);
		return new GoalKick(
				kickOrigin,
				ratedTarget,
				kickFactory.goalKick(kickOrigin.getPos(), ratedTarget.getTarget()),
				isBallRedirectReasonable(kickOrigin, ratedTarget.getTarget())
		);
	}


	private double calcAimingTolerance(final IRatedTarget ratedTarget)
	{
		return Math.max(0.0, ratedTarget.getRange());
	}


	private BestGoalKickRaterData generateBestGoalKickRaterData(final KickOrigin kickOrigin)
	{
		final ITrackedBot bot = worldFrame.getBot(kickOrigin.getShooter());
		final double botCurrentRotation = bot.getAngleByTime(0);
		return new BestGoalKickRaterData(kickOrigin, bot, botCurrentRotation);
	}


	private GoalKickCandidates simulateRotation(
			final ERotationDirection wantedRotationDirection,
			final BestGoalKickRaterData data
	)
	{
		var step = 0;
		List<GoalKick> goalKicks = new ArrayList<>();
		List<GoalKick> redirectGoalKicks = new ArrayList<>();
		for (var angle : generateSimulationAngleSteps(wantedRotationDirection, data))
		{
			var ratedTargets = getReachableRatedTargetForAngle(wantedRotationDirection, data, angle);

			// get best max score target for direct kick
			var ratedTarget = ratedTargets.stream().max(Comparator.comparingDouble(IRatedTarget::getScore));

			// get max score target for redirect
			var ratedRedirectTarget = ratedTargets.stream()
					.filter(e -> isBallRedirectReasonable(data.kickOrigin, e.getTarget()))
					.max(Comparator.comparingDouble(IRatedTarget::getScore));

			drawSimStep(data.kickOrigin.getPos(), ratedTarget.orElse(null), angle, calcRotationTime(data, angle), step);
			ratedTarget.ifPresent(
					target -> goalKicks.add(generateGoalKick(data.kickOrigin, target, calcAimingTolerance(target))));

			ratedRedirectTarget.ifPresent(
					target -> redirectGoalKicks.add(generateGoalKick(data.kickOrigin, target, calcAimingTolerance(target))));
			step++;
		}
		return new GoalKickCandidates(goalKicks, redirectGoalKicks);
	}


	private List<Double> generateSimulationAngleSteps(
			final ERotationDirection wantedRotationDirection,
			final BestGoalKickRaterData data
	)
	{
		final IVector2 firstReachedGoalPost = wantedRotationDirection == ERotationDirection.COUNTER_CLOCKWISE ?
				Geometry.getGoalTheir().getRightPost() :
				Geometry.getGoalTheir().getLeftPost();
		final IVector2 secondReachedGoalPost = wantedRotationDirection == ERotationDirection.COUNTER_CLOCKWISE ?
				Geometry.getGoalTheir().getLeftPost() :
				Geometry.getGoalTheir().getRightPost();

		final IVector2 origin2first = Vector2.fromPoints(data.kickOrigin.getPos(), firstReachedGoalPost);
		final IVector2 origin2second = Vector2.fromPoints(data.kickOrigin.getPos(), secondReachedGoalPost);

		final double botAngle = data.botCurrentAngle;
		final double origin2firstAngle = origin2first.getAngle();
		final double origin2secondAngle = origin2second.getAngle();

		if (isTurnReasonable(wantedRotationDirection, botAngle, origin2firstAngle, origin2secondAngle))
		{
			return SumatraMath.evenDistribution1D(
					AngleMath.diffAbs(origin2firstAngle, origin2secondAngle) > AngleMath
							.diffAbs(botAngle, origin2secondAngle) ?
							botAngle :
							origin2firstAngle,
					origin2secondAngle, maxIterationsForPrediction
			);
		}
		return List.of();
	}


	private boolean isTurnReasonable(
			ERotationDirection wantedRotationDirection, double botAngle,
			double origin2firstAngle,
			double origin2secondAngle
	)
	{
		return AngleMath.rotationDirection(botAngle, origin2firstAngle) == wantedRotationDirection
				|| AngleMath.rotationDirection(botAngle, origin2secondAngle) == wantedRotationDirection;
	}


	private void drawSimStep(
			final IVector2 origin, final IRatedTarget target, final double angle,
			final double time, final int step
	)
	{
		if (target != null)
		{
			ILineSegment line = Lines.segmentFromPoints(origin, target.getTarget());
			shapes.add(new DrawableLine(line, Color.BLACK));
			shapes.add(new DrawableAnnotation(
					line.getPathEnd(),
					String.format("%d: ", step) + DF.format(target.getScore()) + " | " + DF.format(angle) + " | " + DF
							.format(time)
			).withOffset(Vector2f.fromX(100.0)));
		}
	}


	private List<IRatedTarget> getReachableRatedTargetForAngle(
			final ERotationDirection botRotationDirection, final BestGoalKickRaterData data,
			final double targetAngle
	)
	{
		final double timeToKick = calcRotationTime(data, targetAngle);
		setAngleRangeRaterTimeToKick(data, timeToKick);
		var potentialGoalTime = potentialGoalTimeCalc(data, timeToKick);
		if (waitTime - potentialGoalTime > 0)
		{
			return Collections.emptyList();
		}
		var reachableTargets = angleRangeRater.rateMultiple(data.kickOrigin.getPos()).stream()
				.filter(ratedTarget -> willRatedTargetBeReached(botRotationDirection, data, targetAngle, ratedTarget))
				.toList();

		var closestPerfectTarget = reachableTargets.stream()
				.filter(target -> target.getScore() >= 1.0)
				.min(Comparator.comparingDouble(target -> calcRotationTime(data, target)));

		return closestPerfectTarget.map(List::of).orElse(reachableTargets);
	}


	private double potentialGoalTimeCalc(final BestGoalKickRaterData data, final double timeToKick)
	{
		final double freeTimeToRotate = Double.isInfinite(data.kickOrigin.getImpactTime()) ?
				0.0 :
				data.kickOrigin.getImpactTime();
		return Math.max(timeToKick, freeTimeToRotate);
	}


	private boolean willRatedTargetBeReached(
			final ERotationDirection botRotationDirection,
			final BestGoalKickRaterData data, final double futureBotAngle, final IRatedTarget ratedTarget
	)
	{
		final double targetAngle = Vector2.fromPoints(data.kickOrigin.getPos(), ratedTarget.getTarget()).getAngle();

		// Either the bot would have turned more than needed or bot already rotated into the aimingTolerance
		return isCoveredDuringRotation(botRotationDirection, data, futureBotAngle, targetAngle)
				|| AngleMath.diffAbs(futureBotAngle, targetAngle) < calcAimingTolerance(ratedTarget) / 2.0;
	}


	private boolean isCoveredDuringRotation(
			ERotationDirection botRotationDirection, BestGoalKickRaterData data,
			double futureBotAngle, double targetAngle
	)
	{
		return AngleMath.rotationDirection(data.botCurrentAngle, targetAngle) == botRotationDirection
				&& AngleMath.rotationDirection(futureBotAngle, targetAngle) != botRotationDirection;
	}


	private void setAngleRangeRaterTimeToKick(final BestGoalKickRaterData data, final double timeToKick)
	{
		final double freeTimeToRotate = worldFrame.getBall().getTrajectory().getTimeByPos(data.kickOrigin.getPos());
		angleRangeRater.setTimeToKick(Math.max(0.0, (timeToKick - freeTimeToRotate) * rotationTimeFactor));
	}


	private double calcRotationTime(final BestGoalKickRaterData data, final IRatedTarget target)
	{
		return calcRotationTime(data, Vector2.fromPoints(data.bot.getPos(), target.getTarget()).getAngle());
	}


	private double calcRotationTime(final BestGoalKickRaterData data, final double angle)
	{
		return RotationTimeHelper.calcRotationTime(
				data.bot.getAngularVel(),
				data.bot.getAngleByTime(0),
				angle,
				data.bot.getMoveConstraints().getVelMaxW(),
				data.bot.getMoveConstraints().getAccMaxW()
		);
	}


	private boolean isBallRedirectReasonable(KickOrigin kickOrigin, final IVector2 target)
	{
		// used for hyst
		var couldBeRedirected = false;
		if (previousBestGoalKickPerBot != null)
		{
			couldBeRedirected = Optional.ofNullable(previousBestGoalKickPerBot.get(kickOrigin.getShooter()))
					.map(GoalKick::canBeRedirected)
					.orElse(false);
		}

		if (Double.isInfinite(kickOrigin.getImpactTime()))
		{
			// ball is not moving. Therefore, it cannot be redirected!
			return false;
		}

		var originToBall = worldFrame.getBall().getPos().subtractNew(kickOrigin.getPos());
		var originToTarget = target.subtractNew(kickOrigin.getPos());
		var redirectAngle = originToBall.angleToAbs(originToTarget).orElse(Math.PI);
		double maxAngle = maximumReasonableRedirectAngle - (couldBeRedirected ? 0 : 0.1);
		return redirectAngle <= maxAngle;
	}


	private record BestGoalKickRaterData(KickOrigin kickOrigin, ITrackedBot bot, double botCurrentAngle)
	{
	}

	private record GoalKickCandidates(List<GoalKick> directGoalKicks, List<GoalKick> redirectGoalKicks)
	{

	}
}
