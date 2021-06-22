/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.keeper;

import edu.tigers.sumatra.ai.metis.general.ADesiredBotCalc;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ids.BotID;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;


/**
 * Set desired keeper id, if keeper id is present
 */
public class DesiredKeeperCalc extends ADesiredBotCalc
{
	private final Supplier<Map<EPlay, Integer>> playNumbers;


	public DesiredKeeperCalc(
			Supplier<Map<EPlay, Set<BotID>>> desiredBotMap,
			Supplier<Map<EPlay, Integer>> playNumbers)
	{
		super(desiredBotMap);
		this.playNumbers = playNumbers;
	}


	@Override
	public void doCalc()
	{
		if (playNumbers.get().getOrDefault(EPlay.KEEPER, 0) > 0)
		{
			addDesiredBots(EPlay.KEEPER, Collections.singleton(getAiFrame().getKeeperId()));
		}
	}
}
