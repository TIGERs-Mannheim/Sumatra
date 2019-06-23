/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.strategy.features;

import static edu.tigers.sumatra.ai.data.OffensiveStrategy.EOffensiveStrategy;

import edu.tigers.sumatra.ai.data.OffensiveStrategy;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.offense.OffensiveConstants;
import edu.tigers.sumatra.ai.metis.offense.data.OffensiveStatisticsFrame;
import edu.tigers.sumatra.ai.metis.offense.data.TemporaryOffensiveInformation;
import edu.tigers.sumatra.ai.metis.offense.strategy.AOffensiveStrategyFeature;
import edu.tigers.sumatra.ids.BotID;


/**
 * MarkG
 */
public class StatisticsFeature extends AOffensiveStrategyFeature
{
	/**
	 * sets offensive stats if enabled
	 */
	public StatisticsFeature()
	{
		super();
	}
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame,
			TemporaryOffensiveInformation tempInfo, OffensiveStrategy strategy)
	{
		if (!OffensiveConstants.isEnableOffensiveStatistics())
		{
			return;
		}

		OffensiveStatisticsFrame sFrame = newTacticalField.getOffensiveStatistics();
		sFrame.setDesiredNumBots(strategy.getDesiredBots().size());
		sFrame.setMaxNumBots(strategy.getMaxNumberOfBots());
		sFrame.setMinNumBots(strategy.getMinNumberOfBots());
		sFrame.setPrimaryOffensiveBot(tempInfo.getPrimaryBot().getBotId());
		
		for (BotID key : baseAiFrame.getWorldFrame().getTigerBotsAvailable().keySet())
		{
			EOffensiveStrategy eStrat = strategy.getCurrentOffensivePlayConfiguration().get(key);
			sFrame.getBotFrames().get(key).setActiveStrategy(eStrat);
		}
	}
	
}
