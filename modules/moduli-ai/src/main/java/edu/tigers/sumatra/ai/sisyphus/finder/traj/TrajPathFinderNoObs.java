/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 26, 2016
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.sisyphus.finder.traj;

import java.util.Optional;

import edu.tigers.sumatra.ai.sisyphus.TrajectoryGenerator;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.trajectory.ITrajectory;
import edu.tigers.sumatra.trajectory.TrajectoryWithTime;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class TrajPathFinderNoObs implements ITrajPathFinder
{
	private final TrajectoryGenerator gen = new TrajectoryGenerator();
	
	
	@Override
	public Optional<TrajectoryWithTime<IVector2>> calcPath(final TrajPathFinderInput input)
	{
		ITrajectory<IVector2> traj = gen.generatePositionTrajectory(input.getMoveCon().getMoveConstraints(),
				input.getPos(), input.getVel(), input.getDest());
		TrajectoryWithTime<IVector2> path = new TrajectoryWithTime<>(traj, input.getTimestamp());
		return Optional.of(path);
	}
	
}
