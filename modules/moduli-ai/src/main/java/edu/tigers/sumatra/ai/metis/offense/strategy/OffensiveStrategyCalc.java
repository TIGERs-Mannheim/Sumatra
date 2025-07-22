/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.strategy;

import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.awt.Color;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;


/**
 * Calculates the {@link OffensiveStrategy} for the overall attacking plan
 */
@RequiredArgsConstructor
public class OffensiveStrategyCalc extends ACalculator
{
	private static final Color COLOR = new Color(30, 100, 184);


	private final Supplier<EOffensiveStrategy> ballHandlingRobotsStrategy;
	private final Supplier<List<BotID>> ballHandlingBots;
	private final Supplier<List<BotID>> passReceivers;
	private final Supplier<Map<BotID, IVector2>> supportiveAttackers;

	@Getter
	private OffensiveStrategy strategy;


	@Override
	public void doCalc()
	{
		Map<BotID, EOffensiveStrategy> strategyMap = new HashMap<>();
		ballHandlingBots.get().forEach(bot -> strategyMap.put(bot, ballHandlingRobotsStrategy.get()));
		supportiveAttackers.get().keySet()
				.forEach(botID -> strategyMap.put(botID, EOffensiveStrategy.SUPPORTIVE_ATTACKER));
		passReceivers.get().forEach(botID -> strategyMap.put(botID, EOffensiveStrategy.RECEIVE_PASS));

		var attackerBot = ballHandlingBots.get().stream().findFirst().orElse(null);
		strategy = new OffensiveStrategy(attackerBot, Collections.unmodifiableMap(strategyMap));

		drawStrategy(strategy);
	}


	private void drawStrategy(final OffensiveStrategy offensiveStrategy)
	{
		for (var entry : offensiveStrategy.getCurrentOffensivePlayConfiguration().entrySet())
		{
			ITrackedBot tBot = getWFrame().getBot(entry.getKey());
			getShapes(EAiShapesLayer.OFFENSE_STRATEGY_DEBUG)
					.add(new DrawableAnnotation(tBot.getPos(), entry.getValue().name(), COLOR)
							.withCenterHorizontally(true)
							.withOffset(Vector2f.fromY(-170)));
		}
	}
}
