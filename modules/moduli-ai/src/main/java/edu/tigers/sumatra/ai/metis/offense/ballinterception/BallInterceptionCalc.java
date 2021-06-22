/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.ballinterception;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.kicking.Kick;
import edu.tigers.sumatra.ai.metis.kicking.OngoingPass;
import edu.tigers.sumatra.ai.metis.kicking.Pass;
import edu.tigers.sumatra.ball.trajectory.IBallTrajectory;
import edu.tigers.sumatra.bot.MoveConstraints;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableArrow;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.IPenaltyArea;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.Hysteresis;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.botshape.BotShape;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.pathfinder.TrajectoryGenerator;
import edu.tigers.sumatra.trajectory.ITrajectory;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static edu.tigers.sumatra.math.SumatraMath.cap;


/**
 * estimate how each robot can intercept the ball and store this information in the tactical field
 */
@RequiredArgsConstructor
public class BallInterceptionCalc extends ACalculator
{
	private static final DecimalFormat DF = new DecimalFormat("0.00");

	@Configurable(defValue = "400.0")
	private static double stepSize = 400.0;

	@Configurable(defValue = "5.0", comment = "Max considered ball speed")
	private static double maxBallSpeed = 5.0;

	@Configurable(defValue = "-0.5", comment = "Offset for fallback ball interception slack time [s]")
	private static double fallbackInterceptionsSlackTimeOffset = -0.5;

	private double initBallSpeed = 0;
	private double oldBallVel = 0;

	private final Supplier<Set<BotID>> potentialOffensiveBots;

	private final Supplier<Optional<OngoingPass>> ongoingPass;

	@Getter
	private Map<BotID, BallInterception> ballInterceptions;
	@Getter
	private Map<BotID, BallInterception> ballInterceptionsFallback;

	@Getter
	private Map<BotID, BallInterceptionInformation> ballInterceptionInformationMap;

	@Getter
	private boolean ballStopped;

	private final Hysteresis ballSpeedHysteresis = new Hysteresis(0.3, 0.4);

	private Map<BotID, IVector2> previousInterceptionPositions = new IdentityHashMap<>();
	private IPenaltyArea penAreaOur;
	private IPenaltyArea penAreaTheir;
	private IBallTrajectory ballTrajectory;


	@Override
	protected boolean isCalculationNecessary()
	{
		return ballIsMoving() && Geometry.getField().isPointInShape(getBall().getPos());
	}


	@Override
	protected void reset()
	{
		ballInterceptionInformationMap = Collections.emptyMap();
		ballInterceptions = Collections.emptyMap();
		ballInterceptionsFallback = Collections.emptyMap();
		previousInterceptionPositions = Collections.emptyMap();
		ballStopped = true;
	}


	@Override
	public void doCalc()
	{
		ballStopped = false;
		ballInterceptionInformationMap = new HashMap<>();
		ballTrajectory = findBallTrajectory();
		updateInitialBallVelocity();
		updatePenaltyAreas();

		Collection<BotID> consideredBots = consideredBots();

		List<IVector2> interceptionPoints = generateInterceptionPoints();
		Map<BotID, BallInterception> newBallInterceptions = new HashMap<>();
		Map<BotID, BallInterception> newBallInterceptionsFallback = new HashMap<>();
		for (var botID : consideredBots)
		{
			var iterations = iterateOverBallTravelLine(botID, interceptionPoints);
			InterceptionIteration previousInterception = getPreviousInterceptionIteration(botID);
			if (previousInterception != null)
			{
				// replace bot target iteration with near sampled iteration(s)
				iterations.removeIf(e -> Math.abs(e.getBallTravelTime() - previousInterception.getBallTravelTime()) < 0.1);
				iterations.add(previousInterception);
			}

			iterations.removeIf(e -> !Double.isFinite(e.getBallTravelTime()));
			iterations.sort(Comparator.comparingDouble(InterceptionIteration::getBallTravelTime));

			var zeroCrossings = findZeroCrossings(iterations);
			var corridors = findCorridors(zeroCrossings);
			var ballInterception = findTargetInterception(iterations, corridors, botID, previousInterception);
			var targetTime = ballInterception.map(BallInterception::getBallContactTime).orElse(0.0);
			ballInterception.ifPresent(e -> newBallInterceptions.put(botID, e));

			List<InterceptionIteration> iterationsSecondRun = new ArrayList<>();
			for (var iteration : iterations)
			{
				iterationsSecondRun
						.add(new InterceptionIteration(iteration.getBallTravelTime(),
								iteration.getSlackTime() + fallbackInterceptionsSlackTimeOffset, 0));
			}

			var zeroCrossingsSecond = findZeroCrossings(iterationsSecondRun);
			var corridorsSecond = findCorridors(zeroCrossingsSecond);
			var ballInterceptionSecond = findTargetInterception(iterationsSecondRun, corridorsSecond, botID,
					previousInterception);
			var targetTimeSecond = ballInterceptionSecond.map(BallInterception::getBallContactTime).orElse(0.0);
			ballInterceptionSecond.ifPresent(e -> newBallInterceptionsFallback.put(botID, e));

			var information = BallInterceptionInformation.builder()
					.initialIterations(iterations)
					.zeroAxisChanges(zeroCrossings)
					.interceptionCorridors(corridors)
					.interceptionTargetTime(targetTime)
					.interceptionTargetTimeFallback(targetTimeSecond)
					.oldInterception(previousInterception)
					.build();

			ballInterceptionInformationMap.put(botID, information);

			getShapes(EAiShapesLayer.OFFENSIVE_BALL_INTERCEPTION_DEBUG)
					.add(new DrawableAnnotation(getWFrame().getBot(botID).getPos(),
							(ballInterception.isPresent() ? "T" : "F") + DF.format(targetTime))
							.withOffset(Vector2f.fromX(-200)));
		}

		ballInterceptions = Collections.unmodifiableMap(newBallInterceptions);
		ballInterceptionsFallback = Collections.unmodifiableMap(newBallInterceptionsFallback);

		previousInterceptionPositions = newBallInterceptions.entrySet().stream()
				.collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, e -> e.getValue().getPos()));
	}


	private void updatePenaltyAreas()
	{
		var margin = Geometry.getBotRadius() + Geometry.getBallRadius();
		// Robots "must maintain best-effort to fully stay outside the own defense area"
		penAreaOur = Geometry.getPenaltyAreaOur().withMargin(margin);
		// "The ball must not be touched while being partially or fully inside the opponent defense area."
		penAreaTheir = Geometry.getPenaltyAreaTheir().withMargin(margin);
	}


	private IBallTrajectory findBallTrajectory()
	{
		return ongoingPass.get()
				.filter(og -> (getWFrame().getTimestamp() - og.getKickStartTime()) / 1e9 < 0.3)
				.map(OngoingPass::getPass)
				.map(pass -> Geometry.getBallFactory().createTrajectoryFromKickedBallWithoutSpin(
						pass.getKick().getSource(),
						pass.getKick().getKickVel().multiplyNew(1000)
				))
				.orElse(getBall().getTrajectory());
	}


	private InterceptionIteration getPreviousInterceptionIteration(BotID botID)
	{
		return ongoingPass.get()
				.map(OngoingPass::getPass)
				.filter(pass -> pass.getReceiver() == botID)
				.map(Pass::getKick)
				.map(Kick::getTarget)
				.or(() -> Optional.ofNullable(previousInterceptionPositions.get(botID)))
				.map(point -> ballTrajectory.getTravelLineRolling().closestPointOnLine(point))
				.filter(this::isLegal)
				.map(point -> createInterceptionIterationFromPreviousTarget(botID, point))
				.orElse(null);
	}


	private boolean ballMovesTowardsMe(BotID botID)
	{
		var ballDir = getBall().getVel();
		var ballToBotDir = getWFrame().getBot(botID).getPos().subtractNew(getBall().getPos());
		var angle = ballDir.angleToAbs(ballToBotDir).orElse(0.0);
		return angle < AngleMath.DEG_090_IN_RAD;
	}


	private InterceptionIteration createInterceptionIterationFromPreviousTarget(BotID botID, IVector2 target)
	{
		var ballTravelTime = ballTrajectory.getTimeByPos(target);
		var trajectory = generateAsyncTrajectory(botID, target);
		double botTime;
		if (target.distanceTo(trajectory.getPositionMM(0.0)) < 500)
		{
			botTime = trajectory.getTotalTimeToPrimaryDirection();
		} else
		{
			botTime = trajectory.getTotalTime();
		}

		var bot = getWFrame().getBot(botID);
		getShapes(EAiShapesLayer.OFFENSIVE_BALL_INTERCEPTION)
				.add(new DrawableCircle(Circle.createCircle(target, 20), Color.MAGENTA));
		getShapes(EAiShapesLayer.OFFENSIVE_BALL_INTERCEPTION)
				.add(new DrawableLine(Lines.segmentFromPoints(bot.getPos(), target), Color.MAGENTA));

		var slackTime = botTime - ballTravelTime;

		var timeLeftToTravel = trajectory.getTotalTime();
		var bonusValue = SumatraMath.relative(timeLeftToTravel, 2, 0) * 2.0;
		if (!ballMovesTowardsMe(botID) && timeLeftToTravel < 1.5)
		{
			bonusValue = 0;
		}
		return new InterceptionIteration(ballTravelTime, slackTime - bonusValue, bonusValue);
	}


	private boolean ballIsMoving()
	{
		ballSpeedHysteresis.update(getBall().getVel().getLength2());
		return !ballSpeedHysteresis.isLower();
	}


	private void updateInitialBallVelocity()
	{
		double ballVel = getBall().getVel().getLength();
		if (ballVel > oldBallVel || ballVel > initBallSpeed)
		{
			initBallSpeed = ballVel;
		}
		oldBallVel = ballVel;

		getShapes(EAiShapesLayer.OFFENSIVE_BALL_INTERCEPTION).add(new DrawableArrow(getBall().getPos(),
				getBall().getVel().scaleToNew(initBallSpeed * 1000), Color.MAGENTA));
	}


	private Optional<BallInterception> findTargetInterception(
			List<InterceptionIteration> iterations,
			List<InterceptionCorridor> corridors,
			BotID botID, InterceptionIteration previousInterception)
	{
		return corridors.stream()
				.filter(this::isCorridorLongEnough)
				.map(c -> findBestInterceptionIteration(iterations, c, previousInterception))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.map(it -> findBallInterception(botID, it))
				.findFirst();
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


	private BallInterception findBallInterception(BotID botID, InterceptionIteration iteration)
	{
		var interceptTime = iteration.getBallTravelTime();
		var interceptionTarget = ballTrajectory.getPosByTime(interceptTime).getXYVector();
		return new BallInterception(botID, interceptTime, interceptionTarget);
	}


	private Optional<InterceptionIteration> findBestInterceptionIteration(
			List<InterceptionIteration> initialIterations,
			InterceptionCorridor corridor,
			InterceptionIteration previousInterception)
	{
		double startTime = corridor.getStartTime();
		double endTime = corridor.getEndTime();
		if (previousInterception != null && previousInterception.getBallTravelTime() >= startTime
				&& previousInterception.getBallTravelTime() < endTime && ongoingPass.get().isPresent())
		{
			// previousInterception is a pass target and within corridor range
			return Optional.of(previousInterception);
		}
		return initialIterations.stream()
				.filter(it -> it.getBallTravelTime() >= startTime && it.getBallTravelTime() < endTime)
				.filter(it -> isIterationInterceptable(it.getSlackTime(), it.getBallTravelTime()))
				.findFirst();
	}


	private boolean isIterationInterceptable(double slackTime, final double ballTravelTime)
	{
		if (ballTravelTime < 0.5 && slackTime < 0.2)
		{
			return true;
		}
		return slackTime < -0.2;
	}


	private List<InterceptionCorridor> findCorridors(List<InterceptionZeroAxisCrossing> crossings)
	{
		var corridors = new ArrayList<InterceptionCorridor>();
		boolean startCorridorFound = false;
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


	private List<InterceptionZeroAxisCrossing> findZeroCrossings(List<InterceptionIteration> iterations)
	{
		if (iterations.isEmpty())
		{
			return Collections.emptyList();
		}

		var zeroCrossings = new ArrayList<InterceptionZeroAxisCrossing>();
		var previousPoint = iterations.get(0);
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


	private double getBallTravelTimeFromZeroCrossing(InterceptionIteration oldPoint, InterceptionIteration it)
	{
		double m = (oldPoint.getSlackTime() - it.getSlackTime()) / (-it.getBallTravelTime() + oldPoint
				.getBallTravelTime());
		double b = it.getSlackTime() - m * it.getBallTravelTime();
		return -b / m;
	}


	private boolean isIterationInvalid(InterceptionIteration it)
	{
		return !Double.isFinite(it.getBallTravelTime()) || !Double.isFinite(it.getSlackTime());
	}


	private Collection<BotID> consideredBots()
	{
		return ongoingPass.get().map(OngoingPass::getPass)
				.map(Pass::getReceiver)
				.filter(BotID::isBot)
				.map(Set::of)
				.orElse(potentialOffensiveBots.get());
	}


	private List<IVector2> generateInterceptionPoints()
	{
		List<IVector2> iterations = new ArrayList<>();

		var ballTravelLine = ballTrajectory.getTravelLine();
		var borderToBall = ballTravelLine.directionVector().multiplyNew(-1).normalize();
		var ballPos = ballTrajectory.getInitialPos().getXYVector();
		var fieldBorderIntersection = ballPos
				.nearestToOpt(Geometry.getField().lineIntersections(ballTravelLine))
				.orElse(ballPos);
		var ballStopTime = ballTrajectory.getTimeByVel(0);
		var ballStopDist = ballTrajectory.getDistByTime(ballStopTime);

		getShapes(EAiShapesLayer.OFFENSIVE_BALL_INTERCEPTION)
				.add(new DrawableCircle(Circle.createCircle(fieldBorderIntersection, 100)).setColor(Color.BLACK));

		var distBallToBorder = ballPos.distanceTo(fieldBorderIntersection);
		var minStepSize = stepSize / 2.0;
		var relativeBallSpeed = cap(initBallSpeed / maxBallSpeed, 0, 1);
		var adjustedStepSize = relativeBallSpeed * (stepSize - minStepSize) + minStepSize;
		var sampleRange = Math.min(ballStopDist, distBallToBorder);
		var distStartToBorder = distBallToBorder - distBallToBorder % adjustedStepSize;
		var maxStepId = Math.ceil(sampleRange / adjustedStepSize);

		for (int i = 0; i < maxStepId; i++)
		{
			var stepDistance = distStartToBorder - i * adjustedStepSize;
			var point = fieldBorderIntersection.addNew(borderToBall.multiplyNew(stepDistance));

			if (isLegal(point))
			{
				getShapes(EAiShapesLayer.OFFENSIVE_BALL_INTERCEPTION)
						.add(new DrawableCircle(Circle.createCircle(point, 50), Color.MAGENTA));
				iterations.add(point);
			}
		}

		// always add ballPos as potential target
		iterations.add(ballPos);
		return iterations;
	}


	private List<InterceptionIteration> iterateOverBallTravelLine(BotID botID, List<IVector2> interceptionPoints)
	{
		return interceptionPoints.stream()
				.map(point -> createInterceptionIteration(botID, point))
				.collect(Collectors.toList());
	}


	private boolean isLegal(IVector2 interceptionPoint)
	{
		// be a bit more pessimistic here for practical reasons (it should center2Dribbler + ballRadius strictly speaking)
		var offset = Geometry.getBotRadius();
		var destination = interceptionPoint.addNew(ballTrajectory.getTravelLine().directionVector().scaleToNew(offset));
		return !penAreaOur.isPointInShapeOrBehind(destination)
				&& !penAreaTheir.isPointInShapeOrBehind(destination)
				&& Geometry.getField().isPointInShape(interceptionPoint);
	}


	private InterceptionIteration createInterceptionIteration(BotID botID, IVector2 target)
	{
		double timeBot2Target = generateAsyncTrajectory(botID, target).getTotalTime();
		// Assumption: target is on the rolling ball travel line
		double timeBall2Target = ballTrajectory.getTimeByPos(target);

		return new InterceptionIteration(timeBall2Target, timeBot2Target - timeBall2Target, 0);
	}


	private ITrajectory<IVector2> generateAsyncTrajectory(BotID botID, IVector2 target)
	{
		var ballDir = ballTrajectory.getTravelLine().directionVector();
		var bot = getWFrame().getBot(botID);
		var moveConstraints = new MoveConstraints(bot.getMoveConstraints()).setPrimaryDirection(ballDir);
		var offset = bot.getCenter2DribblerDist() + Geometry.getBallRadius();

		// We want to avoid knowing how the bot rotates around the interception target.
		// So we calculate the destination as if the robot would receive the ball straight.
		var destination = target.addNew(ballDir.scaleToNew(offset));

		// We additionally only take the current robot kicker position and based on it, calculate the receive destination
		var orientation = ballDir.getAngle() + AngleMath.DEG_180_IN_RAD;
		var botPos = BotShape.getCenterFromKickerPos(bot.getBotKickerPos(Geometry.getBallRadius()), orientation, offset);
		var botVel = bot.getVel();

		// if kickPos == target, than the trajectory time is 0
		return TrajectoryGenerator.generatePositionTrajectory(moveConstraints, botPos, botVel, destination);
	}
}
