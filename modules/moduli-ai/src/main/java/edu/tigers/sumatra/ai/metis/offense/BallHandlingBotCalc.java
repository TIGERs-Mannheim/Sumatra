/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.ballinterception.RatedBallInterception;
import edu.tigers.sumatra.ai.metis.ballresponsibility.EBallResponsibility;
import edu.tigers.sumatra.ai.metis.botdistance.BotDistance;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.Hysteresis;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.pathfinder.TrajectoryGenerator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;


/**
 * find the best offensive robot to interact with the ball
 */
@RequiredArgsConstructor
public class BallHandlingBotCalc extends ACalculator
{
	@Configurable(defValue = "0.3", comment = "Time in seconds to subtract from current max traj time")
	private static double hystTimeOffsetForNonMovingBall = 0.3;

	@Configurable(defValue = "0.3", comment = "Time in seconds to subtract from current earliest ball time")
	private static double hystTimeOffsetForMovingBall = 0.3;

	@Configurable(defValue = "0.5", comment = "[m/s]")
	private static double estimatedApproachAndStopBallCatchUpSpeed = 0.5;

	@Configurable(defValue = "0.2", comment = "[s]")
	private static double approachAndStopBallTimePenalty = 0.2;

	@Configurable(defValue = "1", comment = "Number of bots to assign when there are no ball interceptions")
	private static int numBotsForNonInterceptableBall = 1;

	private final Supplier<EBallResponsibility> ballResponsibility;
	private final Supplier<Set<BotID>> potentialOffensiveBots;
	private final Supplier<Map<BotID, RatedBallInterception>> ballInterceptions;
	private final Supplier<List<BotDistance>> tigersToBallDist;

	private Hysteresis ballSpeedHysteresis = new Hysteresis(OffensiveConstants.getAbortBallInterceptionVelThreshold(),
			OffensiveConstants.getBallIsRollingThreshold());

	@Getter
	private List<BotID> ballHandlingBots;


	@Override
	protected void reset()
	{
		ballHandlingBots = Collections.emptyList();
	}


	@Override
	protected boolean isCalculationNecessary()
	{
		return ballResponsibility.get() == EBallResponsibility.OFFENSE;
	}


	@Override
	public void doCalc()
	{
		ballHandlingBots = findBestPrimaries();
	}


	private List<BotID> findBestPrimaries()
	{
		ballSpeedHysteresis.update(getBall().getVel().getLength());
		if (getAiFrame().getGameState().isStoppedGame())
		{
			return getPrimariesDuringStop();
		}

		if (!ballInterceptions.get().isEmpty() && ballSpeedHysteresis.isUpper())
		{
			// in this case a interception exists. However in some cases we rather want to activate another bot that
			// can use approach and stop ball.
			var fastestRobotThatCanReachCurrentBallLocation = potentialOffensiveBots.get().stream()
					.filter(bot -> ballMovesAwayFromBot(getWFrame().getBot(bot).getPos()))
					.min(Comparator.comparingDouble(
							bot -> TrajectoryGenerator.generatePositionTrajectory(getWFrame().getBot(bot), getBall().getPos())
									.getTotalTime()))
					.map(bot -> getWFrame().getBot(bot));

			if (fastestRobotThatCanReachCurrentBallLocation.isPresent() && getBall().getVel().getLength() < 2.0)
			{
				var bot = fastestRobotThatCanReachCurrentBallLocation.get();
				double distance = bot.getPos().distanceTo(getBall().getPos());

				// we assume we can catch up to the ball with 0.5m/s
				double timeToCatchUp = distance / estimatedApproachAndStopBallCatchUpSpeed + approachAndStopBallTimePenalty;

				var interceptions = findFastestInterceptableInterceptions(ballInterceptions.get());
				if (!interceptions.isEmpty())
				{
					var fastestInterception = interceptions.getFirst();
					if (fastestInterception.getBallInterception().getBallContactTime() > timeToCatchUp)
					{
						// approachAndStopBall could be faster than the fastest interception
						return List.of(fastestRobotThatCanReachCurrentBallLocation.get().getBotId());
					}
				}

			}

			return findBestPrimariesByInterception(ballInterceptions.get());
		}

		return getBestPrimariesForNonInterceptableBall();
	}


	private List<BotID> getPrimariesDuringStop()
	{
		Map<BotID, BotDistance> closestBots = new HashMap<>();
		tigersToBallDist.get().forEach(d -> closestBots.put(d.getBotId(), d));

		double distHysteresis = 500;
		ballHandlingBots.stream()
				.map(b -> getWFrame().getBot(b))
				.filter(Objects::nonNull)
				.map(b -> new BotDistance(b.getBotId(), b.getPos().distanceTo(getBall().getPos()) - distHysteresis))
				.forEach(d -> closestBots.put(d.getBotId(), d));

		return closestBots.values().stream()
				.filter(d -> potentialOffensiveBots.get().contains(d.getBotId()))
				.min(Comparator.comparingDouble(BotDistance::getDist))
				.map(BotDistance::getBotId)
				.map(List::of)
				.orElseGet(Collections::emptyList);
	}


	private List<BotID> getBestPrimariesForNonInterceptableBall()
	{
		return getBotToBallTimes().entrySet().stream()
				.sorted(Comparator.comparingDouble(Map.Entry::getValue))
				.limit(numBotsForNonInterceptableBall)
				.map(Map.Entry::getKey)
				.toList();
	}


	private Map<BotID, Double> getBotToBallTimes()
	{
		Map<BotID, Double> trajectoryTimes = new HashMap<>();
		for (var botId : potentialOffensiveBots.get())
		{
			var bot = getWFrame().getBot(botId);
			var trajectoryTime = TrajectoryGenerator.generatePositionTrajectory(bot, getBall().getPos()).getTotalTime();
			if (bot.getBallContact().hadRecentContact())
			{
				trajectoryTime = 0;
			}

			// Hyst
			if (!ballHandlingBots.isEmpty() && ballHandlingBots.getFirst().equals(botId))
			{
				trajectoryTime = Math.max(0, trajectoryTime - hystTimeOffsetForNonMovingBall);
			}
			trajectoryTimes.put(bot.getBotId(), trajectoryTime);
		}
		return trajectoryTimes;
	}


	private List<BotID> findBestPrimariesByInterception(Map<BotID, RatedBallInterception> ballInterceptions)
	{
		var interceptions = findFastestInterceptableInterceptions(ballInterceptions);

		if (interceptions.isEmpty())
		{
			return Collections.emptyList();
		}

		var firstBot = interceptions.getFirst().getBallInterception().getBotID();
		if (!getAiFrame().getGameState().isRunning() || interceptions.size() < 2)
		{
			return List.of(firstBot);
		}

		return List.of(firstBot);
	}


	private List<RatedBallInterception> findFastestInterceptableInterceptions(
			Map<BotID, RatedBallInterception> interceptions)
	{
		return interceptions
				.values()
				.stream()
				.sorted(Comparator.comparing(this::getBallContactTimeWithHyst))
				.toList();
	}


	private double getBallContactTimeWithHyst(RatedBallInterception ballInterception)
	{
		var filteredInterceptionsFromlastFrame = getAiFrame().getPrevFrame().getTacticalField()
				.getBallInterceptions()
				.values()
				.stream()
				.sorted(Comparator.comparing(i -> i.getBallInterception().getBallContactTime()))
				.filter(i -> !Double.isInfinite(i.getBallInterception().getBallContactTime()))
				.limit(2)
				.toList();

		if (filteredInterceptionsFromlastFrame
				.stream()
				.filter(e -> ballHandlingBots.contains(e.getBallInterception().getBotID()))
				.map(i -> i.getBallInterception().getBotID()).toList()
				.contains(ballInterception.getBallInterception().getBotID()))
		{
			return Math.max(0, ballInterception.getBallInterception().getBallContactTime() - hystTimeOffsetForMovingBall);
		}
		return ballInterception.getBallInterception().getBallContactTime();
	}


	private boolean ballMovesAwayFromBot(IVector2 botPos)
	{
		var ballDir = getBall().getVel();
		var botToBallDir = getBall().getPos().subtractNew(botPos);
		var angle = ballDir.angleToAbs(botToBallDir).orElse(0.0);
		return angle < AngleMath.deg2rad(45);
	}
}
