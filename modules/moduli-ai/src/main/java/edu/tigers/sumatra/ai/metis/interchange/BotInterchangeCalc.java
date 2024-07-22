/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.interchange;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;


/**
 * Determine the number of bots to be interchanged. Bots are interchanged during STOP and also in any gameState
 * if the number of robots is reduced (yellow card).
 */
@RequiredArgsConstructor
public class BotInterchangeCalc extends ACalculator
{
	@Configurable(comment = "Should automatic robot interchange be processed?", defValue = "true")
	private static boolean robotInterchangeEnabled = true;

	@Configurable(comment = "Should automatic robot interchange also remove weak bots?", defValue = "true")
	private static boolean interchangeWeakBots = true;

	@Configurable(comment = "Should bots also interchange during running game?", defValue = "true")
	private static boolean interchangeDuringRunning = true;

	@Configurable(comment = "Should bots interchange in powerplay when yellow card was given?", defValue = "false")
	private static boolean activeForPowerPlay = false;

	@Getter
	private List<BotID> botsToInterchange = new ArrayList<>();

	@Getter
	private int numInterchangeBots;

	private boolean isPowerPlay;

	private final Supplier<List<BotID>> weakBots;


	@Override
	public boolean isCalculationNecessary()
	{
		// We normally do not want any bots waiting for interchange during a running game
		// We might consider situation where we have the advantage and a forced stop could disturb us
		isPowerPlay = getWFrame().getTigerBotsAvailable().size() > getWFrame().getOpponentBots().size();
		return robotInterchangeEnabled && (getAiFrame().getGameState().isStoppedGame() || interchangeDuringRunning ||
				(isPowerPlay && activeForPowerPlay));
	}


	@Override
	protected void reset()
	{
		botsToInterchange = Collections.emptyList();
		numInterchangeBots = 0;
	}


	@Override
	public void doCalc()
	{
		if (isMaintenanceAfterGame())
		{
			botsToInterchange = getWFrame().getTigerBotsAvailable().keySet().stream().toList();
			numInterchangeBots = botsToInterchange.size();
			return;
		}
		// substitution intent (removed!) on our side is usually triggered when a bot does bullshit or loses parts,
		// in that case the weakest bot is usually not the bot wanted to be exchanged
		int numWeakBots = 0;
		int numBotsToBeRemoved = Math.max(0, getWFrame().getTigerBotsAvailable().size() - numberOfAllowedBots());
		if (interchangeWeakBots)
		{
			numWeakBots = weakBots.get().size();
		}
		int numBotsToInterchange = Math.max(numBotsToBeRemoved, numWeakBots);
		// During running, we only want to interchange bots because of yellow cards
		if (getAiFrame().getGameState().isRunning() && (interchangeDuringRunning || (activeForPowerPlay && isPowerPlay)))
		{
			numBotsToInterchange = numBotsToBeRemoved;
		}

		botsToInterchange = weakestBots(numBotsToInterchange);
		numInterchangeBots = botsToInterchange.size();
	}


	private int numberOfAllowedBots()
	{
		return getAiFrame().getRefereeMsg().getTeamInfo(getAiFrame().getTeamColor()).getMaxAllowedBots();
	}

	private List<BotID> weakestBots(final long numberOfBotsToBeRemoved)
	{
		Set<BotID> weakestBots = new LinkedHashSet<>(botsToInterchange);
		weakestBots.addAll(weakBots.get());
		weakestBots.addAll(allBotsSortedByBattery());
		return weakestBots.stream()
				.filter(b -> getWFrame().getBot(b) != null)
				.filter(b -> b != getAiFrame().getKeeperId())
				.limit(numberOfBotsToBeRemoved)
				.filter(botID -> getWFrame().getTigerBotsAvailable().containsKey(botID))
				.toList();
	}


	private List<BotID> allBotsSortedByBattery()
	{
		return getWFrame().getTigerBotsAvailable().values().stream()
				.sorted((bot1, bot2) -> Float.compare(bot1.getRobotInfo().getBatteryRelative(),
						bot2.getRobotInfo().getBatteryRelative()))
				.map(ITrackedBot::getBotId)
				.toList();
	}

	private boolean isMaintenanceAfterGame()
	{
		boolean isPostGame = getAiFrame().getGameState().getState() == EGameState.POST_GAME;
		Map<ETeamColor, Integer> goals = getAiFrame().getRefereeMsg().getGoals();
		ETeamColor ourTeam = getAiFrame().getTeamColor();
		int ourGoals = goals.get(ourTeam);
		int theirGoals = goals.get(ourTeam.opposite());
		boolean cheering = ourGoals > theirGoals || (ourGoals == theirGoals && ourTeam == ETeamColor.YELLOW);
		return isPostGame && !cheering;
	}
}
