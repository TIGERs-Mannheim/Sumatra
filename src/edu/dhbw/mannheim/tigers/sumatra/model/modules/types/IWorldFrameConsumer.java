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

import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;

/**
 * {@link IWorldFrameProducer}s counterpart.
 * 
 * @author Gero
 * 
 */
public interface IWorldFrameConsumer
{
	public void onNewWorldFrame(WorldFrame worldFrame);
}
