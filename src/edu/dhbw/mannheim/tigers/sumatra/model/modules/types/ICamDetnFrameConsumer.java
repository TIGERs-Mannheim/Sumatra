/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 22.07.2010
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.types;

import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamDetectionFrame;


/**
 * This interface defines the class which is capable of processing the {@link ACam}s {@link CamDetectionFrame}
 * 
 * @author Gero
 * 
 */
public interface ICamDetnFrameConsumer
{
	/**
	 * 
	 * @param camDetectionFrame
	 */
	void onNewCamDetectionFrame(CamDetectionFrame camDetectionFrame);
}
