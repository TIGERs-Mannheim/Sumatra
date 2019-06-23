/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.general;

import java.util.Collection;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ids.BotID;


/**
 * @author Sebastian Stein <sebastian-stein@gmx.de>
 */
public class DesiredGameStateBotsCalc extends ACalculator
{
	
	@Override
	public void doCalc(final TacticalField tacticalField, final BaseAiFrame aiFrame)
	{
		Set<BotID> availableBots = new HashSet<>(aiFrame.getWorldFrame().getTigerBotsAvailable().keySet());
		Set<BotID> assignedBots = tacticalField.getDesiredBotMap().values().stream().flatMap(Collection::stream)
				.collect(Collectors.toSet());
		availableBots.removeAll(assignedBots);
		
		Map<EPlay, Set<BotID>> desiredBots = new EnumMap<>(EPlay.class);
		for (Map.Entry<EPlay, Integer> entry : tacticalField.getPlayNumbers().entrySet())
		{
			if (!(tacticalField.getDesiredBotMap().keySet().contains(entry.getKey()) || entry.getKey() == EPlay.SUPPORT))
			{
				desiredBots.put(entry.getKey(), getNBots(entry.getValue(), availableBots));
			}
		}
		
		for (Map.Entry<EPlay, Set<BotID>> entry : desiredBots.entrySet())
		{
			tacticalField.addDesiredBots(entry.getKey(), entry.getValue());
		}
		
		exchangeDoubleAssignedBots(tacticalField, availableBots);
	}
	
	
	private Set<BotID> getNBots(final int i, final Set<BotID> availableBots)
	{
		
		Set<BotID> resultingBots = availableBots.stream().limit(i).collect(Collectors.toSet());
		availableBots.removeAll(resultingBots);
		
		return resultingBots;
	}
	
	
	/**
	 * used to change desired roles of defense:
	 * when automatedThrowInPlay desires a bot already desired by defense, defense gets another unassigned bot instead
	 *
	 * @param tacticalField
	 * @param availableBots bots that are not assigned to any role yet
	 */
	private void exchangeDoubleAssignedBots(TacticalField tacticalField, Set<BotID> availableBots)
	{
		Map<EPlay, Set<BotID>> desiredBotMap = tacticalField.getDesiredBotMap();
		if (desiredBotMap.containsKey(EPlay.AUTOMATED_THROW_IN))
		{
			int lostBots = 0;
			Set<BotID> defenseBots = new HashSet<>();
			for (BotID id : desiredBotMap.get(EPlay.DEFENSIVE))
			{
				if (!desiredBotMap.get(EPlay.AUTOMATED_THROW_IN).contains(id))
				{
					defenseBots.add(id);
				} else
				{
					lostBots += 1;
				}
			}
			Set<BotID> resultingBots = availableBots.stream().limit(lostBots).collect(Collectors.toSet());
			defenseBots.addAll(resultingBots);
			tacticalField.addDesiredBots(EPlay.DEFENSIVE, defenseBots);
		}
	}
}
