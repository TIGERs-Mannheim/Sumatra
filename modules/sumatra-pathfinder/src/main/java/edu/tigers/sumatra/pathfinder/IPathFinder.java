/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.pathfinder;


import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.pathfinder.finder.PathFinderInput;
import edu.tigers.sumatra.pathfinder.finder.PathFinderResult;

import java.util.Optional;


/**
 * Interface for all path finders
 */
public interface IPathFinder
{
	/**
	 * @param input all necessary inputs to find a path
	 * @return a path finder result with the found path and additional information
	 */
	Optional<PathFinderResult> calcPath(PathFinderInput input);

	/**
	 * Set the shape map to draw to. Will be set to <code>null</code> if drawing is disabled.
	 *
	 * @param shapeMap
	 */
	default void setShapeMap(ShapeMap shapeMap)
	{
	}

	/**
	 * Calculate a valid direct path. If no path without a collision was found, none will be returned.
	 *
	 * @param input all necessary inputs to find a path
	 * @return a path finder result with the found path and additional information
	 */
	Optional<PathFinderResult> calcValidDirectPath(PathFinderInput input);
}
