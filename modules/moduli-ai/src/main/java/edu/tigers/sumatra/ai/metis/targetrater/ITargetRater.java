/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.targetrater;

import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.math.vector.IVector2;

import java.util.Collections;
import java.util.List;
import java.util.Optional;


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

	/**
	 * Rate given point
	 *
	 * @param origin the starting point to measure from
	 * @return the best target, if one exists
	 */
	List<IRatedTarget> rateMultiple(IVector2 origin);

	/**
	 * Generate shapes to visualize the work of the rater
	 *
	 * @return
	 */
	default List<IDrawableShape> createDebugShapes()
	{
		return Collections.emptyList();
	}
}
