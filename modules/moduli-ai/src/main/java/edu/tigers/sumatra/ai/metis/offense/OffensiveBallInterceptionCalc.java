/*
 * Copyright (c) 2009 - 2024, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.ballinterception.BallInterceptionInformation;
import edu.tigers.sumatra.ai.metis.ballinterception.InterceptionIteration;
import edu.tigers.sumatra.ai.metis.ballinterception.RatedBallInterception;
import edu.tigers.sumatra.ai.metis.ballpossession.BallPossession;
import edu.tigers.sumatra.ai.metis.ballpossession.EBallControl;
import edu.tigers.sumatra.ai.metis.general.BallInterceptor;
import edu.tigers.sumatra.ai.metis.kicking.Kick;
import edu.tigers.sumatra.ai.metis.kicking.OngoingPass;
import edu.tigers.sumatra.ai.metis.kicking.Pass;
import edu.tigers.sumatra.ball.trajectory.IBallTrajectory;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.awt.Color;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;


/**
 * estimate how each robot can intercept the ball and store this information in the tactical field
 */
@RequiredArgsConstructor
public class OffensiveBallInterceptionCalc extends ACalculator
{
	@Configurable(defValue = "0.1", comment = "[s] Maximum expected time the vision might deviate from the real world")
	private static double maxExpectedVisionTimeDeviation = 0.1;
	@Configurable(defValue = "300.0", comment = "[mm]")
	private static double stepSize = 300.0;
	@Configurable(defValue = "5.0", comment = "[m/s] Max considered ball speed")
	private static double maxBallSpeed = 5.0;
	@Configurable(defValue = "0.2", comment = "[m/s] accepted slack time for fallback calc, negative time means bot arrives before the ball")
	private static double fallBackCalculationAcceptedSlackTime = 0.2;

	@Configurable(defValue = "0.25", comment = "[s]")
	private static double oldTargetInterceptionBaseBonus = 0.25;

	@Configurable(defValue = "0.12", comment = "[s]")
	private static double timeToTrustPlannedPassInsteadOfKickFitState = 0.12;

	@Configurable(defValue = "500.0", comment = "[mm] Consider all bots to catch if deviation from planned pass to high")
	private static double maxDeviationFromPlannedPass = 500;
	private final Supplier<Set<BotID>> potentialOffensiveBots;
	private final Supplier<BallPossession> ballPossession;
	private Supplier<Optional<OngoingPass>> ongoingPass;
	@Getter
	private Map<BotID, RatedBallInterception> ballInterceptions;
	private Map<BotID, IVector2> previousInterceptionPositions = new HashMap<>();

	@Getter
	private Map<BotID, BallInterceptionInformation> ballInterceptionInformationMap;
	private boolean tigerDribblingBall;
	private IBallTrajectory ballTrajectory;
	private boolean canOngoingPassBeTrusted;
	private BallInterceptor ballInterceptor;


	public OffensiveBallInterceptionCalc(Supplier<Set<BotID>> potentialOffensiveBots,
			Supplier<BallPossession> ballPossession,
			Supplier<Optional<OngoingPass>> ongoingPass)
	{
		ballInterceptor = new BallInterceptor(ongoingPass);
		ballInterceptor.setShapeColor(Color.MAGENTA);
		this.potentialOffensiveBots = potentialOffensiveBots;
		this.ongoingPass = ongoingPass;
		this.ballPossession = ballPossession;
	}


	@Override
	protected boolean isCalculationNecessary()
	{
		var closestTiger = potentialOffensiveBots.get().stream()
				.map(e -> getWFrame().getBot(e))
				.min(Comparator.comparingDouble(
						e -> e.getBotKickerPos().distanceTo(getBall().getPos())));
		boolean tigerHasBall = closestTiger.map(e -> e.getBallContact().hasContact()).orElse(false)
				&& getBall().getVel().getLength() < 2.0;

		return ballInterceptor.ballIsNotUnderOurControl(getWFrame()) &&
				Geometry.getField().isPointInShape(getBall().getPos()) &&
				ballPossession.get().getOpponentBallControl() != EBallControl.STRONG &&
				!tigerHasBall;
	}


	public boolean isTigerDribblingBall()
	{
		return tigerDribblingBall;
	}


	@Override
	protected void reset()
	{
		tigerDribblingBall = true;
		ballTrajectory = null;
		previousInterceptionPositions = Collections.emptyMap();
		ballInterceptionInformationMap = Collections.emptyMap();
		ballInterceptions = Collections.emptyMap();
		canOngoingPassBeTrusted = true;
		ballInterceptor.setOldTargetInterceptionBonus(oldTargetInterceptionBaseBonus);
	}


	@Override
	public void doCalc()
	{
		final List<IDrawableShape> shapes = getShapes(EAiShapesLayer.OFFENSE_BALL_INTERCEPTION);
		ballInterceptor.setShapes(shapes);
		tigerDribblingBall = false;
		ballTrajectory = findBallTrajectory();
		ballInterceptor.setBallTrajectory(ballTrajectory);
		ballInterceptor.updateInitialBallVelocity(getBall());
		ballInterceptor.setOldTargetInterceptionBonus(oldTargetInterceptionBaseBonus);
		ballInterceptor.setAreaOfInterest(Geometry.getField());
		ballInterceptor.setExclusionAreas(List.of(
				Geometry.getPenaltyAreaOur().withMargin(Geometry.getBotRadius() + Geometry.getBallRadius()),
				Geometry.getPenaltyAreaTheir().withMargin(Geometry.getBotRadius() + Geometry.getBallRadius())
		));
		ballInterceptionInformationMap = new HashMap<>();

		Collection<BotID> consideredBots = consideredBots();

		List<IVector2> interceptionPoints = ballInterceptor.generateInterceptionPoints(maxBallSpeed, stepSize);

		Map<BotID, RatedBallInterception> newBallInterceptions = new HashMap<>();
		for (var botID : consideredBots)
		{
			var iterations = ballInterceptor.iterateOverBallTravelLine(getWFrame().getBot(botID), interceptionPoints);

			previousInterceptionPositions = invalidatePreviousInterceptionPositions();
			var previousInterception = getPreviousInterceptionIteration(botID);
			if (previousInterception != null)
			{
				// replace bot target iteration with near sampled iteration(s)
				iterations.removeIf(e -> Math.abs(e.getBallTravelTime() - previousInterception.getBallTravelTime()) < 0.1);
				iterations.add(previousInterception);
			}

			iterations.removeIf(e -> !Double.isFinite(e.getBallTravelTime()));
			iterations.sort(Comparator.comparingDouble(InterceptionIteration::getBallTravelTime));

			var zeroCrossings = ballInterceptor.findZeroCrossings(iterations);
			var corridors = ballInterceptor.findCorridors(zeroCrossings);

			Optional<RatedBallInterception> ballInterception = ballInterceptor.findTargetInterception(iterations,
					corridors, botID,
					previousInterception, -0.35);
			if (ballInterception.isEmpty())
			{
				ballInterception =
						ballInterceptor.findTargetInterception(iterations, corridors, botID, previousInterception,
								fallBackCalculationAcceptedSlackTime);
			}

			var targetTime = ballInterception.map(e -> e.getBallInterception().getBallContactTime()).orElse(0.0);
			ballInterception.ifPresentOrElse(
					e -> newBallInterceptions.put(botID, e),
					() -> ballInterceptor.keepPositionUntilPassVanishes(getBall(), getWFrame().getBot(botID),
									previousInterception)
							.ifPresent(e -> newBallInterceptions.put(botID, e))
			);

			var information = BallInterceptionInformation.builder()
					.initialIterations(iterations)
					.zeroAxisChanges(zeroCrossings)
					.interceptionCorridors(corridors)
					.interceptionTargetTime(targetTime)
					.oldInterception(previousInterception)
					.build();

			ballInterceptionInformationMap.put(botID, information);

			shapes.add(new DrawableAnnotation(getWFrame().getBot(botID).getPos(),
					(ballInterception.isPresent() ? "T" : "F") + BallInterceptor.getDF().format(targetTime))
					.withOffset(Vector2f.fromX(-200)));
		}

		ballInterceptions = Collections.unmodifiableMap(newBallInterceptions);
		previousInterceptionPositions = newBallInterceptions.entrySet().stream()
				.collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, e -> e.getValue().getBallInterception().getPos()));
	}


	private Collection<BotID> consideredBots()
	{
		return ongoingPass.get()
				.filter(e -> isOngoingPassDeviationFromBallTrajWithinErrorMargin(e, getBall().getTrajectory()))
				.map(OngoingPass::getPass)
				.map(Pass::getReceiver)
				.filter(id -> getWFrame().getBots().containsKey(id))
				.map(Set::of)
				.orElse(potentialOffensiveBots.get());
	}


	private Map<BotID, IVector2> invalidatePreviousInterceptionPositions()
	{
		var previousAttacker = getAiFrame().getPrevFrame().getTacticalField()
				.getOffensiveStrategy().getAttackerBot();

		return previousInterceptionPositions.entrySet().stream()
				.filter(e -> previousAttacker.isPresent() && previousAttacker.get() == e.getKey())
				.filter(e -> ballMovesTowardsPreviousInterceptionPoint(e.getValue()))
				.collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
	}


	private InterceptionIteration getPreviousInterceptionIteration(BotID botID)
	{
		var ongoingPassPos = ongoingPass.get()
				.map(OngoingPass::getPass)
				.filter(pass -> pass.getReceiver().equals(botID))
				.map(Pass::getKick)
				.map(Kick::getTarget)
				.map(point -> ballTrajectory.closestPointTo(point))
				.filter(point -> ballInterceptor.isPositionLegal(point))
				.map(point -> ballInterceptor.createInterceptionIterationFromPreviousTarget(getWFrame().getBot(botID),
						point, getBall()));
		var closestOldInterceptPos = Optional.ofNullable(previousInterceptionPositions.get(botID))
				.map(point -> ballTrajectory.closestPointTo(point))
				.filter(point -> ballInterceptor.isPositionLegal(point))
				.map(point -> ballInterceptor.createInterceptionIterationFromPreviousTarget(getWFrame().getBot(botID),
						point, getBall()));

		if (ongoingPassPos.isPresent() && closestOldInterceptPos.isPresent())
		{
			// here we want to prefer the ongoingPass. However, if the bot seems to already intercept the old position,
			// then the bot should keep doing so.
			if (getWFrame().getBot(botID).getPos().distanceTo(
					getBall().getTrajectory().getPosByTime(closestOldInterceptPos.get().getBallTravelTime()).getXYVector())
					> Geometry.getBotRadius() * 2.0 && closestOldInterceptPos.get().getBallTravelTime() < 0.2)
			{
				// bot is already very close to the destination
				// ball impact is imminent
				// Thus, we rather keep or old point instead of the ongoingPass
				return closestOldInterceptPos.get();
			}
		} else if (ongoingPassPos.isPresent())
		{
			return ongoingPassPos.get();
		}
		return closestOldInterceptPos.orElse(null);
	}


	private boolean ballMovesTowardsPreviousInterceptionPoint(IVector2 previousInterceptionPoint)
	{
		var maxExpectedBallMovingDistance = ballTrajectory.getDistByTime(maxExpectedVisionTimeDeviation);
		if (ballTrajectory.getInitialPos().getXYVector().distanceTo(previousInterceptionPoint)
				< maxExpectedBallMovingDistance)
		{
			return true;
		}
		var ballDir = ballTrajectory.getTravelLine().directionVector();
		var ballToPreviousInterceptionPoint = previousInterceptionPoint.subtractNew(getBall().getPos());
		return (ballDir.angleToAbs(ballToPreviousInterceptionPoint).orElse(0.0) < AngleMath.deg2rad(15));
	}


	private boolean isOngoingPassDeviationFromBallTrajWithinErrorMargin(OngoingPass ongoingPass,
			IBallTrajectory trajectory)
	{
		boolean canBallTrajBeTrusted = (getWFrame().getTimestamp() - ongoingPass.getKickStartTime()) / 1e9
				>= timeToTrustPlannedPassInsteadOfKickFitState;
		if (!canBallTrajBeTrusted)
		{
			return true;
		}

		IVector2 plannedTarget = ongoingPass.getPass().getKick().getTarget();
		return trajectory.getTravelLine().closestPointOnPath(plannedTarget).distanceTo(plannedTarget)
				< maxDeviationFromPlannedPass;
	}


	private IBallTrajectory findBallTrajectory()
	{
		var filteredOg = ongoingPass.get()
				.filter(og -> (getWFrame().getTimestamp() - og.getKickStartTime()) / 1e9
						< timeToTrustPlannedPassInsteadOfKickFitState);

		canOngoingPassBeTrusted = filteredOg.isPresent();
		return filteredOg
				.map(OngoingPass::getPass)
				.map(pass -> Geometry.getBallFactory().createTrajectoryFromKickedBallWithoutSpin(
						pass.getKick().getSource(),
						pass.getKick().getKickVel().multiplyNew(1000)
				))
				.orElse(getBall().getTrajectory());
	}


	public Optional<Boolean> canOngoingPassBeTrusted()
	{
		return Optional.of(canOngoingPassBeTrusted);
	}
}
