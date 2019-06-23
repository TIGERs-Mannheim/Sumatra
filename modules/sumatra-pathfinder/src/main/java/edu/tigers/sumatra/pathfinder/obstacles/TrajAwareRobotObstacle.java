/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.obstacles;

import java.awt.*;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.drawable.IDrawableTool;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.trajectory.StubTrajectory;
import edu.tigers.sumatra.trajectory.TrajectoryWithTime;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent
public class TrajAwareRobotObstacle implements IObstacle
{
	private final TrajectoryWithTime<IVector3>	traj;
	private final double									radius;
	private final long									tCur;
	private Color											color	= Color.red;
	
	
	@SuppressWarnings("unused")
	private TrajAwareRobotObstacle()
	{
		traj = new TrajectoryWithTime<>(StubTrajectory.vector3Zero(), 0);
		tCur = 0;
		radius = 0;
	}
	
	
	/**
	 * @param traj
	 * @param tCur
	 * @param radius
	 */
	TrajAwareRobotObstacle(final TrajectoryWithTime<IVector3> traj, final long tCur, final double radius)
	{
		super();
		this.traj = traj;
		this.tCur = tCur;
		this.radius = radius;
	}
	
	
	@Override
	public boolean isPointCollidingWithObstacle(final IVector2 point, final double t)
	{
		return isPointCollidingWithObstacle(point, t, 0);
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
	public void paintShape(final Graphics2D g, final IDrawableTool tool, final boolean invert)
	{
		
		double tStart = Math.min((tCur - traj.gettStart()) / 1e9, traj.getTrajectory().getTotalTime());
		double extraMargin = ObstacleGenerator.getExtraMargin(traj.getTrajectory().getVelocity(tStart).getLength());
		final double guiRadius = tool.scaleXLength(radius + extraMargin);
		IVector2 botPos = traj.getTrajectory().getPositionMM(tStart).getXYVector();
		final IVector2 center = tool.transformToGuiCoordinates(botPos, invert);
		
		g.setColor(color);
		g.drawOval((int) (center.x() - guiRadius), (int) (center.y() - guiRadius), (int) guiRadius * 2,
				(int) guiRadius * 2);
	}
	
	
	/**
	 * @param color the color to set
	 */
	@Override
	public void setColor(final Color color)
	{
		this.color = color;
	}
}
