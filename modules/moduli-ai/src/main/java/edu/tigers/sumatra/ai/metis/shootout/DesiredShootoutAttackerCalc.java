/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.shootout;

import java.util.HashSet;
import java.util.Set;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.ai.metis.general.ADesiredBotCalc;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ids.BotID;


/**
 * @author Sebastian Stein <sebastian-stein@gmx.de>
 */
public class DesiredShootoutAttackerCalc extends ADesiredBotCalc
{
	public DesiredShootoutAttackerCalc()
	{
		super(EPlay.ATTACKER_SHOOTOUT);
	}
	
	
	@Override
	public void doCalc(final TacticalField tacticalField, final BaseAiFrame aiFrame)
	{
		Set<BotID> attackerList = new HashSet<>();
		attackerList.add(aiFrame.getKeeperId());
		
		if (tacticalField.getPlayNumbers().getOrDefault(EPlay.ATTACKER_SHOOTOUT, 0) > 0)
		{
			addDesiredBots(attackerList);
		}
	}
}
