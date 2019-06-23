/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.athena;

import edu.tigers.sumatra.ai.data.EAIControlState;


/**
 * Interface for all ai-module panels.
 * 
 * @author Malte
 */
public interface IAIModeChanged
{
	/**
	 * @param mode
	 */
	void onAiModeChanged(final EAIControlState mode);
}
