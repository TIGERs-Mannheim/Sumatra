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

import edu.dhbw.mannheim.tigers.sumatra.model.data.CamDetectionFrame;

/**
 * Produces {@link CamDetectionFrame}s and pushes them to a {@link ICamFrameConsumer}.
 * 
 * @author Gero
 * 
 */
public interface ICamDetnFrameProducer
{
	public void setCamFrameConsumer(ICamDetnFrameConsumer consumer);
}
