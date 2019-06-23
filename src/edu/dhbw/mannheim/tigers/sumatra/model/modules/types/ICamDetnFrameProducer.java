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

/**
 * Produces {@link edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamDetectionFrame}s and pushes them to a
 * {@link ICamDetnFrameConsumer}.
 * 
 * @author Gero
 * 
 */
public interface ICamDetnFrameProducer
{
	/**
	 * 
	 * @param consumer
	 */
	void setCamFrameConsumer(ICamDetnFrameConsumer consumer);
}
