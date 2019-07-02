/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.loganalysis.microtypes;

import edu.tigers.sumatra.loganalysis.eventtypes.TypeDetectionFrame;


/**
 * This class detects pattern to support the detection of event types
 */
public interface IMicroTypeDetection
{
	/**
	 * This abstract methods provides the next frame for the detection of the event type
	 * 
	 * @param frame the given frame for detection
	 */
	void nextFrameForDetection(final TypeDetectionFrame frame);
}
