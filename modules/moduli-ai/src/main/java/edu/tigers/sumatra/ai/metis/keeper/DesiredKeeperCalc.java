/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.keeper;

import java.util.Collections;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.ai.metis.general.ADesiredBotCalc;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;


/**
 * Set desired keeper id, if keeper id is present
 */
public class DesiredKeeperCalc extends ADesiredBotCalc
{
	public DesiredKeeperCalc()
	{
		super(EPlay.KEEPER);
	}
	
	
	@Override
	public void doCalc(final TacticalField tacticalField, final BaseAiFrame aiFrame)
	{
		if (tacticalField.getPlayNumbers().getOrDefault(EPlay.KEEPER, 0) > 0)
		{
			addDesiredBots(Collections.singleton(getAiFrame().getKeeperId()));
		}
	}
}
