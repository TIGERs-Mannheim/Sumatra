/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis;

import edu.tigers.sumatra.ai.metis.support.passtarget.IPassTarget;


public interface IAiInfoForNextFrame
{
	/**
	 * Announce that the given pass target is currently handled by some receiver.
	 * This information is only valid for the next frame, so this method has to be called in each frame.
	 * If the passTarget is null, it will be ignored
	 *
	 * @param passTarget the passTarget that is currently handled
	 */
	void announcePassingTo(final IPassTarget passTarget);
}
