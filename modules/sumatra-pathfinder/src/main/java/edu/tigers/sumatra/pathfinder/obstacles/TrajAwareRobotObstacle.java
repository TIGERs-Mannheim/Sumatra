/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.obstacles;

import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.trajectory.TrajectoryWithTime;

import java.util.List;


/**
 * An obstacle for a robot that is based on the current trajectory path of the robot
 */
public class TrajAwareRobotObstacle extends AObstacle
{
	private final TrajectoryWithTime<IVector3> traj;
	private final double radius;
	private final double tStart;


	/**
	 * Create a new obstacle.
	 *
	 * @param traj   the timestamp-aware trajectory of the bot
	 * @param tCur   the current timestamp
	 * @param radius the minimum base-radius
	 */
	TrajAwareRobotObstacle(final TrajectoryWithTime<IVector3> traj, final long tCur, final double radius)
	{
		this.traj = traj;
		this.radius = radius;
		this.tStart = Math.min((tCur - traj.gettStart()) / 1e9, traj.getTrajectory().getTotalTime());
	}


	@Override
	public boolean isPointCollidingWithObstacle(final IVector2 point, final double t, final double margin)
	{
		var tt = tStart + t;
		var pos = traj.getTrajectory().getPositionMM(tt).getXYVector();
		var vel = traj.getTrajectory().getVelocity(tt);
		var extraMargin = ObstacleGenerator.getExtraMargin(vel.getLength());
		return pos.distanceToSqr(point) <= SumatraMath.square(radius + margin + extraMargin);
	}


	@Override
	protected void initializeShapes(final List<IDrawableShape> shapes)
	{
		var drawMargin = radius + ObstacleGenerator.getExtraMargin(traj.getTrajectory().getVelocity(tStart).getLength());
		var botPos = traj.getTrajectory().getPositionMM(tStart).getXYVector();
		shapes.add(new DrawableCircle(Circle.createCircle(botPos, drawMargin)));
	}
}
