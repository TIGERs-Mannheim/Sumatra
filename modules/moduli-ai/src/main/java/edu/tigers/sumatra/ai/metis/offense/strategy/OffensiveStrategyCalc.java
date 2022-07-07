/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.strategy;

import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.offense.OffensiveConstants;
import edu.tigers.sumatra.ai.metis.offense.statistics.OffensiveStatisticsFrame;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.ids.BotID;
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
	private final Supplier<OffensiveStatisticsFrame> offensiveStatisticsFrame;
	private final Supplier<List<BotID>> ballHandlingBots;
	private final Supplier<List<BotID>> passReceivers;
	private final Supplier<List<BotID>> supportiveAttackers;
	private final Supplier<List<BotID>> desiredOffenseBots;

	@Getter
	private OffensiveStrategy strategy;


	@Override
	public void doCalc()
	{
		Map<BotID, EOffensiveStrategy> strategyMap = new HashMap<>();
		ballHandlingBots.get().forEach(bot -> strategyMap.put(bot, ballHandlingRobotsStrategy.get()));
		supportiveAttackers.get().forEach(botID -> strategyMap.put(botID, EOffensiveStrategy.SUPPORTIVE_ATTACKER));
		passReceivers.get().forEach(botID -> strategyMap.put(botID, EOffensiveStrategy.RECEIVE_PASS));

		var attackerBot = ballHandlingBots.get().stream().findFirst().orElse(null);
		strategy = new OffensiveStrategy(attackerBot, Collections.unmodifiableMap(strategyMap));

		statistics();
		drawStrategy(strategy);
	}


	private void drawStrategy(final OffensiveStrategy offensiveStrategy)
	{
		for (var entry : offensiveStrategy.getCurrentOffensivePlayConfiguration().entrySet())
		{
			ITrackedBot tBot = getWFrame().getBot(entry.getKey());
			getShapes(EAiShapesLayer.OFFENSIVE_STRATEGY_DEBUG)
					.add(new DrawableAnnotation(tBot.getPos(), entry.getValue().name(), COLOR)
							.withCenterHorizontally(true)
							.withOffset(Vector2f.fromY(-170)));
		}
	}


	private void statistics()
	{
		if (!OffensiveConstants.isEnableOffensiveStatistics())
		{
			return;
		}

		OffensiveStatisticsFrame sFrame = offensiveStatisticsFrame.get();
		sFrame.setDesiredNumBots(desiredOffenseBots.get().size());
		sFrame.setPrimaryOffensiveBot(ballHandlingBots.get().stream().findFirst().orElse(BotID.noBot()));

		for (BotID key : getWFrame().getTigerBotsAvailable().keySet())
		{
			EOffensiveStrategy eStrat = strategy.getCurrentOffensivePlayConfiguration().get(key);
			sFrame.getBotFrames().get(key).setActiveStrategy(eStrat);
		}
	}
}
