/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.strategy;

import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.ai.metis.offense.OffensiveConstants;
import edu.tigers.sumatra.ai.metis.offense.statistics.OffensiveStatisticsFrame;
import edu.tigers.sumatra.ids.BotID;


/**
 * sets offensive stats if enabled
 */
public class StatisticsFeature extends AOffensiveStrategyFeature
{
	@Override
	public void doCalc(final TacticalField newTacticalField, final OffensiveStrategy strategy)
	{
		if (!OffensiveConstants.isEnableOffensiveStatistics())
		{
			return;
		}
		
		OffensiveStatisticsFrame sFrame = newTacticalField.getOffensiveStatistics();
		sFrame.setDesiredNumBots(strategy.getDesiredBots().size());
		sFrame.setPrimaryOffensiveBot(strategy.getAttackerBot().orElse(BotID.noBot()));
		
		for (BotID key : getWFrame().getTigerBotsAvailable().keySet())
		{
			EOffensiveStrategy eStrat = strategy.getCurrentOffensivePlayConfiguration().get(key);
			sFrame.getBotFrames().get(key).setActiveStrategy(eStrat);
		}
	}
}
