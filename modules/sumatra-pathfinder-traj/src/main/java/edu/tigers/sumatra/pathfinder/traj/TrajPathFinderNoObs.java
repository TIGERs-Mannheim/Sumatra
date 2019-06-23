/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.pathfinder.traj;

import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.pathfinder.IPathFinderResult;
import edu.tigers.sumatra.pathfinder.ITrajPathFinder;
import edu.tigers.sumatra.pathfinder.TrajPathFinderInput;
import edu.tigers.sumatra.pathfinder.TrajectoryGenerator;
import edu.tigers.sumatra.trajectory.ITrajectory;


/**
 * path finder without obstacles -> simple stub that generates a trajectory to the destination
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class TrajPathFinderNoObs implements ITrajPathFinder
{
	@Override
	public IPathFinderResult calcPath(final TrajPathFinderInput input)
	{
		ITrajectory<IVector2> traj = TrajectoryGenerator.generatePositionTrajectory(
				input.getMoveConstraints(),
				input.getPos(), input.getVel(), input.getDest());
		return new PathFinderNoObsResult(traj);
	}
	
	private static class PathFinderNoObsResult implements IPathFinderResult
	{
		
		private ITrajectory<IVector2> traj;
		
		
		/**
		 * @param traj resulting trajectory
		 */
		PathFinderNoObsResult(ITrajectory<IVector2> traj)
		{
			this.traj = traj;
		}
		
		
		@Override
		public ITrajectory<IVector2> getTrajectory()
		{
			return traj;
		}
	}
}
