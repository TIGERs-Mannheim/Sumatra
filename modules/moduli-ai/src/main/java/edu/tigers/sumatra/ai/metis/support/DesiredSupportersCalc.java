/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.support;

import java.util.Set;
import java.util.stream.Collectors;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.ai.metis.general.ADesiredBotCalc;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ids.BotID;


/**
 * @author Sebastian Stein <sebastian-stein@gmx.de>
 */
public class DesiredSupportersCalc extends ADesiredBotCalc
{
	
	public DesiredSupportersCalc()
	{
		super(EPlay.SUPPORT);
	}
	
	
	@Override
	public void doCalc(final TacticalField tacticalField, final BaseAiFrame aiFrame)
	{
		Set<BotID> desiredSupporters = getUnassignedBots();
		
		Set<BotID> realSupporters = desiredSupporters.stream()
				.limit(tacticalField.getPlayNumbers().getOrDefault(EPlay.SUPPORT, 0))
				.collect(Collectors.toSet());
		
		addDesiredBots(realSupporters);
	}
}
