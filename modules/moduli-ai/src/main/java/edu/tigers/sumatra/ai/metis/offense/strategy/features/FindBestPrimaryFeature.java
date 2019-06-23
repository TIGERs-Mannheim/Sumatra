/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.strategy.features;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.data.OffensiveStrategy;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.math.OffensiveMath;
import edu.tigers.sumatra.ai.metis.offense.data.TemporaryOffensiveInformation;
import edu.tigers.sumatra.ai.metis.offense.strategy.AOffensiveStrategyFeature;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.IBotIDMap;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * @author MarkG
 */
public class FindBestPrimaryFeature extends AOffensiveStrategyFeature
{
	private static final double	TOGGLE_DELAY			= 0;
	
	private BotID						oldPrimary				= BotID.noBot();
	private long						newPrimarySetTimer	= 0;
	
	@Configurable
	private static boolean			avoidToggling			= false;
	
	
	/**
	 * tries to find the best offensive robot to interact with the ball
	 */
	public FindBestPrimaryFeature()
	{
		super();
	}
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame,
			TemporaryOffensiveInformation tempInfo, OffensiveStrategy strategy)
	{
		oldPrimary = findBestPrimary(newTacticalField, baseAiFrame);
		if (oldPrimary.isBot())
		{
			strategy.setMinNumberOfBots(1);
			strategy.setMaxNumberOfBots(1);
			strategy.getDesiredBots().add(oldPrimary);
			tempInfo.setPrimaryBot(baseAiFrame.getWorldFrame().getBot(oldPrimary));
		} else
		{
			strategy.setMinNumberOfBots(0);
			strategy.setMaxNumberOfBots(0);
		}
	}
	
	
	private BotID findBestPrimary(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		if (OffensiveMath.isKeeperInsane(baseAiFrame, newTacticalField))
		{
			return baseAiFrame.getKeeperId();
		}

		BotID bestGetter = OffensiveMath.getBestGetter(baseAiFrame, newTacticalField);

		if (avoidToggling)
		{
			bestGetter = avoidToggling(newTacticalField, baseAiFrame, bestGetter);
		}
		
		return bestGetter;
	}
	
	
	private BotID avoidToggling(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame, BotID bestGetter)
	{
		IBotIDMap<ITrackedBot> potentialBots = OffensiveMath.getPotentialOffensiveBotMap(newTacticalField, baseAiFrame);
		if (potentialBots.containsKey(oldPrimary))
		{
			/*
			 * Keep old Primary robot if it wasn't active longer than its min active time.
			 */
			if (oldPrimary != bestGetter)
			{
				if (newPrimarySetTimer == 0)
				{
					newPrimarySetTimer = baseAiFrame.getWorldFrame().getTimestamp();
				}
				if ((baseAiFrame.getWorldFrame().getTimestamp() - newPrimarySetTimer) / 1e9 < TOGGLE_DELAY)
				{
					return oldPrimary;
				}
			} else
			{
				newPrimarySetTimer = 0;
			}
		}
		return bestGetter;
	}
}
