/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.obstacles;

import java.awt.Color;
import java.awt.Graphics2D;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.IDrawableTool;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.math.vector.VectorMath;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent
public class MovingRobotObstacle implements IObstacle
{
	private final IVector2 pos;
	private final IVector2 vel;
	private final double vmax;
	private final double acc;
	private final double brk;
	private final double maxtHorz;
	private final double radius;
	
	private Color color = Color.red;
	
	
	@SuppressWarnings("unused")
	private MovingRobotObstacle()
	{
		pos = null;
		vel = null;
		vmax = 0;
		acc = 0;
		brk = 0;
		radius = 0;
		maxtHorz = 0;
	}
	
	
	/**
	 * @param tBot
	 * @param tHorz
	 * @param radius
	 */
	public MovingRobotObstacle(final ITrackedBot tBot, final double tHorz, final double radius)
	{
		pos = tBot.getPos();
		vel = tBot.getVel();
		vmax = tBot.getRobotInfo().getBotParams().getMovementLimits().getVelMax();
		acc = tBot.getRobotInfo().getBotParams().getMovementLimits().getAccMax();
		brk = acc;
		maxtHorz = tHorz;
		this.radius = radius;
	}
	
	
	private ICircle getCircle(final double t, final double margin)
	{
		double tHorz = Math.min(maxtHorz, t);
		double v1 = acc * tHorz;
		double v2 = brk * tHorz;
		IVector2 dir;
		double v0;
		if (vel.isZeroVector())
		{
			dir = Vector2f.X_AXIS;
			v0 = 0;
		} else
		{
			dir = vel.normalizeNew();
			v0 = vel.getLength();
		}
		IVector2 vFront = dir.multiplyNew(Math.min(vmax, v0 + v1));
		IVector2 vBack = dir.multiplyNew(Math.max(-vmax, v0 - v2));
		IVector2 pFront = vFront.multiplyNew(tHorz * 1000);
		IVector2 pBack = vBack.multiplyNew(tHorz * 1000);
		double circleRadius = VectorMath.distancePP(pBack, pFront) / 2;
		IVector2 center = pos.addNew(pBack.addNew(dir.multiplyNew(circleRadius)));
		return Circle.createCircle(center, circleRadius + radius + margin);
	}
	
	
	@Override
	public void paintShape(final Graphics2D g, final IDrawableTool tool, final boolean invert)
	{
		new DrawableCircle(getCircle(maxtHorz, 0), color).paintShape(g, tool, invert);
	}
	
	
	@Override
	public boolean isPointCollidingWithObstacle(final IVector2 point, final double t)
	{
		return getCircle(t, 0).isPointInShape(point);
	}
	
	
	/**
	 * @param color the color to set
	 */
	@Override
	public void setColor(final Color color)
	{
		this.color = color;
	}
	
	
	@Override
	public boolean isPointCollidingWithObstacle(final IVector2 point, final double t, final double margin)
	{
		return getCircle(t, margin).isPointInShape(point);
	}
}
