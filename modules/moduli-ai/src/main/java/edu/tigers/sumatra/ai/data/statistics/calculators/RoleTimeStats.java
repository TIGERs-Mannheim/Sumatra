/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 16, 2016
 * Author(s): Phillipp Mevenkamp <phillippmevenkamp@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.data.statistics.calculators;

import edu.tigers.sumatra.ai.data.IPlayStrategy;
import edu.tigers.sumatra.ai.data.MatchStatistics;
import edu.tigers.sumatra.ai.data.MatchStatistics.EAvailableStatistic;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.AIInfoFrame;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.data.statistics.AStats;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.BotIDMap;


/**
 * @author Phillipp Mevenkamp <phillippmevenkamp@gmail.com>
 */
public class RoleTimeStats extends AStats
{
	BotIDMap<Integer>	framesAsOffensive;
	BotIDMap<Integer>	framesAsDefender;
	BotIDMap<Integer>	framesAsSupport;
	
	
	/**
	 * 
	 */
	public RoleTimeStats()
	{
		framesAsOffensive = new BotIDMap<Integer>();
		framesAsDefender = new BotIDMap<Integer>();
		framesAsSupport = new BotIDMap<Integer>();
	}
	
	
	@Override
	public void saveStatsToMatchStatistics(final MatchStatistics matchStatistics)
	{
		matchStatistics.setCountFramesAsDefender(framesAsDefender);
		matchStatistics.setCountFramesAsOffensive(framesAsOffensive);
		matchStatistics.setCountFramesAsSupporter(framesAsSupport);
		
		StatisticData defender = new StatisticData(framesAsDefender.getContentMap(), 0);
		matchStatistics.putStatisticData(EAvailableStatistic.FramesAsDefender, defender);
		
		StatisticData offensive = new StatisticData(framesAsOffensive.getContentMap(), 0);
		matchStatistics.putStatisticData(EAvailableStatistic.FramesAsOffensive, offensive);
		
		StatisticData support = new StatisticData(framesAsSupport.getContentMap(), 0);
		matchStatistics.putStatisticData(EAvailableStatistic.FramesAsSupport, support);
	}
	
	
	// TODO: Check for the roles to be the roles during an active play
	@Override
	public void onStatisticUpdate(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		AIInfoFrame previousFrame = baseAiFrame.getPrevFrame();
		
		IPlayStrategy previousPlayStrategy = previousFrame.getPlayStrategy();
		
		BotIDMap<ARole> previousBotRoles = previousPlayStrategy.getActiveRoles();
		
		for (BotID bot : previousBotRoles.keySet())
		{
			ERole botRole = previousBotRoles.get(bot).getType();
			
			switch (botRole)
			{
				case DEFENDER:
					increaseDefenseTimeForBot(bot);
					break;
				case OFFENSIVE:
					increaseOffensiveTimeForBot(bot);
					break;
				case SUPPORT:
					increaseSupportTimeForBot(bot);
					break;
				default:
			}
		}
	}
	
	
	private void increaseMapEntryByOne(final BotIDMap<Integer> mapContainingEntry, final BotID key)
	{
		if (mapContainingEntry.containsKey(key))
		{
			mapContainingEntry.put(key, mapContainingEntry.get(key) + 1);
		} else
		{
			mapContainingEntry.put(key, 0);
		}
	}
	
	
	private void increaseSupportTimeForBot(final BotID idToIncrease)
	{
		increaseMapEntryByOne(framesAsSupport, idToIncrease);
	}
	
	
	private void increaseDefenseTimeForBot(final BotID idToIncrease)
	{
		increaseMapEntryByOne(framesAsDefender, idToIncrease);
	}
	
	
	private void increaseOffensiveTimeForBot(final BotID idToIncrease)
	{
		increaseMapEntryByOne(framesAsOffensive, idToIncrease);
	}
}
