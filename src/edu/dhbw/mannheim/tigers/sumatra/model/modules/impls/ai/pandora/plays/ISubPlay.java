/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 10.11.2010
 * Author(s): GuntherB
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;


/**
 * This SubPlay Interface may be implemented by SubPlays in APlays that follow the standards of a statemachine,
 * to handle before and afterUpdate of these specific subPlays
 * 
 * @author GuntherB
 * 
 */
public interface ISubPlay
{
	/**
	 * 
	 * @param currentFrame
	 */
	void beforeUpdate(AIInfoFrame currentFrame);
	
	
	/**
	 * 
	 * @param currentFrame
	 */
	void afterUpdate(AIInfoFrame currentFrame);
	
}
