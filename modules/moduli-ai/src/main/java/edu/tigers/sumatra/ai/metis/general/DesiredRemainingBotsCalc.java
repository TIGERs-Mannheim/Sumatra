/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.general;

import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ids.BotID;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;


/**
 * Associate the remaining robots to plays according to playNumbers.
 */
public class DesiredRemainingBotsCalc extends ADesiredBotCalc
{
	private final Supplier<Map<EPlay, Integer>> playNumbers;


	public DesiredRemainingBotsCalc(
			Supplier<Map<EPlay, Set<BotID>>> desiredBotMap,
			Supplier<Map<EPlay, Integer>> playNumbers)
	{
		super(desiredBotMap);
		this.playNumbers = playNumbers;
	}


	@Override
	public void doCalc()
	{
		for (Map.Entry<EPlay, Integer> entry : playNumbers.get().entrySet())
		{
			var play = entry.getKey();
			var numBots = entry.getValue();
			if (!desiredBotMap.get().containsKey(play))
			{
				addDesiredBots(play, getUnassignedBots(numBots));
			}
		}
	}
}
