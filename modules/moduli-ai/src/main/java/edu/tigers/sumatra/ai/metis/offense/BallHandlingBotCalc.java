/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.ballresponsibility.EBallResponsibility;
import edu.tigers.sumatra.ai.metis.botdistance.BotDistance;
import edu.tigers.sumatra.ai.metis.offense.ballinterception.BallInterception;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.pathfinder.TrajectoryGenerator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;


/**
 * find the best offensive robot to interact with the ball
 */
@RequiredArgsConstructor
public class BallHandlingBotCalc extends ACalculator
{
	@Configurable(defValue = "0.3", comment = "Max abs diff of slack times for adding a second bot")
	private static double maxSlackTimeDiffForSecondBot = 0.3;

	@Configurable(defValue = "0.3", comment = "Min 'ballContactTime' for adding a second bot")
	private static double minBallContactTimeForSecondBot = 0.3;

	@Configurable(defValue = "300.0", comment = "Min distance between interceptions positions for adding a second bot")
	private static double minDistForSecondBot = 300.0;

	@Configurable(defValue = "1", comment = "Number of bots to assign when there are no ball interceptions")
	private static int numBotsForNonInterceptableBall = 1;

	private final Supplier<EBallResponsibility> ballResponsibility;
	private final Supplier<Set<BotID>> potentialOffensiveBots;
	private final Supplier<Map<BotID, BallInterception>> ballInterceptions;
	private final Supplier<Map<BotID, BallInterception>> ballInterceptionsFallback;
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
		return ballResponsibility.get() == EBallResponsibility.OFFENSE
				&& !getAiFrame().getGameState().isPenaltyOrPreparePenalty();
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

		if (!ballInterceptionsFallback.get().isEmpty())
		{
			var interceptors = findBestPrimariesByInterception(ballInterceptionsFallback.get());
			if (!interceptors.isEmpty())
			{
				return interceptors;
			}
		}

		return getBestPrimariesForNonInterceptableBall();
	}


	private List<BotID> getPrimariesDuringStop()
	{
		Map<BotID, BotDistance> closestBots = new IdentityHashMap<>();
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
				.collect(Collectors.toList());
	}


	private Map<BotID, Double> getBotToBallTimes()
	{
		Map<BotID, Double> trajectoryTimes = new HashMap<>();
		for (var botId : potentialOffensiveBots.get())
		{
			var bot = getWFrame().getBot(botId);
			var trajectory = TrajectoryGenerator.generatePositionTrajectory(bot, getBall().getPos());
			trajectoryTimes.put(bot.getBotId(), trajectory.getTotalTime());
		}
		return trajectoryTimes;
	}


	private List<BotID> findBestPrimariesByInterception(Map<BotID, BallInterception> ballInterceptions)
	{
		var interceptions = findFastestInterceptableInterceptions(ballInterceptions);

		if (interceptions.isEmpty())
		{
			return Collections.emptyList();
		}

		var firstBot = interceptions.get(0).getBotID();
		if (!getAiFrame().getGameState().isRunning() || interceptions.size() < 2)
		{
			return List.of(firstBot);
		}

		var secondBot = interceptions.get(1).getBotID();
		if (isAddingSecondPrimaryReasonable(firstBot, secondBot, ballInterceptions))
		{
			return List.of(firstBot, secondBot);
		}
		return List.of(firstBot);
	}


	private List<BallInterception> findFastestInterceptableInterceptions(Map<BotID, BallInterception> interceptions)
	{
		return interceptions
				.values()
				.stream()
				.sorted(Comparator.comparing(BallInterception::getBallContactTime))
				.collect(Collectors.toList());
	}


	private boolean isAddingSecondPrimaryReasonable(BotID bestPrimary, BotID secondBestPrimary,
			Map<BotID, BallInterception> ballInterceptions)
	{
		var bestBotBallInterception = ballInterceptions.get(bestPrimary);
		var secondBestBotBallInterception = ballInterceptions.get(secondBestPrimary);

		var bestBotPos = bestBotBallInterception.getPos();
		var secondBestBotPos = secondBestBotBallInterception.getPos();

		// negative slack time means that robot reaches the intercept pos before the ball
		var firstPrimarySlackTime = bestBotBallInterception.getBallContactTime();
		var secondPrimarySlackTime = secondBestBotBallInterception.getBallContactTime();

		return Math.abs(firstPrimarySlackTime - secondPrimarySlackTime) < maxSlackTimeDiffForSecondBot
				&& bestBotPos.distanceTo(secondBestBotPos) > minDistForSecondBot
				&& bestBotBallInterception.getBallContactTime() > minBallContactTimeForSecondBot;
	}
}
