/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense;

import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ids.BotID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@RequiredArgsConstructor
public class DesiredOffenseBotsCalc extends ACalculator
{
	private final Supplier<List<BotID>> ballHandlingBots;
	private final Supplier<List<BotID>> supportiveAttackers;
	private final Supplier<List<BotID>> passReceivers;

	@Getter
	private List<BotID> desiredOffenseBots;

	@Getter
	private int numOffenseBots;


	@Override
	protected void doCalc()
	{
		desiredOffenseBots = Stream.of(ballHandlingBots, supportiveAttackers, passReceivers)
				.map(Supplier::get)
				.flatMap(Collection::stream)
				.distinct()
				.collect(Collectors.toUnmodifiableList());
		numOffenseBots = desiredOffenseBots.size();
	}
}
