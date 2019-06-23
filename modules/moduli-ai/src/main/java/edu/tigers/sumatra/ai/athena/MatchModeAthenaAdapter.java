/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.athena;

import edu.tigers.sumatra.ai.athena.PlayStrategy.Builder;
import edu.tigers.sumatra.ai.metis.MetisAiFrame;


/**
 * Match mode...
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class MatchModeAthenaAdapter extends AAthenaAdapter
{
	@Override
	public void doProcess(final MetisAiFrame metisAiFrame, final Builder playStrategyBuilder, final AIControl aiControl)
	{
		syncTargetPlaySet(metisAiFrame.getTacticalField().getRoleMapping().keySet(),
				playStrategyBuilder.getActivePlays());
		playStrategyBuilder.getRoleMapping().putAll(metisAiFrame.getTacticalField().getRoleMapping());
	}
}
