/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 21, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.sisyphus.finder.traj.obstacles;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;
import java.util.Random;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.drawable.IDrawableTool;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.IVector3;
import edu.tigers.sumatra.shapes.circle.Circle;
import edu.tigers.sumatra.shapes.circle.ICircle;
import edu.tigers.sumatra.trajectory.TrajectoryWithTime;
import edu.tigers.sumatra.wp.data.Geometry;


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
	private final double									maxLookahead;
	
	
	@SuppressWarnings("unused")
	private TrajAwareRobotObstacle()
	{
		traj = null;
		tCur = 0;
		radius = 0;
		maxLookahead = 0;
	}
	
	
	/**
	 * @param traj
	 * @param tCur
	 * @param radius
	 * @param maxLookahead
	 */
	public TrajAwareRobotObstacle(final TrajectoryWithTime<IVector3> traj, final long tCur, final double radius,
			final double maxLookahead)
	{
		super();
		this.traj = traj;
		this.tCur = tCur;
		this.radius = radius;
		this.maxLookahead = maxLookahead;
	}
	
	
	@Override
	public IVector2 shiftDestination(final IVector2 dest)
	{
		IVector2 pos = traj.getTrajectory().getPositionMM(Double.MAX_VALUE).getXYVector();
		ICircle circle = new Circle(pos, Geometry.getBotRadius() * 2);
		if (circle.isPointInShape(dest))
		{
			return circle.nearestPointOutside(dest);
		}
		return dest;
	}
	
	
	@Override
	public boolean isPointCollidingWithObstacle(final IVector2 point, final double t)
	{
		return isPointCollidingWithObstacle(point, t, 0);
	}
	
	
	@Override
	public boolean isPointCollidingWithObstacle(final IVector2 point, final double t, final double margin)
	{
		if (t > maxLookahead)
		{
			return false;
		}
		double t0 = (tCur - traj.gettStart()) / 1e9;
		double t1 = t0 + Math.min(maxLookahead, t);
		IVector2 pos = traj.getTrajectory().getPositionMM(t1).getXYVector();
		
		double extraMargin = ObstacleGenerator.getExtraMargin(traj.getTrajectory().getVelocity(t).getLength());
		
		ICircle circle = new Circle(pos, radius + margin + extraMargin);
		return circle.isPointInShape(point);
	}
	
	
	@Override
	public IVector2 nearestPointOutsideObstacle(final IVector2 point, final double t)
	{
		double t0 = (tCur - traj.gettStart()) / 1e9;
		double t1 = t0 + Math.min(maxLookahead, t);
		IVector2 pos = traj.getTrajectory().getPositionMM(t1).getXYVector();
		double extraMargin = ObstacleGenerator.getExtraMargin(traj.getTrajectory().getVelocity(t).getLength());
		ICircle circle = new Circle(pos, radius + extraMargin);
		return circle.nearestPointOutside(point);
	}
	
	
	@Override
	public void generateObstacleAvoidancePoints(final IVector2 curBotPos, final Random rnd,
			final List<IVector2> subPoints)
	{
		double t = (tCur - traj.gettStart()) / 1e9;
		IVector2 pos = traj.getTrajectory().getPositionMM(t).getXYVector();
		CircleObstacle circle = new CircleObstacle(new Circle(pos, radius));
		circle.generateObstacleAvoidancePoints(curBotPos, rnd, subPoints);
	}
	
	
	@Override
	public void paintShape(final Graphics2D g, final IDrawableTool tool, final boolean invert)
	{
		
		double tStart = Math.min((tCur - traj.gettStart()) / 1e9, traj.getTrajectory().getTotalTime());
		// double tEnd = tStart + Math.min(maxLookahead, traj.getTrajectory().getTotalTime() - tStart);
		// for (double t = tStart; t <= tEnd; t += 0.1)
		double t = tStart;
		{
			double extraMargin = ObstacleGenerator.getExtraMargin(traj.getTrajectory().getVelocity(t).getLength());
			final double guiRadius = tool.scaleXLength(radius + extraMargin);
			IVector2 botPos = traj.getTrajectory().getPositionMM(t).getXYVector();
			final IVector2 center = tool.transformToGuiCoordinates(botPos, invert);
			
			g.setColor(color);
			g.setStroke(new BasicStroke(1));
			g.drawOval((int) (center.x() - guiRadius), (int) (center.y() - guiRadius), (int) guiRadius * 2,
					(int) guiRadius * 2);
			double t0 = t - tStart;
			if (t0 > 0)
			{
				g.drawString(String.format("%.1f", t0), (float) (center.x() - guiRadius), (float) (center.y() - guiRadius));
			}
		}
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
