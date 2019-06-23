/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense;

import java.util.HashSet;
import java.util.Set;

import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ids.BotID;


/**
 * @author Sebastian Stein <sebastian-stein@gmx.de>
 */
public class DesiredShootoutAttackerCalc extends ACalculator
{
	
	
	@Override
	public void doCalc(final TacticalField tacticalField, final BaseAiFrame aiFrame)
	{
		
		Set<BotID> attackerList = new HashSet<>();
		attackerList.add(aiFrame.getKeeperId());
		
		if (tacticalField.getPlayNumbers().getOrDefault(EPlay.ATTACKER_SHOOTOUT, 0) > 0)
		{
			tacticalField.addDesiredBots(EPlay.ATTACKER_SHOOTOUT, attackerList);
		}
		
	}
}
