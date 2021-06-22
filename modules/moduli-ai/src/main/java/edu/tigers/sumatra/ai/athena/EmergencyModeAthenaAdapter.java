/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.athena;

import edu.tigers.sumatra.ai.metis.MetisAiFrame;

import java.util.Collections;


/**
 * Emergency mode
 */
public class EmergencyModeAthenaAdapter implements IAthenaAdapter
{
	@Override
	public PlayStrategy process(final MetisAiFrame metisAiFrame, final AthenaGuiInput athenaGuiInput)
	{
		return new PlayStrategy(Collections.emptySet());
	}
}
