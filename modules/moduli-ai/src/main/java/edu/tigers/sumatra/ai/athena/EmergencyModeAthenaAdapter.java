/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.athena;

import edu.tigers.sumatra.ai.athena.PlayStrategy.Builder;
import edu.tigers.sumatra.ai.metis.MetisAiFrame;


/**
 * Emergency mode
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class EmergencyModeAthenaAdapter extends AAthenaAdapter
{
	
	@Override
	public void doProcess(final MetisAiFrame metisAiFrame, final Builder playStrategyBuilder, final AIControl aiControl)
	{
		clear(playStrategyBuilder);
	}
}
