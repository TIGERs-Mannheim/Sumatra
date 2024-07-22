/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.general;

import com.github.g3force.configurable.ConfigRegistration;
import edu.tigers.sumatra.ai.metis.ballinterception.BallInterception;
import edu.tigers.sumatra.ai.metis.ballinterception.InterceptionCorridor;
import edu.tigers.sumatra.ai.metis.ballinterception.InterceptionIteration;
import edu.tigers.sumatra.ai.metis.ballinterception.InterceptionZeroAxisCrossing;
import edu.tigers.sumatra.ai.metis.ballinterception.RatedBallInterception;
import edu.tigers.sumatra.ai.metis.ballinterception.RatedInterceptionIteration;
import edu.tigers.sumatra.ai.metis.kicking.OngoingPass;
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
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.I2DShape;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.botshape.BotShape;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
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
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static edu.tigers.sumatra.math.SumatraMath.cap;


@RequiredArgsConstructor
public class BallInterceptor
{
	@Getter
	private static final DecimalFormat DF = new DecimalFormat("0.00");

	static
	{
		ConfigRegistration.registerClass("metis", BallInterceptor.class);
	}

	private final Supplier<Optional<OngoingPass>> ongoingPass;
	@Setter
	private IBallTrajectory ballTrajectory;
	@Setter
	private double oldTargetInterceptionBonus = 0.1;
	@Setter
	private Color shapeColor;
	@Setter
	private List<IDrawableShape> shapes;
	@Setter
	private List<I2DShape> exclusionAreas = List.of();
	@Setter
	private I2DShape areaOfInterest;
	private double oldBallVel = 0;
	private double initBallSpeed = 0;


	public boolean ballIsNotUnderOurControl(final WorldFrame wFrame)
	{
		var tigerHasBallTraction = wFrame.getTigerBotsAvailable().entrySet().stream()
				.filter(e -> e.getValue().getPos().distanceTo(wFrame.getBall().getPos()) < Geometry.getBotRadius() * 4)
				.anyMatch(e -> e.getValue().getRobotInfo().getDribbleTraction() == EDribbleTractionState.STRONG
						|| e.getValue().getRobotInfo().getDribbleTraction() == EDribbleTractionState.LIGHT);
		ITrackedBall ball = wFrame.getBall();
		return (!tigerHasBallTraction || ball.getVel().getLength() > 2.5);
	}


	public void updateInitialBallVelocity(ITrackedBall ball)
	{
		double ballVel = ball.getVel().getLength();
		if (ballVel > oldBallVel || ballVel > initBallSpeed)
		{
			initBallSpeed = ballVel;
		}
		oldBallVel = ballVel;

		shapes.add(new DrawableArrow(ball.getPos(), ball.getVel().scaleToNew(initBallSpeed * 1000), shapeColor));
	}


	public InterceptionIteration createInterceptionIterationFromPreviousTarget(ITrackedBot bot, IVector2 target,
			ITrackedBall ball)
	{
		shapes.add(new DrawableCircle(Circle.createCircle(target, 50), Color.RED));

		var ballTravelTime = ballTrajectory.getTimeByPos(target);
		var trajectory = generateAsyncTrajectory(bot, target);
		double botTime;
		if (target.distanceTo(trajectory.getPositionMM(0.0)) < 500)
		{
			botTime = trajectory.getTotalTimeToPrimaryDirection();
		} else
		{
			botTime = trajectory.getTotalTime();
		}

		shapes.add(new DrawableCircle(Circle.createCircle(target, 20), shapeColor));
		shapes.add(new DrawableLine(bot.getPos(), target, shapeColor));

		var slackTime = botTime - ballTravelTime;

		var timeLeftToTravel = trajectory.getTotalTime();
		var bonusValue = SumatraMath.relative(timeLeftToTravel, 2, 0) * 2.0 + oldTargetInterceptionBonus;
		if (!ballMovesTowardsMe(bot.getPos(), ball) && timeLeftToTravel < 1.5)
		{
			bonusValue = 0;
		}
		return new InterceptionIteration(ballTravelTime, slackTime - bonusValue, bonusValue);
	}


	private boolean ballMovesTowardsMe(IVector2 botPos, ITrackedBall ball)
	{
		var ballDir = ball.getVel();
		var ballToBotDir = botPos.subtractNew(ball.getPos());
		var angle = ballDir.angleToAbs(ballToBotDir).orElse(0.0);
		return angle < AngleMath.DEG_090_IN_RAD;
	}


	public Optional<RatedBallInterception> keepPositionUntilPassVanishes(ITrackedBall ball, ITrackedBot bot,
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

			var totalBotTravelTime = generateAsyncTrajectory(bot, destination).getTotalTime();

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


	public Optional<RatedBallInterception> findTargetInterception(List<InterceptionIteration> iterations,
			List<InterceptionCorridor> corridors, BotID botID, InterceptionIteration previousInterception,
			double maxSlackTimeToAccept)
	{
		return corridors.stream().filter(this::isCorridorLongEnough)
				.map(c -> findBestInterceptionIteration(iterations, c, previousInterception, maxSlackTimeToAccept))
				.flatMap(Optional::stream).map(it -> findBallInterception(botID, it)).findAny();
	}


	private RatedBallInterception findBallInterception(BotID botID, RatedInterceptionIteration iteration)
	{
		var interceptTime = iteration.getIteration().getBallTravelTime();
		var interceptionTarget = ballTrajectory.getPosByTime(interceptTime).getXYVector();
		return new RatedBallInterception(new BallInterception(botID, interceptTime, interceptionTarget),
				iteration.getCorridorLength(), iteration.getMinCorridorSlackTime());
	}


	private Optional<RatedInterceptionIteration> findBestInterceptionIteration(
			List<InterceptionIteration> initialIterations, InterceptionCorridor corridor,
			InterceptionIteration previousInterception, double maxSlackTimeToAccept)
	{
		double startTime = corridor.getStartTime();
		double endTime = corridor.getEndTime();

		double minSlackTimeInCorridor = initialIterations.stream()
				.filter(e -> e.getBallTravelTime() >= startTime && e.getBallTravelTime() < endTime)
				.filter(e -> isPositionLegal(ballTrajectory.getPosByTime(e.getBallTravelTime()).getXYVector()))
				.map(InterceptionIteration::getSlackTime).min(Comparator.comparingDouble(e -> e)).orElse(0.0);

		if (previousInterception != null && previousInterception.getBallTravelTime() >= startTime
				&& previousInterception.getBallTravelTime() < endTime && ongoingPass.get().isPresent())
		{
			// previousInterception is a pass target and within corridor range
			return Optional.of(
					new RatedInterceptionIteration(previousInterception, endTime - startTime, minSlackTimeInCorridor));
		}

		return initialIterations.stream()
				.filter(it -> it.getBallTravelTime() >= startTime && it.getBallTravelTime() < endTime)
				.filter(e -> isPositionLegal(ballTrajectory.getPosByTime(e.getBallTravelTime()).getXYVector()))
				.filter(it -> isIterationInterceptable(it.getSlackTime(), it.getBallTravelTime(), maxSlackTimeToAccept))
				.map(e -> new RatedInterceptionIteration(e, endTime - startTime, minSlackTimeInCorridor)).findFirst();
	}


	private boolean isIterationInterceptable(double slackTime, final double ballTravelTime, double maxSlackTimeToAccept)
	{
		if (ballTravelTime < 0.5 && slackTime < 0.2)
		{
			return true;
		}
		return slackTime < maxSlackTimeToAccept;
	}


	private boolean isCorridorLongEnough(final InterceptionCorridor interceptionCorridor)
	{
		if (interceptionCorridor.getStartTime() < 1.0)
		{
			// ball collision imminent, do not abort intercept!
			return true;
		}
		return interceptionCorridor.getEndTime() - interceptionCorridor.getStartTime() > 0.2;
	}


	public List<InterceptionCorridor> findCorridors(List<InterceptionZeroAxisCrossing> crossings)
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


	public List<InterceptionZeroAxisCrossing> findZeroCrossings(List<InterceptionIteration> iterations)
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


	public List<IVector2> generateInterceptionPoints(double maxBallSpeed, double stepSize)
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


	public List<InterceptionIteration> iterateOverBallTravelLine(ITrackedBot bot, List<IVector2> interceptionPoints)
	{
		return interceptionPoints.stream().map(point -> createInterceptionIteration(bot, point))
				.collect(Collectors.toCollection(ArrayList::new));
	}


	private InterceptionIteration createInterceptionIteration(ITrackedBot bot, IVector2 target)
	{
		double timeBot2Target = generateAsyncTrajectory(bot, target).getTotalTime();
		// Assumption: target is on the rolling ball travel line
		double timeBall2Target = ballTrajectory.getTimeByPos(target);

		return new InterceptionIteration(timeBall2Target, timeBot2Target - timeBall2Target, 0);
	}


	private ITrajectory<IVector2> generateAsyncTrajectory(ITrackedBot bot, IVector2 target)
	{
		var ballDir = ballTrajectory.getTravelLine().directionVector();
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
		return TrajectoryGenerator.generatePositionTrajectory(moveConstraints, botPos, botVel, destination);
	}


	public boolean isPositionRoughlyLegal(IVector2 interceptionPoint, double extraMargin)
	{
		// be a bit more pessimistic here for practical reasons (it should center2Dribbler + ballRadius strictly speaking)
		var offset = Geometry.getBotRadius();
		var destination = interceptionPoint.addNew(ballTrajectory.getTravelLine().directionVector().scaleToNew(offset));

		return exclusionAreas.stream().noneMatch(shape -> shape.withMargin(-extraMargin).isPointInShape(destination))
				&& areaOfInterest.withMargin(extraMargin).isPointInShape(interceptionPoint);
	}


	public boolean isPositionLegal(IVector2 interceptionPoint)
	{
		// be a bit more pessimistic here for practical reasons (it should center2Dribbler + ballRadius strictly speaking)
		var offset = Geometry.getBotRadius();
		var destination = interceptionPoint.addNew(ballTrajectory.getTravelLine().directionVector().scaleToNew(offset));

		return exclusionAreas.stream().noneMatch(shape -> shape.isPointInShape(destination))
				&& exclusionAreas.stream().noneMatch(shape -> shape.isPointInShape(interceptionPoint))
				&& areaOfInterest.isPointInShape(interceptionPoint);
	}

}
