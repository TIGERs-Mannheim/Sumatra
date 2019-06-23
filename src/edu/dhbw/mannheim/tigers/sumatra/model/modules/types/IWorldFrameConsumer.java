/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 25.07.2010
 * Author(s): Gero
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.types;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrameWrapper;


/**
 * {@link IWorldFrameProducer}s counterpart.
 * 
 * @author Gero
 */
public interface IWorldFrameConsumer
{
	/**
	 * A new worldframe is coming in. Note that there is a worldframe for each team color,
	 * so you may need to sort some out
	 * 
	 * @param wFrameWrapper
	 */
	void onNewWorldFrame(WorldFrameWrapper wFrameWrapper);
	
	
	/**
	 * This is called if the WP is stopped (Sumatra closes/stops)
	 */
	void onStop();
}
