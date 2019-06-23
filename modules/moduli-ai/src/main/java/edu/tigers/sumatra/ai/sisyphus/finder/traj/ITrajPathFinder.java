/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jul 7, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.sisyphus.finder.traj;

import java.util.Optional;

import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.trajectory.TrajectoryWithTime;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface ITrajPathFinder
{
	/**
	 * @param input
	 * @return
	 */
	Optional<TrajectoryWithTime<IVector2>> calcPath(final TrajPathFinderInput input);
}
