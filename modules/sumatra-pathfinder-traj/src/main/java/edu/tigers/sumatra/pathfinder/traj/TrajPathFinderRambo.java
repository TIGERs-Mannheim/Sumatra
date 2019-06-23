/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.traj;

import edu.tigers.sumatra.pathfinder.TrajPathFinderInput;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class TrajPathFinderRambo extends ATrajPathFinder
{
	@Override
	protected PathCollision generatePath(final TrajPathFinderInput input)
	{
		return getPath(input, input.getDest());
	}
}
