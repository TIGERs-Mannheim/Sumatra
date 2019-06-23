/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.interchange;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.referee.data.GameState;
import edu.tigers.sumatra.referee.data.TeamInfo;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * Determine the number of bots to be interchanged. Bots are only interchanged during STOP right now.
 */
public class NumInterchangeBotsCalc extends ACalculator
{
	@Configurable(comment = "Should automatic robot interchange be processed?", defValue = "true")
	private static boolean robotInterchangeEnabled = true;
	
	@Configurable(comment = "Should automatic robot interchange also remove weak bots?", defValue = "true")
	private static boolean interchangeWeakBots = true;
	
	private List<BotID> lastBotsToInterchange = new ArrayList<>();
	
	
	@Override
	public boolean isCalculationNecessary(final TacticalField tacticalField, final BaseAiFrame aiFrame)
	{
		return robotInterchangeEnabled;
	}
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		GameState gameState = baseAiFrame.getGamestate();
		int numBotsToInterchange;
		if (gameState.isStoppedGame())
		{
			int numWeakBots = 0;
			if (interchangeWeakBots)
			{
				numWeakBots = newTacticalField.getBotInterchange().getWeakBots().size();
			}
			int numBotsToBeRemoved = getWFrame().getTigerBotsVisible().size() - numberOfAllowedBots();
			numBotsToInterchange = Math.max(numBotsToBeRemoved, numWeakBots);
		} else
		{
			numBotsToInterchange = 0;
		}
		
		newTacticalField.getBotInterchange().setNumInterchangeBots(numBotsToInterchange);
		
		lastBotsToInterchange = weakestBots(numBotsToInterchange);
		newTacticalField.getBotInterchange().setDesiredInterchangeBots(new HashSet<>(lastBotsToInterchange));
	}
	
	
	private int numberOfAllowedBots()
	{
		TeamInfo teamInfo = getAiFrame().getRefereeMsg().getTeamInfo(getAiFrame().getTeamColor());
		int nCards = teamInfo.getYellowCardsTimes().size() + teamInfo.getRedCards();
		return RuleConstraints.getBotsPerTeam() - nCards;
	}
	
	
	private List<BotID> weakestBots(final long numberOfBotsToBeRemoved)
	{
		Set<BotID> weakestBots = new LinkedHashSet<>(lastBotsToInterchange);
		weakestBots.addAll(getNewTacticalField().getBotInterchange().getWeakBots());
		weakestBots.addAll(allBotsSortedByBattery());
		return weakestBots.stream()
				.filter(b -> b != getAiFrame().getKeeperId())
				.limit(numberOfBotsToBeRemoved)
				.collect(Collectors.toList());
	}
	
	
	private List<BotID> allBotsSortedByBattery()
	{
		return getWFrame().getTigerBotsAvailable().values().stream()
				.sorted((bot1, bot2) -> Float.compare(bot1.getRobotInfo().getBattery(), bot2.getRobotInfo().getBattery()))
				.map(ITrackedBot::getBotId)
				.collect(Collectors.toList());
	}
}
