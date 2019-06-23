/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 17, 2016
 * Author(s): Phillipp Mevenkamp <phillippmevenkamp@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.data.statistics.calculators;

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
import edu.tigers.sumatra.statistics.MarkovChain;


/**
 * @author Phillipp Mevenkamp <phillippmevenkamp@gmail.com>
 */
public class RoleChangeStats extends AStats
{
	private BotIDMap<MarkovChain<ERole>>	botRoleTransitions	= new BotIDMap<>();
	private BotIDMap<ERole>						previousRoles			= new BotIDMap<ERole>();
	
	
	@Override
	public void saveStatsToMatchStatistics(final MatchStatistics matchStatistics)
	{
		matchStatistics.setRoleTransitions(botRoleTransitions);
		
		StatisticData defensiveToOffensive = new StatisticData(botRoleTransitions.getContentMap(), ERole.DEFENDER,
				ERole.OFFENSIVE);
		matchStatistics.putStatisticData(EAvailableStatistic.DefensiveToOffensive, defensiveToOffensive);
		
		StatisticData defensiveToSupport = new StatisticData(botRoleTransitions.getContentMap(), ERole.DEFENDER,
				ERole.SUPPORT);
		matchStatistics.putStatisticData(EAvailableStatistic.DefensiveToSupport, defensiveToSupport);
		
		StatisticData offensiveToDefensive = new StatisticData(botRoleTransitions.getContentMap(), ERole.OFFENSIVE,
				ERole.DEFENDER);
		matchStatistics.putStatisticData(EAvailableStatistic.OffensiveToDefensive, offensiveToDefensive);
		
		StatisticData offensiveToSupport = new StatisticData(botRoleTransitions.getContentMap(), ERole.OFFENSIVE,
				ERole.SUPPORT);
		matchStatistics.putStatisticData(EAvailableStatistic.OffensiveToSupport, offensiveToSupport);
		
		StatisticData supportToOffensive = new StatisticData(botRoleTransitions.getContentMap(), ERole.SUPPORT,
				ERole.OFFENSIVE);
		matchStatistics.putStatisticData(EAvailableStatistic.SupportToOffensive, supportToOffensive);
		
		StatisticData supportToDefensive = new StatisticData(botRoleTransitions.getContentMap(), ERole.SUPPORT,
				ERole.DEFENDER);
		matchStatistics.putStatisticData(EAvailableStatistic.SupportToDefensive, supportToDefensive);
	}
	
	
	@Override
	public void onStatisticUpdate(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		AIInfoFrame previousAiInfoFrame = baseAiFrame.getPrevFrame();
		
		BotIDMap<ARole> activeRoles = previousAiInfoFrame.getPlayStrategy().getActiveRoles();
		
		for (BotID bot : activeRoles.keySet())
		{
			if (previousRoles.containsKey(bot))
			{
				if (!botRoleTransitions.containsKey(bot))
				{
					botRoleTransitions.put(bot, new MarkovChain<ERole>());
				}
				
				botRoleTransitions.get(bot)
						.increaseCountTransitions(previousRoles.get(bot), activeRoles.get(bot).getType());
			}
			previousRoles.put(bot, activeRoles.get(bot).getType());
		}
	}
	
}
