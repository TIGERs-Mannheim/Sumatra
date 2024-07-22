/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.metis.ballplacement;

import edu.tigers.sumatra.ai.metis.general.ADesiredBotCalc;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;


/**
 * Select the desired bots for ball placement.
 */
public class DesiredBallPlacementBotsCalc extends ADesiredBotCalc
{
	private final Supplier<Map<EPlay, Integer>> playNumbers;

	private Set<BotID> lastDesiredBots;


	public DesiredBallPlacementBotsCalc(
			Supplier<Map<EPlay, Set<BotID>>> desiredBotMap,
			Supplier<Map<EPlay, Integer>> playNumbers)
	{
		super(desiredBotMap);
		this.playNumbers = playNumbers;
	}


	@Override
	public boolean isCalculationNecessary()
	{
		return getAiFrame().getGameState().isBallPlacementForUs();
	}


	@Override
	protected void reset()
	{
		lastDesiredBots = Collections.emptySet();
	}


	@Override
	public void doCalc()
	{
		lastDesiredBots = getDesiredBots();
		addDesiredBots(EPlay.BALL_PLACEMENT, lastDesiredBots);
	}


	private IVector2 getPlacementPos()
	{
		return Optional.ofNullable(getAiFrame().getGameState().getBallPlacementPositionForUs()).orElse(Vector2.zero());
	}


	private Set<BotID> getDesiredBots()
	{
		final Set<BotID> newDesiredBots = calcNewDesiredBots();
		if (lastDesiredBots.stream().anyMatch(b -> !isAssignable(b))
				|| lastDesiredBots.size() != newDesiredBots.size())
		{
			return newDesiredBots;
		}
		return lastDesiredBots;
	}


	private Set<BotID> calcNewDesiredBots()
	{
		Set<BotID> desiredBots = new HashSet<>();

		if (playNumbers.get().isEmpty())
		{
			return Collections.emptySet();
		}

		List<BotID> availableBots = new ArrayList<>(getUnassignedBots());

		IVector2 botTargetPos = getBall().getTrajectory().closestPointTo(getPlacementPos());
		getUnassignedBots().stream()
				.min(Comparator.comparing(b -> getWFrame().getBot(b).getPos().distanceTo(botTargetPos)))
				.ifPresent(b -> {
					desiredBots.add(b);
					availableBots.remove(b);
				});

		if (playNumbers.get().get(EPlay.BALL_PLACEMENT) == 1)
		{
			return desiredBots;
		}

		availableBots.stream()
				.min(Comparator.comparingDouble(b -> getWFrame().getBot(b).getPos().distanceTo(getPlacementPos())))
				.ifPresent(desiredBots::add);
		return desiredBots;
	}
}
