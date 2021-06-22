/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.interchange;

import edu.tigers.sumatra.ai.metis.general.ADesiredBotCalc;
import edu.tigers.sumatra.ai.metis.general.PlayNumberCalc;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ids.BotID;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;


/**
 * Set the desired bots to be interchanged based on the number of bots from {@link PlayNumberCalc}.
 */
public class DesiredInterchangeBotsCalc extends ADesiredBotCalc
{
	private final Supplier<Map<EPlay, Integer>> playNumbers;
	private final Supplier<List<BotID>> botsToInterchange;


	public DesiredInterchangeBotsCalc(
			Supplier<Map<EPlay, Set<BotID>>> desiredBotMap,
			Supplier<Map<EPlay, Integer>> playNumbers,
			Supplier<List<BotID>> botsToInterchange
	)
	{
		super(desiredBotMap);
		this.playNumbers = playNumbers;
		this.botsToInterchange = botsToInterchange;
	}


	@Override
	public void doCalc()
	{
		int numBotsToInterchange = playNumbers.get().getOrDefault(EPlay.INTERCHANGE, 0);
		final Set<BotID> interchangeBots = botsToInterchange.get().stream()
				.limit(numBotsToInterchange).collect(Collectors.toSet());
		addDesiredBots(EPlay.INTERCHANGE, interchangeBots);
	}
}
