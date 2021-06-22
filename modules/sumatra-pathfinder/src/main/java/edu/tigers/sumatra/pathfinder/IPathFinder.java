/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.pathfinder;


import java.util.concurrent.Future;


/**
 * Interface for all path finders
 */
@FunctionalInterface
public interface IPathFinder
{
	/**
	 * @param input all necessary inputs to find a path
	 * @return a path finder result with the found path and additional information
	 */
	Future<IPathFinderResult> calcPath(final PathFinderInput input);
}
