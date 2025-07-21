/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.metis.ballplacement;

import edu.tigers.sumatra.ai.metis.general.ADesiredBotCalc;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ids.BotID;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;


/**
 * Select the desired bots for ball placement.
 */
@Log4j2
public class DesiredBallPlacementBotsCalc extends ADesiredBotCalc
{
	private final Supplier<Map<EPlay, Integer>> playNumbers;
	private final Supplier<List<BotID>> preferredBotsSupplier;


	public DesiredBallPlacementBotsCalc(
			Supplier<Map<EPlay, Set<BotID>>> desiredBotMap,
			Supplier<Map<EPlay, Integer>> playNumbers,
			Supplier<List<BotID>> preferredBotsSupplier
	)
	{
		super(desiredBotMap);
		this.playNumbers = playNumbers;
		this.preferredBotsSupplier = preferredBotsSupplier;
	}


	@Override
	public boolean isCalculationNecessary()
	{
		return !playNumbers.get().isEmpty() && getAiFrame().getGameState().isBallPlacementForUs();
	}


	@Override
	public void doCalc()
	{
		addDesiredBots(EPlay.BALL_PLACEMENT, getDesiredBots());
	}


	private Set<BotID> getDesiredBots()
	{
		var bots = new ArrayList<>(preferredBotsSupplier.get());
		bots.removeIf(id -> !this.isAssignable(id));

		return bots.stream()
				.limit(playNumbers.get().get(EPlay.BALL_PLACEMENT))
				.collect(Collectors.toSet());
	}
}
