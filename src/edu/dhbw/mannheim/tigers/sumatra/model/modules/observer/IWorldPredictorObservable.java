/* 
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 29.08.2010
 * Author(s): Gero
 *
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.observer;

import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;

/**
 * Counterpart to {@link IWorldPredictorObserver}
 * 
 * @author Gero
 * 
 */
public interface IWorldPredictorObservable
{
	public void notifyFunctionalNewWorldFrame(WorldFrame wFrame);
	public void notifyNewWorldFrame(WorldFrame wFrame);
}
