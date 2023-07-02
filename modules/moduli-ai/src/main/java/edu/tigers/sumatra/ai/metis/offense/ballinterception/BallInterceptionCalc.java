/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.ballinterception;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.kicking.OngoingPass;
import edu.tigers.sumatra.ai.metis.kicking.Pass;
import edu.tigers.sumatra.ai.metis.pass.PassInterceptionCalc;
import edu.tigers.sumatra.ball.trajectory.IBallTrajectory;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import lombok.Getter;

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
public class BallInterceptionCalc extends PassInterceptionCalc
{
	@Configurable(defValue = "300.0", comment = "[mm]")
	private static double stepSize = 300.0;
	@Configurable(defValue = "5.0", comment = "[m/s] Max considered ball speed")
	private static double maxBallSpeed = 5.0;

	@Configurable(defValue = "0.1", comment = "[s]")
	private static double oldTargetInterceptionBaseBonus = 0.1;

	@Configurable(defValue = "0.12", comment = "[s]")
	private static double timeToTrustPlannedPassInsteadOfKickFitState = 0.12;

	@Configurable(defValue = "500.0", comment = "[mm] Consider all bots to catch if deviation from planned pass to high")
	private static double maxDeviationFromPlannedPass = 500;

	private final Supplier<Set<BotID>> potentialOffensiveBots;
	@Getter
	private Map<BotID, RatedBallInterception> ballInterceptions;

	@Getter
	private Map<BotID, BallInterceptionInformation> ballInterceptionInformationMap;

	private boolean canOngoingPassBeTrusted;


	public BallInterceptionCalc(Supplier<Set<BotID>> potentialOffensiveBots, Supplier<Optional<OngoingPass>> ongoingPass)
	{
		super(ongoingPass);
		this.potentialOffensiveBots = potentialOffensiveBots;
	}


	@Override
	protected void reset()
	{
		super.reset();
		ballInterceptionInformationMap = Collections.emptyMap();
		ballInterceptions = Collections.emptyMap();
		canOngoingPassBeTrusted = true;
		oldTargetInterceptionBonus = oldTargetInterceptionBaseBonus;
	}


	@Override
	public void doCalc()
	{
		super.doCalc();
		oldTargetInterceptionBonus = oldTargetInterceptionBaseBonus;
		ballInterceptionInformationMap = new HashMap<>();

		Collection<BotID> consideredBots = consideredBots();

		List<IVector2> interceptionPoints = generateInterceptionPoints(Geometry.getField(), maxBallSpeed, stepSize);

		Map<BotID, RatedBallInterception> newBallInterceptions = new HashMap<>();
		for (var botID : consideredBots)
		{
			var iterations = iterateOverBallTravelLine(botID, interceptionPoints);

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

			var zeroCrossings = findZeroCrossings(iterations);
			var corridors = findCorridors(zeroCrossings);


			Optional<RatedBallInterception> ballInterception = findTargetInterception(iterations, corridors, botID,
					previousInterception, -0.35);
			if (ballInterception.isEmpty())
			{
				ballInterception =
						findTargetInterception(iterations, corridors, botID, previousInterception, 0.0);
			}

			var targetTime = ballInterception.map(e -> e.getBallInterception().getBallContactTime()).orElse(0.0);
			ballInterception.ifPresentOrElse(e -> newBallInterceptions.put(botID, e),
					() -> keepPositionUntilPassVanishes(newBallInterceptions, botID, previousInterception));

			var information = BallInterceptionInformation.builder()
					.initialIterations(iterations)
					.zeroAxisChanges(zeroCrossings)
					.interceptionCorridors(corridors)
					.interceptionTargetTime(targetTime)
					.oldInterception(previousInterception)
					.build();

			ballInterceptionInformationMap.put(botID, information);

			getShapes(shapeLayer)
					.add(new DrawableAnnotation(getWFrame().getBot(botID).getPos(),
							(ballInterception.isPresent() ? "T" : "F") + DF.format(targetTime))
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


	@Override
	protected IBallTrajectory findBallTrajectory()
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
