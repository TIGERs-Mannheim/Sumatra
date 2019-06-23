/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 22.07.2010
 * Author(s): Gero
 * *********************************************************
 */
package edu.tigers.sumatra.cam;

import edu.tigers.sumatra.cam.data.CamDetectionFrame;


/**
 * This interface defines the class which is capable of processing the {@link ACam}s {@link CamDetectionFrame}
 * 
 * @author Gero
 */
public interface ICamDetnFrameConsumer
{
	/**
	 * @param camDetectionFrame
	 */
	void onNewCamDetectionFrame(CamDetectionFrame camDetectionFrame);
}
