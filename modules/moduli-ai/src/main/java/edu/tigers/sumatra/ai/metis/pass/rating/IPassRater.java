/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.pass.rating;

import edu.tigers.sumatra.ai.metis.kicking.Pass;
import edu.tigers.sumatra.drawable.IDrawableShape;

import java.util.Collections;
import java.util.List;


/**
 * Rate a pass.
 */
public interface IPassRater
{
	/**
	 * Rate a pass
	 *
	 * @param pass the chipped or straight pass.
	 * @return a score in [0..1]
	 */
	double rate(Pass pass);

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
