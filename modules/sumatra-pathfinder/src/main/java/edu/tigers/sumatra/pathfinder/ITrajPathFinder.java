/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jul 7, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.pathfinder;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@FunctionalInterface
public interface ITrajPathFinder
{
	/**
	 * @param input all necessary inputs to find a path
	 * @return a path finder result with the found path and additional information
	 */
	IPathFinderResult calcPath(final TrajPathFinderInput input);
}
