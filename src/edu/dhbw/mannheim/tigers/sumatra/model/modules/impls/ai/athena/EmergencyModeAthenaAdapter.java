/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 8, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.MetisAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.PlayStrategy.Builder;


/**
 * Emergency mode
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class EmergencyModeAthenaAdapter extends AAthenaAdapter
{
	
	@Override
	public void doProcess(MetisAiFrame metisAiFrame, Builder playStrategyBuilder, AIControl aiControl)
	{
		clear(playStrategyBuilder);
	}
}
