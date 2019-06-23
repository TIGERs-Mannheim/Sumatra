/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.interchange;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.ai.metis.general.PlayNumberCalc;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ids.BotID;


/**
 * Set the desired bots to be interchanged based on the number of bots from {@link PlayNumberCalc}.
 */
public class DesiredInterchangeBotsCalc extends ACalculator
{
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		int numBotsToInterchange = newTacticalField.getPlayNumbers().getOrDefault(EPlay.INTERCHANGE, 0);
		final Set<BotID> interchangeBots = newTacticalField.getBotInterchange().getDesiredInterchangeBots().stream()
				.limit(numBotsToInterchange).collect(Collectors.toSet());
		newTacticalField.addDesiredBots(EPlay.INTERCHANGE, new HashSet<>(interchangeBots));
	}
}
