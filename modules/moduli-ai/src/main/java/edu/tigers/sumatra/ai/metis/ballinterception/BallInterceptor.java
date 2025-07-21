/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.ballinterception;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.offense.OffensiveConstants;
import edu.tigers.sumatra.ball.trajectory.IBallTrajectory;
import edu.tigers.sumatra.bot.EDribbleTractionState;
import edu.tigers.sumatra.bot.MoveConstraints;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableArrow;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.DrawableShapeBoundary;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.I2DShape;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.botshape.BotShape;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.line.ILineSegment;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.pathfinder.TrajectoryGenerator;
import edu.tigers.sumatra.trajectory.ITrajectory;
import edu.tigers.sumatra.wp.data.ITrackedBall;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static edu.tigers.sumatra.math.SumatraMath.cap;


@RequiredArgsConstructor
public class BallInterceptor
{
	private static final DecimalFormat DF = new DecimalFormat("0.00");
	@Configurable(defValue = "5.0", comment = "[m/s] Max considered ball speed")
	private static double maxBallSpeed = 5.0;
	@Configurable(defValue = "300.0", comment = "[mm]")
	private static double stepSize = 300.0;

	static
	{
		ConfigRegistration.registerClass("metis", BallInterceptor.class);
	}

	private final Function<BotID, Optional<IVector2>> preferredInterceptPosition;
	private final Function<BotID, Optional<IVector2>> previousInterceptPosition;
	private final Supplier<IBallTrajectory> ballTrajectorySupplier;
	private final Color shapeColor;

	@Setter
	private boolean isAllowedToInterceptChipped = false;
	@Setter
	private double oldTargetInterceptionBonus = 0.1;
	@Setter
	private List<I2DShape> exclusionAreas;
	@Setter
	private I2DShape areaOfInterest;
	@Getter
	private List<IDrawableShape> shapes = new ArrayList<>();
	@Setter
	private Set<BotID> consideredBots;
	@Setter
	private List<InterceptionFinderParameters> finderParams;

	private IBallTrajectory ballTrajectory;
	private double oldBallVel = 0;
	private double initBallSpeed = 0;


	public BallInterceptorResult processFrame(WorldFrame wFrame)
	{
		shapes = new ArrayList<>();
		ballTrajectory = ballTrajectorySupplier.get();
		updateInitialBallVelocity(wFrame.getBall());

		var interceptionPoints = generateInterceptionPoints();
		Map<BotID, RatedBallInterception> interceptionMap = new HashMap<>();
		Map<BotID, BallInterceptionInformation> informationMap = new HashMap<>();
		for (var botId : consideredBots)
		{
			var bot = wFrame.getBot(botId);
			var iterations = iterateOverBallTravelLine(bot, interceptionPoints);

			var previousInterception = getPreviousInterceptionIteration(bot);
			if (previousInterception.isPresent())
			{
				// replace bot target iteration with near sampled iteration(s)
				iterations.removeIf(
						e -> Math.abs(e.getBallTravelTime() - previousInterception.get().getBallTravelTime()) < 0.1);
				iterations.add(previousInterception.get());
			}

			iterations.removeIf(e -> !Double.isFinite(e.getBallTravelTime()));
			iterations.sort(Comparator.comparingDouble(InterceptionIteration::getBallTravelTime));

			var zeroCrossings = findZeroCrossings(iterations);
			var corridors = findCorridors(zeroCrossings);

			Optional<RatedBallInterception> ballInterception = Optional.empty();
			for (var params : finderParams)
			{
				ballInterception = findTargetInterception(iterations, corridors, botId, previousInterception.orElse(null),
						params);
				if (ballInterception.isPresent())
				{
					break;
				}
			}

			var targetTime = ballInterception.map(e -> e.getBallInterception().getBallContactTime()).orElse(0.0);
			ballInterception.ifPresentOrElse(
					e -> interceptionMap.put(botId, e),
					() -> keepPositionUntilPassVanishes(wFrame.getBall(), bot, previousInterception.orElse(null))
							.ifPresent(e -> interceptionMap.put(botId, e))
			);

			var information = BallInterceptionInformation.builder()
					.initialIterations(iterations)
					.zeroAxisChanges(zeroCrossings)
					.interceptionCorridors(corridors)
					.interceptionTargetTime(targetTime)
					.oldInterception(previousInterception.orElse(null))
					.build();

			informationMap.put(botId, information);

			shapes.add(new DrawableAnnotation(bot.getPos(),
					(ballInterception.isPresent() ? "T" : "F") + DF.format(targetTime))
					.withOffset(Vector2f.fromX(-200)));
		}
		return new BallInterceptorResult(
				Collections.unmodifiableMap(interceptionMap),
				Collections.unmodifiableMap(informationMap)
		);
	}


	private Optional<InterceptionIteration> getPreviousInterceptionIteration(ITrackedBot bot)
	{
		var preferredPosition = preferredInterceptPosition.apply(bot.getBotId())
				.map(this::projectPointOntoBallTrajectory)
				.filter(this::isPositionLegal)
				.map(p -> createInterceptionIterationFromPreviousTarget(bot, p, true));
		var previousPosition = previousInterceptPosition.apply(bot.getBotId())
				.map(this::projectPointOntoBallTrajectory)
				.filter(this::isPositionLegal)
				.map(p -> createInterceptionIterationFromPreviousTarget(bot, p, false));

		if (preferredPosition.isPresent() && previousPosition.isPresent())
		{
			// here we want to prefer the preferred position. However, if the bot seems to already intercept the old position,
			// then the bot should keep doing so.
			if (bot.getPos().distanceTo(
					ballTrajectory.getPosByTime(previousPosition.get().getBallTravelTime()).getXYVector())
					> Geometry.getBotRadius() * 2.0 && previousPosition.get().getBallTravelTime() < 0.2)
			{
				// bot is already very close to the destination
				// ball impact is imminent
				// Thus, we rather keep or old point and flag it as preferred instead of the new preferred one
				var prev = previousPosition.get();
				return Optional.of(InterceptionIteration.builder()
						.ballTravelTime(prev.getBallTravelTime())
						.slackTime(prev.getSlackTime())
						.includedSlackTimeBonus(prev.getIncludedSlackTimeBonus())
						.isPreferred(true)
						.distanceAtOvershoot(prev.getDistanceAtOvershoot())
						.velocityAtOvershoot(prev.getVelocityAtOvershoot())
						.build()
				);
			} else
			{
				return preferredPosition;
			}
		} else if (preferredPosition.isPresent())
		{
			return preferredPosition;
		}
		return previousPosition;
	}


	public boolean ballIsNotUnderOurControl(final WorldFrame wFrame)
	{
		var tigerHasBallTraction = wFrame.getTigerBotsAvailable().entrySet().stream()
				.filter(e -> e.getValue().getPos().distanceTo(wFrame.getBall().getPos()) < Geometry.getBotRadius() * 4)
				.anyMatch(e -> e.getValue().getRobotInfo().getDribbleTraction() == EDribbleTractionState.STRONG
						|| e.getValue().getRobotInfo().getDribbleTraction() == EDribbleTractionState.LIGHT);
		ITrackedBall ball = wFrame.getBall();
		return (!tigerHasBallTraction || ball.getVel().getLength() > 2.5);
	}


	private void updateInitialBallVelocity(ITrackedBall ball)
	{
		double ballVel = ballTrajectory.getVelByTime(0).getXYVector().getLength();
		if (ballVel > oldBallVel || ballVel > initBallSpeed)
		{
			initBallSpeed = ballVel;
		}
		oldBallVel = ballVel;

		shapes.add(new DrawableArrow(ball.getPos(), ball.getVel().scaleToNew(initBallSpeed * 1000), shapeColor));
	}


	private boolean ballMovesTowardsMe(IVector2 botPos)
	{
		var ballDir = ballTrajectory.getVelByTime(0).getXYVector();
		var ballToBotDir = botPos.subtractNew(ballTrajectory.getPosByTime(0).getXYVector());
		var angle = ballDir.angleToAbs(ballToBotDir).orElse(0.0);
		return angle < AngleMath.DEG_090_IN_RAD;
	}


	private Optional<RatedBallInterception> keepPositionUntilPassVanishes(ITrackedBall ball, ITrackedBot bot,
			InterceptionIteration previousInterception)
	{
		var leadPointOnLine = ball.getTrajectory().getTravelLineSegment().closestPointOnPath(bot.getBotKickerPos());
		boolean isBotCloseToBallLine = ball.getTrajectory().getTravelLineSegment().distanceTo(bot.getBotKickerPos())
				< Geometry.getBotRadius() * 2.0;

		var ballContactTime = ball.getTrajectory().getTimeByDist(leadPointOnLine.distanceTo(ball.getPos()));
		boolean ballImpactIsImminent = ballContactTime < 0.3;

		if (isBotCloseToBallLine && ballImpactIsImminent)
		{
			var destination = leadPointOnLine;
			if (previousInterception != null)
			{
				destination = ball.getTrajectory().getPosByTime(previousInterception.getBallTravelTime()).getXYVector();
			}

			var totalBotTravelTime = generateTrajectoryPair(bot, destination).normalTrajectory().getTotalTime();

			shapes.add(new DrawableAnnotation(leadPointOnLine, "bot_time: " + DF.format(totalBotTravelTime),
					Vector2.fromY(-150)).setColor(Color.WHITE));

			// just a validity check.
			if (totalBotTravelTime < ballContactTime + 1)
			{
				shapes.add(new DrawableAnnotation(leadPointOnLine, "forcing intercept", Vector2.fromY(-100)).setColor(
						Color.WHITE));
				return Optional.of(
						new RatedBallInterception(new BallInterception(bot.getBotId(), ballContactTime, destination), 0, 0));
			}
		}
		return Optional.empty();
	}


	private Optional<RatedBallInterception> findTargetInterception(List<InterceptionIteration> iterations,
			List<InterceptionCorridor> corridors, BotID botID, InterceptionIteration previousInterception,
			InterceptionFinderParameters finderParams)
	{
		return corridors.stream().filter(e -> isCorridorLongEnough(e, previousInterception))
				.map(c -> findBestInterceptionIteration(iterations, c, previousInterception, finderParams))
				.flatMap(Optional::stream)
				.map(it -> findBallInterception(botID, it)).findAny();
	}


	private Optional<RatedInterceptionIteration> findBestInterceptionIteration(
			List<InterceptionIteration> initialIterations, InterceptionCorridor corridor,
			InterceptionIteration previousInterception, InterceptionFinderParameters finderParams)
	{
		double startTime = corridor.getStartTime();
		double endTime = corridor.getEndTime();

		double minSlackTimeInCorridor = initialIterations.stream()
				.filter(e -> e.getBallTravelTime() >= startTime && e.getBallTravelTime() < endTime)
				.filter(e -> isPositionLegal(ballTrajectory.getPosByTime(e.getBallTravelTime()).getXYVector()))
				.map(InterceptionIteration::getSlackTime).min(Comparator.comparingDouble(e -> e)).orElse(0.0);

		if (previousInterception != null && previousInterception.isPreferred()
				&& previousInterception.getBallTravelTime() >= startTime
				&& previousInterception.getBallTravelTime() < endTime)
		{
			// previousInterception is a pass target and within corridor range
			return Optional.of(
					new RatedInterceptionIteration(previousInterception, endTime - startTime, minSlackTimeInCorridor));
		}

		return initialIterations.stream()
				.filter(it -> it.getBallTravelTime() >= startTime && it.getBallTravelTime() < endTime)
				.filter(e -> isPositionLegal(ballTrajectory.getPosByTime(e.getBallTravelTime()).getXYVector()))
				.filter(it -> isIterationInterceptable(it, finderParams))
				.map(e -> new RatedInterceptionIteration(e, endTime - startTime, minSlackTimeInCorridor)).findFirst();
	}


	private RatedBallInterception findBallInterception(BotID botID, RatedInterceptionIteration iteration)
	{
		var interceptTime = iteration.getIteration().getBallTravelTime();
		var interceptionTarget = ballTrajectory.getPosByTime(interceptTime).getXYVector();
		return new RatedBallInterception(new BallInterception(botID, interceptTime, interceptionTarget),
				iteration.getCorridorLength(), iteration.getMinCorridorSlackTime());
	}


	private boolean isIterationInterceptable(InterceptionIteration iteration, InterceptionFinderParameters finderParams)
	{
		var ballTravelTime = iteration.getBallTravelTime();
		var slackTime = iteration.getSlackTime();

		// Check if already close to the target
		if (ballTravelTime < 0.5 && slackTime < 0.2)
		{
			return true;
		}

		// Check if timing is okay
		if (slackTime > finderParams.maxSlackTimeToAccept())
		{
			if (finderParams.isOvershootAllowed())
			{
				if (iteration.getDistanceAtOvershoot() > 0.1
						|| iteration.getVelocityAtOvershoot() > finderParams.maxVelocityAtInterceptWithOvershoot())
				{
					return false;
				}
			} else
			{
				return false;
			}
		}

		// Check if z-position is okay
		var zPos = ballTrajectory.getPosByTime(ballTravelTime).z();
		var zVel = ballTrajectory.getVelByTime(ballTravelTime).z();

		if (!isAllowedToInterceptChipped)
		{
			return Math.abs(zVel) < 0.1 && zPos < 0.1;
		}

		return ((zVel > 0.1 && zPos < RuleConstraints.getMaxRobotHeight())
				|| zPos < OffensiveConstants.getMaxInterceptHeight());
	}


	private boolean isCorridorLongEnough(final InterceptionCorridor interceptionCorridor,
			InterceptionIteration previousInterception)
	{
		if (interceptionCorridor.getStartTime() < 1.0)
		{
			// ball collision imminent, do not abort intercept!
			return true;
		}

		double startTime = interceptionCorridor.getStartTime();
		double endTime = interceptionCorridor.getEndTime();

		boolean isPreviousInterceptionAPassInValidCorridor = previousInterception != null
				&& previousInterception.isPreferred()
				&& previousInterception.getBallTravelTime() >= startTime
				&& previousInterception.getBallTravelTime() < endTime;
		if (isPreviousInterceptionAPassInValidCorridor)
		{
			// we accept planned passes even if they are very close calls to catch.
			return interceptionCorridor.getEndTime() - interceptionCorridor.getStartTime() > 0;
		}

		return interceptionCorridor.getEndTime() - interceptionCorridor.getStartTime() > 0.2;
	}


	private List<InterceptionCorridor> findCorridors(List<InterceptionZeroAxisCrossing> crossings)
	{
		var corridors = new ArrayList<InterceptionCorridor>();
		var startCorridorFound = false;
		double startCorridorTime = 0;
		for (var axisCrossing : crossings)
		{
			double ballTravelTime = axisCrossing.getBallTravelTime();
			if (axisCrossing.isPositiveCrossing())
			{
				// changed from negative to positive, end of corridor
				corridors.add(createCorridor(startCorridorTime, ballTravelTime));
				startCorridorFound = false;
			} else
			{
				// changed from positive to negative, start of corridor
				startCorridorTime = ballTravelTime;
				startCorridorFound = true;
			}
		}

		if (startCorridorFound)
		{
			double endCorridorTime = ballTrajectory.getTimeByVel(0);
			corridors.add(createCorridor(startCorridorTime, endCorridorTime));
		}
		return corridors;
	}


	private InterceptionCorridor createCorridor(double startCorridorTime, double endCorridorTime)
	{
		double end = ballTrajectory.getDistByTime(endCorridorTime);
		double start = ballTrajectory.getDistByTime(startCorridorTime);
		double width = end - start;
		return new InterceptionCorridor(startCorridorTime, endCorridorTime, width);
	}


	private List<InterceptionZeroAxisCrossing> findZeroCrossings(List<InterceptionIteration> iterations)
	{
		if (iterations.isEmpty())
		{
			return Collections.emptyList();
		}

		var zeroCrossings = new ArrayList<InterceptionZeroAxisCrossing>();
		var previousPoint = iterations.getFirst();
		var wasPositive = previousPoint.getSlackTime() > 0;
		for (var it : iterations)
		{
			if (isIterationInvalid(it))
			{
				continue;
			}

			if (wasPositive && it.getSlackTime() < 0)
			{
				// changed from positive to negative
				wasPositive = false;
				double ballTravelTime = getBallTravelTimeFromZeroCrossing(previousPoint, it);
				zeroCrossings.add(new InterceptionZeroAxisCrossing(ballTravelTime, false));
			} else if (!wasPositive && it.getSlackTime() > 0)
			{
				// changed from negative to positive
				wasPositive = true;
				double ballTravelTime = getBallTravelTimeFromZeroCrossing(previousPoint, it);
				zeroCrossings.add(new InterceptionZeroAxisCrossing(ballTravelTime, true));
			}
			previousPoint = it;
		}
		return zeroCrossings;
	}


	private boolean isIterationInvalid(InterceptionIteration it)
	{
		return !Double.isFinite(it.getBallTravelTime()) || !Double.isFinite(it.getSlackTime());
	}


	private double getBallTravelTimeFromZeroCrossing(InterceptionIteration oldPoint, InterceptionIteration it)
	{
		double m =
				(oldPoint.getSlackTime() - it.getSlackTime()) / (-it.getBallTravelTime() + oldPoint.getBallTravelTime());
		double b = it.getSlackTime() - m * it.getBallTravelTime();
		return -b / m;
	}


	private List<IVector2> generateInterceptionPoints()
	{
		var ballTravelLine = ballTrajectory.getTravelLine();
		var ballPos = ballTrajectory.getInitialPos().getXYVector();
		var borderToBall = ballTravelLine.directionVector().multiplyNew(-1).normalize();

		final IVector2 firstInterestingPos;
		final IVector2 lastInterestingPos;

		var enlargedAreaOfInterest = areaOfInterest.withMargin(stepSize * 1.5);

		var areaBorderIntersections = ballTrajectory.getTravelLineSegments().stream()
				.map(enlargedAreaOfInterest::intersectPerimeterPath).flatMap(Collection::stream)
				.sorted(Comparator.comparingDouble(ballPos::distanceToSqr)).toList();

		var ballStopTime = ballTrajectory.getTimeByVel(0);
		var ballStopDist = ballTrajectory.getDistByTime(ballStopTime);

		if (enlargedAreaOfInterest.isPointInShape(ballTrajectory.getInitialPos().getXYVector()))
		{
			firstInterestingPos = ballPos;
			lastInterestingPos = ballPos.nearestToOpt(areaBorderIntersections)
					.orElse(ballTrajectory.getPosByTime(ballStopTime).getXYVector());
		} else
		{
			if (areaBorderIntersections.isEmpty())
			{
				firstInterestingPos = ballPos;
				lastInterestingPos = ballTrajectory.getPosByTime(ballStopTime).getXYVector();
			} else if (areaBorderIntersections.size() == 1)
			{
				firstInterestingPos = areaBorderIntersections.getFirst();
				lastInterestingPos = ballTrajectory.getPosByTime(ballStopTime).getXYVector();
			} else
			{
				firstInterestingPos = areaBorderIntersections.get(0);
				lastInterestingPos = areaBorderIntersections.get(1);
			}
		}

		shapes.add(new DrawableShapeBoundary(areaOfInterest, Color.GRAY));
		shapes.add(new DrawableShapeBoundary(enlargedAreaOfInterest, Color.GRAY));
		shapes.add(new DrawableCircle(Circle.createCircle(firstInterestingPos, 100)).setColor(Color.WHITE));
		shapes.add(new DrawableCircle(Circle.createCircle(lastInterestingPos, 100)).setColor(Color.BLACK));

		var distBallToBorder = firstInterestingPos.distanceTo(lastInterestingPos);
		var minStepSize = stepSize / 2.0;
		var relativeBallSpeed = cap(initBallSpeed / maxBallSpeed, 0, 1);
		var adjustedStepSize = relativeBallSpeed * (stepSize - minStepSize) + minStepSize;
		var sampleRange = Math.min(ballStopDist, distBallToBorder);
		var distStartToBorder = distBallToBorder - distBallToBorder % adjustedStepSize;
		var maxStepId = Math.ceil(sampleRange / adjustedStepSize);

		// add intersections with the exclusion shapes.
		var intersections = exclusionAreas.stream()
				.flatMap(shape -> shape.intersectPerimeterPath(ballTrajectory.getTravelLineSegment()).stream()).toList();
		intersections.forEach(
				e -> shapes.add(new DrawableCircle(Circle.createCircle(e, 35), shapeColor.darker().darker())));
		List<IVector2> iterations = new ArrayList<>(intersections);
		for (var i = 0; i < maxStepId; i++)
		{
			var stepDistance = distStartToBorder - i * adjustedStepSize;
			var point = lastInterestingPos.addNew(borderToBall.multiplyNew(stepDistance));

			if (isPositionRoughlyLegal(point, stepSize * 1.5) && intersections.stream()
					.noneMatch(e -> e.distanceTo(point) < stepSize / 4))
			{
				iterations.add(point);
			}
		}

		// delete sample that may occupy the balls spot
		iterations.stream().filter(e -> e.distanceTo(firstInterestingPos) < 10).findFirst().ifPresent(iterations::remove);
		// always add ballPos as potential target
		iterations.add(firstInterestingPos);

		iterations.forEach(e -> shapes.add(new DrawableCircle(Circle.createCircle(e, 50), shapeColor)));
		return iterations;
	}


	private List<InterceptionIteration> iterateOverBallTravelLine(ITrackedBot bot, List<IVector2> interceptionPoints)
	{
		return interceptionPoints.stream().map(point -> createInterceptionIteration(bot, point))
				.collect(Collectors.toCollection(ArrayList::new));
	}


	private InterceptionIteration createInterceptionIterationFromPreviousTarget(ITrackedBot bot, IVector2 target,
			boolean isPreferred)
	{
		shapes.add(new DrawableCircle(Circle.createCircle(target, 50), Color.RED));
		shapes.add(new DrawableCircle(Circle.createCircle(target, 20), shapeColor));
		shapes.add(new DrawableLine(bot.getPos(), target, shapeColor));

		var trajectoryPair = generateTrajectoryPair(bot, target);
		var timeLeftToTravel = trajectoryPair.normalTrajectory().getTotalTime();
		var bonusValue = SumatraMath.relative(timeLeftToTravel, 2, 0) * 2.0 + oldTargetInterceptionBonus;
		if (!ballMovesTowardsMe(bot.getPos()) && timeLeftToTravel < 1.5)
		{
			bonusValue = 0;
		}
		return createInterceptionIteration(trajectoryPair, target, bonusValue, isPreferred);
	}


	private InterceptionIteration createInterceptionIteration(ITrackedBot bot, IVector2 target)
	{
		var trajectoryPair = generateTrajectoryPair(bot, target);
		return createInterceptionIteration(trajectoryPair, target, 0, false);
	}


	private InterceptionIteration createInterceptionIteration(TrajectoryPair trajectoryPair, IVector2 target,
			double bonusValue, boolean isPreferred)
	{
		var timeBot2Target = trajectoryPair.normalTrajectory().getTotalTime();
		// Assumption: target is on the rolling ball travel line
		var timeBall2Target = ballTrajectory.getTimeByPos(target);
		var overshootDistance = trajectoryPair.overshootTrajectory().getPositionMM(timeBall2Target).distanceTo(target);
		var overshootVelocity = trajectoryPair.overshootTrajectory().getVelocity(timeBot2Target).getLength();
		var slackTime = timeBot2Target - timeBall2Target;
		return InterceptionIteration.builder()
				.ballTravelTime(timeBall2Target)
				.slackTime(slackTime - bonusValue)
				.includedSlackTimeBonus(bonusValue)
				.isPreferred(isPreferred)
				.distanceAtOvershoot(overshootDistance)
				.velocityAtOvershoot(overshootVelocity)
				.build();
	}


	private TrajectoryPair generateTrajectoryPair(ITrackedBot bot, IVector2 target)
	{
		var ballDir = ballTrajectory.getTravelLine().directionVector();
		var ballTime = ballTrajectory.getTimeByPos(target);

		var moveConstraints = new MoveConstraints(bot.getMoveConstraints()).setPrimaryDirection(Vector2.zero());
		var offset = bot.getCenter2DribblerDist() + Geometry.getBallRadius();

		// We want to avoid knowing how the bot rotates around the interception target.
		// So we calculate the destination as if the robot would receive the ball straight.
		var destination = target.addNew(ballDir.scaleToNew(offset));

		// We additionally only take the current robot kicker position and based on it, calculate the receive destination
		var orientation = ballDir.getAngle() + AngleMath.DEG_180_IN_RAD;
		var botPos = BotShape.getCenterFromKickerPos(bot.getBotKickerPos(Geometry.getBallRadius()), orientation, offset);
		var botVel = bot.getVel();

		// if kickPos == target, then the trajectory time is 0
		return new TrajectoryPair(
				TrajectoryGenerator.generatePositionTrajectory(moveConstraints, botPos, botVel, destination),
				TrajectoryGenerator.generatePositionTrajectoryToReachPointInTime(moveConstraints, botPos, botVel,
						destination, ballTime)
		);
	}


	private boolean isPositionRoughlyLegal(IVector2 interceptionPoint, double extraMargin)
	{
		// be a bit more pessimistic here for practical reasons (it should center2Dribbler + ballRadius strictly speaking)
		var offset = Geometry.getBotRadius();
		var destination = interceptionPoint.addNew(ballTrajectory.getTravelLine().directionVector().scaleToNew(offset));

		return exclusionAreas.stream().noneMatch(shape -> shape.withMargin(-extraMargin).isPointInShape(destination))
				&& areaOfInterest.withMargin(extraMargin).isPointInShape(interceptionPoint);
	}


	private IVector2 projectPointOntoBallTrajectory(IVector2 position)
	{
		List<ILineSegment> travelLines;
		if (isAllowedToInterceptChipped)
		{
			travelLines = ballTrajectory.getTravelLinesInterceptableBelow(OffensiveConstants.getMaxInterceptHeight());
		} else
		{
			travelLines = ballTrajectory.getTravelLinesRolling();
		}


		var projected = travelLines
				.stream()
				.map(line -> line.closestPointOnPath(position))
				.toList();
		return position.nearestTo(projected);
	}


	private boolean isPositionLegal(IVector2 interceptionPoint)
	{
		// be a bit more pessimistic here for practical reasons (it should center2Dribbler + ballRadius strictly speaking)
		var offset = Geometry.getBotRadius();
		var destination = interceptionPoint.addNew(ballTrajectory.getTravelLine().directionVector().scaleToNew(offset));

		return exclusionAreas.stream().noneMatch(shape -> shape.isPointInShape(destination))
				&& exclusionAreas.stream().noneMatch(shape -> shape.isPointInShape(interceptionPoint))
				&& areaOfInterest.isPointInShape(interceptionPoint);
	}


	public record BallInterceptorResult(
			Map<BotID, RatedBallInterception> interceptionMap,
			Map<BotID, BallInterceptionInformation> informationMap
	)
	{
	}

	private record TrajectoryPair(
			ITrajectory<IVector2> normalTrajectory,
			ITrajectory<IVector2> overshootTrajectory
	)
	{
	}
}
