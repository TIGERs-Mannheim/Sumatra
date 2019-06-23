/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 8, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.athena;

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
		updatePlays(metisAiFrame.getTacticalField().getRoleFinderInfos(), playStrategyBuilder.getActivePlays());
	}
}
