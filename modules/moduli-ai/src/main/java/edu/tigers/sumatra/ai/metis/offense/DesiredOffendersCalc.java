/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense;

import edu.tigers.sumatra.ai.metis.general.ADesiredBotCalc;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ids.BotID;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;


/**
 * Find the desired bots for offense
 */
@Log4j2
public class DesiredOffendersCalc extends ADesiredBotCalc
{
	private final Supplier<Map<EPlay, Integer>> playNumbers;
	private final Supplier<List<BotID>> desiredOffenseBots;


	public DesiredOffendersCalc(
			Supplier<Map<EPlay, Set<BotID>>> desiredBotMap,
			Supplier<Map<EPlay, Integer>> playNumbers,
			Supplier<List<BotID>> desiredOffenseBots)
	{
		super(desiredBotMap);
		this.playNumbers = playNumbers;
		this.desiredOffenseBots = desiredOffenseBots;
	}


	@Override
	public void doCalc()
	{
		int numOffenders = playNumbers.get().getOrDefault(EPlay.OFFENSIVE, 0);
		Set<BotID> desiredBots = desiredOffenseBots.get().stream().limit(numOffenders).collect(Collectors.toSet());
		addDesiredBots(EPlay.OFFENSIVE, desiredBots);
	}
}
