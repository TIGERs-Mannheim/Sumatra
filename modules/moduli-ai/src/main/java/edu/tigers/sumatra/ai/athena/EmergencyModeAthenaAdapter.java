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
