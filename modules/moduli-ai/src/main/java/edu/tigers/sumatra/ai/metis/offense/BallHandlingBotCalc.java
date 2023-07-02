/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.ballresponsibility.EBallResponsibility;
import edu.tigers.sumatra.ai.metis.botdistance.BotDistance;
import edu.tigers.sumatra.ai.metis.offense.ballinterception.RatedBallInterception;
import edu.tigers.sumatra.ids.BotID;
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
	@Configurable(defValue = "0.3", comment = "Max abs diff of slack times for adding a second bot")
	private static double maxSlackTimeDiffForSecondBot = 0.3;

	@Configurable(defValue = "0.1", comment = "Time in seconds to subtract from current max traj time")
	private static double hystTimeOffsetForNonMovingBall = 0.1;

	@Configurable(defValue = "300.0", comment = "Min distance between interceptions positions for adding a second bot")
	private static double minDistForSecondBot = 300.0;

	@Configurable(defValue = "1", comment = "Number of bots to assign when there are no ball interceptions")
	private static int numBotsForNonInterceptableBall = 1;

	@Configurable(defValue = "true")
	private static boolean allowDoubleAttackerForBallInterception = true;

	private final Supplier<EBallResponsibility> ballResponsibility;
	private final Supplier<Set<BotID>> potentialOffensiveBots;
	private final Supplier<Map<BotID, RatedBallInterception>> ballInterceptions;
	private final Supplier<List<BotDistance>> tigersToBallDist;

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
		if (getAiFrame().getGameState().isStoppedGame())
		{
			return getPrimariesDuringStop();
		}

		if (!ballInterceptions.get().isEmpty())
		{
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
			if (bot.hasBallContact())
			{
				trajectoryTime = 0;
			}

			// Hyst
			if (!ballHandlingBots.isEmpty() && ballHandlingBots.get(0).equals(botId))
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

		var firstBot = interceptions.get(0).getBallInterception().getBotID();
		if (!getAiFrame().getGameState().isRunning() || interceptions.size() < 2)
		{
			return List.of(firstBot);
		}

		if (allowDoubleAttackerForBallInterception)
		{
			var secondBot = interceptions.get(1).getBallInterception().getBotID();
			if (isAddingSecondPrimaryReasonable(firstBot, secondBot, ballInterceptions))
			{
				return List.of(firstBot, secondBot);
			}
		}

		return List.of(firstBot);
	}


	private List<RatedBallInterception> findFastestInterceptableInterceptions(
			Map<BotID, RatedBallInterception> interceptions)
	{
		return interceptions
				.values()
				.stream()
				.sorted(Comparator.comparing(e -> e.getBallInterception().getBallContactTime()))
				.toList();
	}


	private boolean isAddingSecondPrimaryReasonable(BotID bestPrimary, BotID secondBestPrimary,
			Map<BotID, RatedBallInterception> ballInterceptions)
	{
		var bestBotBallInterception = ballInterceptions.get(bestPrimary);
		var secondBestBotBallInterception = ballInterceptions.get(secondBestPrimary);

		var bestBotPos = bestBotBallInterception.getBallInterception().getPos();
		var secondBestBotPos = secondBestBotBallInterception.getBallInterception().getPos();

		// negative slack time means that robot reaches the intercept pos before the ball
		var firstPrimarySlackTime = bestBotBallInterception.getBallInterception().getBallContactTime();
		var secondPrimarySlackTime = secondBestBotBallInterception.getBallInterception().getBallContactTime();

		return Math.abs(firstPrimarySlackTime - secondPrimarySlackTime) < maxSlackTimeDiffForSecondBot
				&& bestBotPos.distanceTo(secondBestBotPos) > minDistForSecondBot;
	}
}
