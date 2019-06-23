/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis;

import java.util.List;

import edu.tigers.sumatra.ai.metis.support.IPassTarget;


public interface IAiInfoFromPrevFrame
{
	/**
	 * @return the pass targets that were announced to be handled during the last frame
	 */
	List<IPassTarget> getActivePassTargets();
}
