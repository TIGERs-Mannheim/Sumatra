/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.cam;

/**
 * Get triggered for each frame that was processed by the {@link LogfileAnalyzerVisionCam}.
 */
@FunctionalInterface
public interface LogfileAnalyzerConsumer
{
	/**
	 * Do a processing step.
	 *
	 * @param frameId the current frame id in the log file
	 */
	void process(long frameId);
}
