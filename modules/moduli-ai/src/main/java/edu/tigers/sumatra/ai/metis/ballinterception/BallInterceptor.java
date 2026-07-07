package edu.tigers.sumatra.ai.metis.ballinterception;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import com.github.g3force.configurable.EConfigUnit;
import edu.tigers.sumatra.ai.metis.offense.OffensiveConstants;
import edu.tigers.sumatra.ball.trajectory.IBallTrajectory;
import edu.tigers.sumatra.bot.EDribbleTractionState;
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
import edu.tigers.sumatra.trajectory.TimedTrajectory2D;
import edu.tigers.sumatra.wp.data.ITrackedBall;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.awt.Color;
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
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static edu.tigers.sumatra.math.SumatraMath.cap;


@Log4j2
@RequiredArgsConstructor
public class BallInterceptor
{
	@Configurable(defValue = "5.0", comment = "Max considered ball speed", unit = EConfigUnit.VELOCITY_M)
	private static double maxBallSpeed = 5.0;
	@Configurable(defValue = "300.0", unit = EConfigUnit.DISTANCE_MM)
	private static double stepSize = 300.0;

	static
	{
		ConfigRegistration.registerClass("metis", BallInterceptor.class);
	}

	private final Function<BotID, Optional<PreviousBallInterception>> preferredInterceptPosition;
	private final Function<BotID, Optional<PreviousBallInterception>> previousInterceptPosition;
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
				iterations.removeIf(e -> Math.abs(e.ballTravelTime() - previousInterception.get().ballTravelTime()) < 0.1);
				iterations.add(previousInterception.get());
			}

			iterations.removeIf(e -> !Double.isFinite(e.ballTravelTime()));
			iterations.sort(Comparator.comparingDouble(CombinedInterceptionIteration::ballTravelTime));

			var parameterizedBest = findBestInterceptionIteration(bot, iterations);
			var params = parameterizedBest.map(e -> e.params).orElseGet(finderParams::getLast);
			var best = parameterizedBest.map(e -> buildRatedBallInterceptionFromIteration(botId, e.applyParams()));

			var targetTime = best.map(e -> e.getBallInterception().getBallContactTime()).orElse(0.0);
			best.ifPresent(e -> interceptionMap.put(botId, e));

			var information = BallInterceptionInformation.builder()
					.initialIterations(iterations.stream().map(e -> e.applyParams(params)).toList())
					.interceptionTargetTime(targetTime)
					.oldInterception(previousInterception.map(e -> e.applyParams(params)).orElse(null))
					.build();
			informationMap.put(botId, information);

			shapes.add(new DrawableAnnotation(
					bot.getPos(),
					String.format(
							"ok=%c, tt=%.02f, st=%.02f%n" + "os=%c, max_st=%.02f",
							best.isPresent() ? 'y' : 'n',
							targetTime,
							best.map(RatedBallInterception::getSlackTime).orElse(0.0),
							params.isOvershootAllowed() ? 'y' : 'n',
							params.maxSlackTimeToAccept()
					)
			)
					.withOffset(Vector2f.fromX(100)));

		}
		return new BallInterceptorResult(
				Collections.unmodifiableMap(interceptionMap),
				Collections.unmodifiableMap(informationMap)
		);
	}


	private Optional<CombinedInterceptionIteration> getPreviousInterceptionIteration(ITrackedBot bot)
	{
		var preferredPosition = preferredInterceptPosition.apply(bot.getBotId())
				.map(this::projectPointOntoBallTrajectory)
				.map(this::moveOutsideExclusionAreas)
				.filter(this::isPositionLegal)
				.map(p -> createInterceptionIterationFromPreviousTarget(bot, p, true));
		var previousPosition = previousInterceptPosition.apply(bot.getBotId())
				.map(this::projectPointOntoBallTrajectory)
				.map(this::moveOutsideExclusionAreas)
				.filter(this::isPositionLegal)
				.map(p -> createInterceptionIterationFromPreviousTarget(bot, p, false));

		if (preferredPosition.isPresent() && previousPosition.isPresent())
		{
			// here we want to prefer the preferred position. However, if the bot seems to already intercept the old position,
			// then the bot should keep doing so.
			if (bot.getPos().distanceTo(
					ballTrajectory.getPosByTime(previousPosition.get().ballTravelTime()).getXYVector())
					> Geometry.getBotRadius() * 2.0 && previousPosition.get().ballTravelTime() < 0.2)
			{
				// bot is already very close to the destination
				// ball impact is imminent
				// Thus, we rather keep our old point and flag it as preferred instead of the new preferred one
				return previousPosition.map(prev -> prev.toBuilder().isPreferred(true).build());
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


	private PreviousBallInterception moveOutsideExclusionAreas(PreviousBallInterception interceptionPoint)
	{
		if (isPositionLegal(interceptionPoint))
		{
			return interceptionPoint;
		}

		for (var exclusionArea : exclusionAreas)
		{
			if (exclusionArea.isPointInShape(interceptionPoint))
			{
				var intersection = exclusionArea.intersectPerimeterPath(ballTrajectory.getTravelLine());
				if (intersection.size() == 1 && intersection.getFirst().distanceTo(interceptionPoint) < 300)
				{
					getShapes().add(DrawableArrow.fromPositions(interceptionPoint, intersection.getFirst()));
					return new PreviousBallInterception(intersection.getFirst(), interceptionPoint.isOvershooting());
				}
			}
		}
		return interceptionPoint;
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


	private boolean keepPositionUntilPassVanishes(
			ParameterizedIteration parameterizedIteration,
			ITrackedBot bot
	)
	{
		var iteration = parameterizedIteration.iteration();
		if (!iteration.isPrevious())
		{
			return false;
		}

		var targetPos = ballTrajectory.getPosByTime(iteration.ballTravelTime()).getXYVector();
		if (bot.getPos().distanceTo(targetPos) > Geometry.getBotRadius() * 2)
		{
			return false;
		}

		var ballContactTime = iteration.ballTravelTime();
		if (ballContactTime > 0.3)
		{
			return false;
		}

		var totalBotTravelTime = iteration.ballTravelTime
				+ parameterizedIteration.getInterceptionInfo().slackTime
				+ iteration.includedSlackTimeBonus;

		shapes.add(new DrawableAnnotation(
				targetPos, String.format("bot_time: %.02f", totalBotTravelTime),
				Vector2.fromY(-150)
		).setColor(Color.WHITE));

		return true;
	}


	private Optional<ParameterizedIteration> findBestInterceptionIteration(
			ITrackedBot bot,
			List<CombinedInterceptionIteration> initialIterations
	)
	{
		return initialIterations.stream()
				.filter(e -> isPositionLegal(ballTrajectory.getPosByTime(e.ballTravelTime()).getXYVector()))
				.flatMap(e -> e.applyParamsList(finderParams))
				.filter(e -> isIterationInterceptable(bot, e))
				.min(this::bestInterceptionIterationComparator);
	}


	private int bestInterceptionIterationComparator(ParameterizedIteration paramIt1, ParameterizedIteration paramIt2)
	{
		// First sort by isPreferred yes/no
		if (paramIt1.iteration.isPreferred != paramIt2.iteration.isPreferred)
		{
			return paramIt1.iteration.isPreferred ? -1 : 1;
		}

		// Then by params Index inside the list of InterceptionFinderParameters
		if (paramIt1.paramsIndex != paramIt2.paramsIndex)
		{
			return Integer.compare(paramIt1.paramsIndex, paramIt2.paramsIndex);
		}

		// Finally by the ballTravelTime
		return Double.compare(paramIt1.iteration.ballTravelTime(), paramIt2.iteration.ballTravelTime());
	}


	private boolean isIterationInterceptable(
			ITrackedBot bot,
			ParameterizedIteration parameterizedIteration
	)
	{
		var iteration = parameterizedIteration.iteration;
		var params = parameterizedIteration.params;

		var ballTravelTime = iteration.ballTravelTime();
		var interceptInfo = parameterizedIteration.getInterceptionInfo();
		var slackTime = interceptInfo.slackTime();

		// current iteration is previous iteration and is imminent
		if (iteration.isPrevious() && keepPositionUntilPassVanishes(parameterizedIteration, bot))
		{
			return true;
		}

		// Check if timing is okay
		if (slackTime > params.maxSlackTimeToAccept())
		{
			return false;
		}

		if (iteration.isPreferred())
		{
			return true;
		}

		if (interceptInfo.botVelAtInterceptTime() > params.maxVelocityAtInterceptWithOvershoot())
		{
			return false;
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


	private RatedBallInterception buildRatedBallInterceptionFromIteration(BotID botID, InterceptionIteration iteration)
	{
		var interceptTime = iteration.getBallTravelTime();
		var interceptionTarget = ballTrajectory.getPosByTime(interceptTime).getXYVector();
		return new RatedBallInterception(
				new BallInterception(botID, interceptTime, interceptionTarget), iteration.getSlackTime(),
				iteration.isOvershooting()
		);
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


	private List<CombinedInterceptionIteration> iterateOverBallTravelLine(
			ITrackedBot bot,
			List<IVector2> interceptionPoints
	)
	{
		return interceptionPoints.stream()
				.map(point -> createInterceptionIteration(bot, point))
				.collect(Collectors.toCollection(ArrayList::new));
	}


	private CombinedInterceptionIteration createInterceptionIterationFromPreviousTarget(
			ITrackedBot bot,
			PreviousBallInterception target,
			boolean isPreferred
	)
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
		return createInterceptionIteration(trajectoryPair, bonusValue, isPreferred, true, target.isOvershooting());
	}


	private CombinedInterceptionIteration createInterceptionIteration(
			ITrackedBot bot,
			IVector2 target
	)
	{
		var trajectoryPair = generateTrajectoryPair(bot, target);
		return createInterceptionIteration(trajectoryPair, 0, false, false, false);
	}


	private CombinedInterceptionIteration createInterceptionIteration(
			InterceptionTrajectories interceptionTrajectories,
			double bonusValue,
			boolean isPreferred,
			boolean isPrevious,
			boolean overshooting
	)
	{
		// Assumption: target is on the rolling ball travel line
		var timeBall2Target = interceptionTrajectories.overshootTrajectory().getTargetTime();

		double appliedNoOvershootBonus = overshooting ? 0 : bonusValue;
		var infoNoOvershoot = new InterceptionInformation(
				interceptionTrajectories.normalTrajectory().getTotalTime() - timeBall2Target -
						(isPreferred ? bonusValue : appliedNoOvershootBonus),
				false,
				interceptionTrajectories.normalTrajectory().getVelocity(timeBall2Target).getLength()
		);

		var timeBot2TargetOvershooting = SumatraMath.max(
				interceptionTrajectories.overshootTrajectory().getTimeAtTargetX(),
				interceptionTrajectories.overshootTrajectory().getTimeAtTargetY()
		);

		double appliedOvershootBonus = overshooting ? bonusValue : 0;
		var infoOvershoot = new InterceptionInformation(
				timeBot2TargetOvershooting - timeBall2Target -
						(isPreferred ? bonusValue : appliedOvershootBonus),
				timeBot2TargetOvershooting < interceptionTrajectories.normalTrajectory().getTotalTime(),
				interceptionTrajectories.overshootTrajectory().getVelocity(timeBall2Target).getLength()
		);

		return CombinedInterceptionIteration.builder()
				.interceptPos(interceptionTrajectories.normalTrajectory().getFinalDestination())
				.botInfoNoOvershoot(infoNoOvershoot)
				.botInfoOvershoot(infoOvershoot)
				.ballTravelTime(timeBall2Target)
				.includedSlackTimeBonus(bonusValue)
				.isPrevious(isPrevious)
				.isPreferred(isPreferred)
				.ballPosZ(interceptionTrajectories.ballPosZ())
				.ballVelZ(interceptionTrajectories.ballVelZ())
				.build();
	}


	private InterceptionTrajectories generateTrajectoryPair(
			ITrackedBot bot,
			IVector2 target
	)
	{
		var ballDir = ballTrajectory.getTravelLine().directionVector();
		var ballTime = ballTrajectory.getTimeByPos(target);

		var moveConstraints = bot.getMoveConstraints().withPrimaryDirection(Vector2.zero());
		var offset = bot.getCenter2DribblerDist() + Geometry.getBallRadius();

		// We want to avoid knowing how the bot rotates around the interception target.
		// So we calculate the destination as if the robot would receive the ball straight.
		var destination = target.addNew(ballDir.scaleToNew(offset));

		// We additionally only take the current robot kicker position and based on it, calculate the receive destination
		var orientation = ballDir.getAngle() + AngleMath.DEG_180_IN_RAD;
		var botPos = BotShape.getCenterFromKickerPos(bot.getBotKickerPos(Geometry.getBallRadius()), orientation, offset);
		var botVel = bot.getVel();

		var stopping = TrajectoryGenerator.generatePositionTrajectory(moveConstraints, botPos, botVel, destination);
		var overshooting = TrajectoryGenerator.generateTimedPositionTrajectory(
				moveConstraints, botPos, botVel, destination, ballTime);

		// if kickPos == target, then the trajectory time is 0
		return new InterceptionTrajectories(
				stopping,
				overshooting,
				ballTrajectory.getPosByTime(ballTime).z(),
				ballTrajectory.getVelByTime(ballTime).z()
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


	private PreviousBallInterception projectPointOntoBallTrajectory(PreviousBallInterception position)
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
		var nearest = position.nearestTo(projected);
		return new PreviousBallInterception(nearest, position.isOvershooting());
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

	private record InterceptionTrajectories(
			ITrajectory<IVector2> normalTrajectory,
			TimedTrajectory2D overshootTrajectory, // target time is ball travel time
			double ballPosZ,
			double ballVelZ
	)
	{
	}

	private record InterceptionInformation(
			double slackTime,
			boolean isOvershooting,
			double botVelAtInterceptTime
	)
	{}


	@Builder(toBuilder = true)
	private record CombinedInterceptionIteration(
			InterceptionInformation botInfoNoOvershoot,
			InterceptionInformation botInfoOvershoot,

			IVector2 interceptPos,

			double ballTravelTime,
			double includedSlackTimeBonus,
			boolean isPrevious,
			boolean isPreferred,

			double ballPosZ,
			double ballVelZ
	)
	{

		public InterceptionInformation getInterceptionInfo(InterceptionFinderParameters params)
		{
			if (!params.isOvershootAllowed())
			{
				return botInfoNoOvershoot;
			}

			return botInfoOvershoot;
		}


		public InterceptionIteration applyParams(InterceptionFinderParameters params)
		{
			var info = getInterceptionInfo(params);
			return InterceptionIteration.builder()
					.ballTravelTime(ballTravelTime)
					.slackTime(info.slackTime)
					.includedSlackTimeBonus(includedSlackTimeBonus)
					.isOvershooting(info.isOvershooting)
					.build();
		}


		public Stream<ParameterizedIteration> applyParamsList(List<InterceptionFinderParameters> paramsList)
		{
			return IntStream.range(0, paramsList.size())
					.mapToObj(i -> new ParameterizedIteration(i, paramsList.get(i), this));
		}
	}

	private record ParameterizedIteration(
			int paramsIndex,
			InterceptionFinderParameters params,
			CombinedInterceptionIteration iteration
	)
	{

		public InterceptionInformation getInterceptionInfo()
		{
			return iteration.getInterceptionInfo(params);
		}


		public InterceptionIteration applyParams()
		{
			return iteration.applyParams(params);
		}
	}
}
