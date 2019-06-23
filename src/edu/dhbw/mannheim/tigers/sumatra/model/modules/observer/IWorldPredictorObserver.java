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

import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AWorldPredictor;

/**
 * Sumatra-Observer for {@link AWorldPredictor}-implementations
 * 
 * @author Gero
 */
public interface IWorldPredictorObserver
{
	public void onNewWorldFrame(WorldFrame wf);
}
