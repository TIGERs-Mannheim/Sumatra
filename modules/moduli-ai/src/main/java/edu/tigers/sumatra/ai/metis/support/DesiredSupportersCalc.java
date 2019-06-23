/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.support;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ids.BotID;


/**
 * @author Sebastian Stein <sebastian-stein@gmx.de>
 */
public class DesiredSupportersCalc extends ACalculator
{
	
	@Override
	public void doCalc(final TacticalField tacticalField, final BaseAiFrame aiFrame)
	{
		Set<BotID> botsAvailable = aiFrame.getWorldFrame().getTigerBotsAvailable().keySet();
		
		Map<EPlay, Set<BotID>> desiredRoles = new EnumMap<>(EPlay.class);
		desiredRoles.putAll(tacticalField.getDesiredBotMap());
		
		List<BotID> assignedBots = desiredRoles.values().stream().flatMap(Collection::stream).collect(toList());
		
		List<BotID> desiredSupporters = new ArrayList<>(botsAvailable);
		desiredSupporters.removeAll(assignedBots);
		
		Set<BotID> realSupporters = desiredSupporters.stream()
				.limit(tacticalField.getPlayNumbers().getOrDefault(EPlay.SUPPORT, 0))
				.distinct()
				.collect(Collectors.toSet());
		
		tacticalField.addDesiredBots(EPlay.SUPPORT, realSupporters);
	}
}
