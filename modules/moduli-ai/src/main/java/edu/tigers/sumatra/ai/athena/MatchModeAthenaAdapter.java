/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.athena;

import java.util.Collections;

import edu.tigers.sumatra.ai.data.PlayStrategy.Builder;
import edu.tigers.sumatra.ai.data.frames.MetisAiFrame;


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
		updatePlays(metisAiFrame.getTacticalField().getRoleFinderInfos(), playStrategyBuilder.getActivePlays(),
				Collections.emptySet());
	}
}
