/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.targetrater;

import java.util.Optional;

import edu.tigers.sumatra.math.vector.IVector2;


/**
 * Interface for all target raters
 */
public interface ITargetRater
{
	/**
	 * Rate given point
	 * 
	 * @param origin the starting point to measure from
	 * @return the best target, if one exists
	 */
	Optional<IRatedTarget> rate(IVector2 origin);
}
