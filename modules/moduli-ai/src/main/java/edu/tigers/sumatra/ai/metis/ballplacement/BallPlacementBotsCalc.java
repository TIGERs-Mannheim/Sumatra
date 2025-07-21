/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.ballplacement;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;


@RequiredArgsConstructor
public class BallPlacementBotsCalc extends ACalculator
{
	@Configurable(defValue = "true")
	private static boolean useSecondBallPlacer = true;

	private final Supplier<List<BotID>> botsToInterchange;

	@Getter
	private List<BotID> preferredBallPlacementBots;

	private BotID lastPrimaryBot;


	@Override
	protected void doCalc()
	{
		var availableBots = getAiFrame().getWorldFrame().getTigerBotsAvailable().values().stream()
				.filter(bot -> !botsToInterchange.get().contains(bot.getBotId()))
				.filter(bot -> !bot.getBotId().equals(getAiFrame().getKeeperId()))
				.toList();

		var lastPlacementBots = getAiFrame().getPrevFrame().getTacticalField().getDesiredBotMap()
				.getOrDefault(EPlay.BALL_PLACEMENT, Set.of());
		var lastAssistantBot = lastPlacementBots.stream().filter(b -> !b.equals(lastPrimaryBot)).findFirst().orElse(null);

		var ballPlacementBotChooser = new BallPlacementBotChooser(
				getBall(),
				getPlacementPos(),
				availableBots,
				lastPrimaryBot,
				lastAssistantBot
		);

		Optional<BotID> primaryBot = ballPlacementBotChooser.choosePrimary();
		if (primaryBot.isEmpty())
		{
			lastPrimaryBot = null;
			return;
		}
		lastPrimaryBot = primaryBot.get();
		if (useSecondBallPlacer)
		{
			List<BotID> orderedAssistants = ballPlacementBotChooser.getOrderedAssistants(lastPrimaryBot);
			preferredBallPlacementBots = Stream.concat(
					Stream.of(lastPrimaryBot),
					orderedAssistants.stream().limit(1)
			).toList();
		} else
		{
			preferredBallPlacementBots = List.of(lastPrimaryBot);
		}
	}


	@Override
	public boolean isCalculationNecessary()
	{
		return getAiFrame().getGameState().isBallPlacementForUs();
	}


	@Override
	protected void reset()
	{
		preferredBallPlacementBots = List.of();
	}


	private IVector2 getPlacementPos()
	{
		return Optional.ofNullable(getAiFrame().getGameState().getBallPlacementPositionForUs()).orElse(Vector2.zero());
	}


	public int getNumBallPlacementBots()
	{
		return preferredBallPlacementBots.size();
	}
}
