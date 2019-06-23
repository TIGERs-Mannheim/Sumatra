/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 25.07.2010
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.types;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;


/**
 * {@link IWorldFrameProducer}s counterpart.
 * 
 * @author Gero
 * 
 */
public interface IWorldFrameConsumer
{
	/**
	 * 
	 * @param worldFrame
	 */
	void onNewWorldFrame(WorldFrame worldFrame);
	
	
	/**
	 * 
	 */
	void onStop();
	
	
	/**
	 * Called when there is no vision signal.
	 * 
	 * @param emptyWf
	 */
	void onVisionSignalLost(WorldFrame emptyWf);
}
