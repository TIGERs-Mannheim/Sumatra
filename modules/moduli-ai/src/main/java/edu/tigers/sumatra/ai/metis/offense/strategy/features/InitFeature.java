/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.strategy.features;

import edu.tigers.sumatra.ai.data.OffensiveStrategy;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.offense.data.TemporaryOffensiveInformation;
import edu.tigers.sumatra.ai.metis.offense.strategy.AOffensiveStrategyFeature;
import edu.tigers.sumatra.ids.BotID;


/**
 * MarkG
 */
public class InitFeature extends AOffensiveStrategyFeature
{
	/**
	 * Default
	 */
	public InitFeature()
	{
		super();
	}
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame,
			TemporaryOffensiveInformation tempInfo, OffensiveStrategy strategy)
	{
		for (BotID key : baseAiFrame.getWorldFrame().tigerBotsAvailable.keySet())
		{
			strategy.getCurrentOffensivePlayConfiguration().put(key, OffensiveStrategy.EOffensiveStrategy.KICK);
		}
	}
}
