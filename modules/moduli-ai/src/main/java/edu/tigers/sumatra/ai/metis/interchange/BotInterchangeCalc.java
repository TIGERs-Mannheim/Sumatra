/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.interchange;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
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

	@Getter
	private List<BotID> botsToInterchange = new ArrayList<>();

	@Getter
	private int numInterchangeBots;


	private final Supplier<List<BotID>> weakBots;


	@Override
	public boolean isCalculationNecessary()
	{
		// We do not want any bots waiting for interchange during a running game
		return robotInterchangeEnabled && getAiFrame().getGameState().isStoppedGame();
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
		boolean substitutionFlag = getAiFrame().getRefereeMsg().getTeamInfo(getAiFrame().getTeamColor())
				.isBotSubstitutionIntent();
		int numWeakBots = 0;
		int numBotsToBeRemoved = Math.max(0, getWFrame().getTigerBotsVisible().size() - numberOfAllowedBots());
		if (interchangeWeakBots)
		{
			numWeakBots = weakBots.get().size();
		}

		int numBotsToInterchange = Math.max(numBotsToBeRemoved, numWeakBots);
		if (substitutionFlag)
		{
			numBotsToInterchange += 1;
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
				.filter(b -> b != getAiFrame().getKeeperId())
				.limit(numberOfBotsToBeRemoved)
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
}
