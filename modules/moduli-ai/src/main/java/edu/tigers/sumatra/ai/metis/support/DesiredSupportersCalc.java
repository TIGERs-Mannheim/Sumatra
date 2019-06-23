/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.support;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
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
		
		Set<BotID> realSupporters = new HashSet<>();
		
		realSupporters.addAll(desiredSupporters.stream()
				.limit(tacticalField.getPlayNumbers().getOrDefault(EPlay.SUPPORT, 0)).collect(toSet()));
		
		tacticalField.addDesiredBots(EPlay.SUPPORT, realSupporters);
	}
}