/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.general;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.lachesis.RoleFinderInfo;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ids.BotID;


/**
 * Find number of roles and preferred bots for all roles
 *
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class RoleFinderCalc extends ACalculator
{
	@Override
	public void doCalc(final TacticalField tacticalField, final BaseAiFrame aiFrame)
	{
		Map<EPlay, RoleFinderInfo> roleFinderInfoMap = new EnumMap<>(EPlay.class);
		Map<EPlay, Set<BotID>> desiredRoles = tacticalField.getDesiredBotMap();
		
		for (Map.Entry<EPlay, Set<BotID>> entry : desiredRoles.entrySet())
		{
			RoleFinderInfo info = mapToRoleFinderInfo(entry.getKey(), entry.getValue().size(), tacticalField);
			roleFinderInfoMap.put(entry.getKey(), info);
		}
		
		tacticalField.setRoleFinderInfos(roleFinderInfoMap);
	}
	
	
	@SuppressWarnings("squid:MethodCyclomaticComplexity") // enum switch case
	private RoleFinderInfo mapToRoleFinderInfo(final EPlay play, final int desiredBots,
			final TacticalField tacticalField)
	{
		RoleFinderInfo roleFinderInfo = new RoleFinderInfo(desiredBots, desiredBots, desiredBots);
		roleFinderInfo.getDesiredBots()
				.addAll(tacticalField.getDesiredBotMap().getOrDefault(play, Collections.emptySet()));
		
		if (play == EPlay.KEEPER || play == EPlay.KEEPER_SHOOTOUT)
		{
			roleFinderInfo.setForceNumDesiredBots(desiredBots);
		}
		
		return roleFinderInfo;
	}
	
}
