/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 22.07.2010
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.observer;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;


/**
 * Sumatra-Observer for {@link edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AWorldPredictor}-implementations
 * 
 * @author Gero
 */
public interface IWorldPredictorObserver
{
	/**
	 * 
	 * @param wf
	 */
	void onNewWorldFrame(WorldFrame wf);
	
	
	/**
	 * Called when there is no vision signal.
	 * @param emptyWf
	 */
	void onVisionSignalLost(WorldFrame emptyWf);
}
