/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 25, 2016
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.pathfinder.traj;

import edu.tigers.sumatra.bot.MoveConstraints;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.pathfinder.TrajectoryGenerator;
import edu.tigers.sumatra.trajectory.ITrajectory;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class TrajPathGen
{
	private ITrajectory<IVector2> generateTrajectory(
			final MoveConstraints moveConstraints,
			final IVector2 curPos,
			final IVector2 curVel,
			final IVector2 dest)
	{
		ITrajectory<IVector2> traj = TrajectoryGenerator.generatePositionTrajectory(moveConstraints, curPos, curVel,
				dest);
		return traj;
	}
	
	
	/**
	 * @param moveConstraints
	 * @param curPos
	 * @param curVel
	 * @param dest
	 * @return
	 */
	public TrajPathV2 pathTo(final MoveConstraints moveConstraints, final IVector2 curPos, final IVector2 curVel,
			final IVector2 dest)
	{
		ITrajectory<IVector2> traj = generateTrajectory(moveConstraints, curPos, curVel, dest);
		return new TrajPathV2(traj, traj.getTotalTime(), null);
	}
	
	
	/**
	 * @param parentPath
	 * @param moveConstraints
	 * @param connectionTime
	 * @param dest
	 * @return
	 */
	public TrajPathV2 append(
			final TrajPathV2 parentPath,
			final MoveConstraints moveConstraints,
			final double connectionTime,
			final IVector2 dest)
	{
		IVector2 curPos = parentPath.getPositionMM(connectionTime);
		IVector2 curVel = parentPath.getVelocity(connectionTime);
		TrajPathV2 childPath = pathTo(moveConstraints, curPos, curVel, dest);
		return parentPath.connect(childPath, connectionTime);
	}
	
	
	/**
	 * @param path
	 * @param moveConstraints
	 * @param tCutFront
	 * @return
	 */
	public TrajPathV2 shortenPath(final TrajPathV2 path, final MoveConstraints moveConstraints, final double tCutFront)
	{
		TrajPathV2 shortenedPath = path.removeOldParts(tCutFront);
		double tCut = Math.min(tCutFront, shortenedPath.getTotalTime());
		IVector2 pos = shortenedPath.getPositionMM(tCut);
		IVector2 vel = shortenedPath.getVelocity(tCut);
		IVector2 dest = shortenedPath.getTrajectory().getPositionMM(Double.MAX_VALUE);
		ITrajectory<IVector2> traj = generateTrajectory(moveConstraints, pos, vel, dest);
		
		return new TrajPathV2(traj, shortenedPath.gettEnd() - tCut, shortenedPath.getChild());
	}
}
