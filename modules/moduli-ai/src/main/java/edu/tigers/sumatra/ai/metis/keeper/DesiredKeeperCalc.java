/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.keeper;

import java.util.HashSet;
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
public class DesiredKeeperCalc extends ACalculator
{
	
	@Override
	public void doCalc(final TacticalField tacticalField, final BaseAiFrame aiFrame)
	{
		Map<EPlay, Integer> playNumbers = tacticalField.getPlayNumbers();
		
		Set<BotID> keeperList = new HashSet<>();
		keeperList.add(aiFrame.getKeeperId());
		
		EPlay play;
		if (playNumbers.getOrDefault(EPlay.KEEPER, 0) > 0)
		{
			play = EPlay.KEEPER;
		} else if (playNumbers.getOrDefault(EPlay.KEEPER_SHOOTOUT, 0) > 0)
		{
			play = EPlay.KEEPER_SHOOTOUT;
		} else
		{
			return;
		}
		
		tacticalField.addDesiredBots(play, keeperList);
	}
}
