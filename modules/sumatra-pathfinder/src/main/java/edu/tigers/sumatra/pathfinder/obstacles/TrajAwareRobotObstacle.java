/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.obstacles;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.trajectory.StubTrajectory;
import edu.tigers.sumatra.trajectory.TrajectoryWithTime;


/**
 * An obstacle for a robot that is based on the current trajectory path of the robot
 */
@Persistent
public class TrajAwareRobotObstacle extends AObstacle
{
	private final transient TrajectoryWithTime<IVector3> traj;
	private final transient long tCur;
	private final double radius;

	private final double drawMargin;
	private final IVector2 botPos;


	@SuppressWarnings("unused")
	private TrajAwareRobotObstacle()
	{
		this(new TrajectoryWithTime<>(StubTrajectory.vector3Zero(), 0), 0, 0);
	}


	/**
	 * Create a new obstacle.
	 *
	 * @param traj the timestamp-aware trajectory of the bot
	 * @param tCur the current timestamp
	 * @param radius the minimum base-radius
	 */
	TrajAwareRobotObstacle(final TrajectoryWithTime<IVector3> traj, final long tCur, final double radius)
	{
		this.traj = traj;
		this.tCur = tCur;
		this.radius = radius;

		double tStart = Math.min((tCur - traj.gettStart()) / 1e9, traj.getTrajectory().getTotalTime());
		drawMargin = radius + ObstacleGenerator.getExtraMargin(traj.getTrajectory().getVelocity(tStart).getLength());
		botPos = traj.getTrajectory().getPositionMM(tStart).getXYVector();
	}


	@Override
	public boolean isPointCollidingWithObstacle(final IVector2 point, final double t, final double margin)
	{
		double t0 = (tCur - traj.gettStart()) / 1e9;
		double t1 = t0 + t;
		IVector2 pos = traj.getTrajectory().getPositionMM(t1).getXYVector();

		double extraMargin = ObstacleGenerator.getExtraMargin(traj.getTrajectory().getVelocity(t).getLength());

		ICircle circle = Circle.createCircle(pos, radius + margin + extraMargin);
		return circle.isPointInShape(point);
	}


	@Override
	protected void initializeShapes()
	{
		shapes.add(new DrawableCircle(Circle.createCircle(botPos, drawMargin)));
	}
}
