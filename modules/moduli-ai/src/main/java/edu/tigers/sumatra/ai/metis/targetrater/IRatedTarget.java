/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.targetrater;

import edu.tigers.sumatra.wp.data.DynamicPosition;


/**
 * A target for passes and goal kicks with a rated score and an optional angle range
 */
public interface IRatedTarget
{
	/**
	 * @return the target point
	 */
	DynamicPosition getTarget();
	
	
	/**
	 * @return the range within this target is valid
	 */
	double getRange();
	
	
	/**
	 * @return the score between 0 and 1 where 1 is best
	 */
	double getScore();
}
