/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.interchange;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.referee.data.GameState;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * Determine the number of bots to be interchanged. Bots are interchanged during STOP and also in any gameState
 * if the number of robots is reduced (yellow card).
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
			boolean substitutionFlag = baseAiFrame.getRefereeMsg().getTeamInfo(baseAiFrame.getTeamColor())
					.isBotSubstitutionIntent();
			int numWeakBots = 0;
			int numBotsToBeRemoved = Math.max(0, getWFrame().getTigerBotsVisible().size() - numberOfAllowedBots());
			if (interchangeWeakBots)
			{
				numWeakBots = newTacticalField.getBotInterchange().getWeakBots().size();
			}
			
			numBotsToInterchange = Math.max(numBotsToBeRemoved, numWeakBots);
			if (substitutionFlag)
			{
				numBotsToInterchange += 1;
			}
			lastBotsToInterchange = weakestBots(numBotsToInterchange);
			
		} else
		{
			// We do not want any bots waiting for interchange during a running game
			numBotsToInterchange = 0;
			lastBotsToInterchange = Collections.emptyList();
		}
		
		newTacticalField.getBotInterchange().setNumInterchangeBots(numBotsToInterchange);
		newTacticalField.getBotInterchange().setDesiredInterchangeBots(new HashSet<>(lastBotsToInterchange));
	}
	
	
	private int numberOfAllowedBots()
	{
		return getAiFrame().getRefereeMsg().getTeamInfo(getAiFrame().getTeamColor()).getMaxAllowedBots();
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
