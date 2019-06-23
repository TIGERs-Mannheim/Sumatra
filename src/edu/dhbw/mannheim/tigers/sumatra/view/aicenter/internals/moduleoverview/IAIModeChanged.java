/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 14.04.2011
 * Author(s): Malte
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.aicenter.internals.moduleoverview;

import edu.dhbw.mannheim.tigers.sumatra.presenter.aicenter.EAIControlState;


/**
 * Interface for all ai-module panels.
 * 
 * @author Malte
 * 
 */
public interface IAIModeChanged
{
	
	/**
	 * @param mode
	 */
	void onAiModeChanged(EAIControlState mode);
}
