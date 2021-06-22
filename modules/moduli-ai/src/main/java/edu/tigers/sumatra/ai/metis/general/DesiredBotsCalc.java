/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.general;

import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ids.BotID;
import lombok.Getter;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;


/**
 * Initialize the desired bots map.
 */
public class DesiredBotsCalc extends ACalculator
{
	@Getter
	private Map<EPlay, Set<BotID>> desiredBots;


	@Override
	protected void doCalc()
	{
		desiredBots = new EnumMap<>(EPlay.class);
	}
}
