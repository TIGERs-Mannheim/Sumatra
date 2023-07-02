/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense;

import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ids.BotID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;


@RequiredArgsConstructor
public class PotentialOffensiveBotsCalc extends ACalculator
{
	private final Supplier<Boolean> ballLeavingFieldGood;
	private final Supplier<Set<BotID>> crucialDefenders;
	private final Supplier<List<BotID>> botsToInterchange;

	@Getter
	private Set<BotID> potentialOffensiveBots;


	@Override
	protected void doCalc()
	{
		potentialOffensiveBots = getPotentialOffensiveBotMap();
	}


	private Set<BotID> getPotentialOffensiveBotMap()
	{
		if (ballLeavingFieldGood.get())
		{
			return Collections.emptySet();
		}

		Set<BotID> bots = new HashSet<>(getAiFrame().getWorldFrame().getTigerBotsAvailable().keySet());
		crucialDefenders.get().forEach(bots::remove);
		botsToInterchange.get().forEach(bots::remove);
		bots.remove(getAiFrame().getKeeperId());

		return bots;
	}
}
