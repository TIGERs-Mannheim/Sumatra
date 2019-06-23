/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 22, 2016
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.sisyphus.finder.traj.obstacles;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;
import java.util.Random;

import com.github.g3force.configurable.ConfigRegistration;
import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.IDrawableTool;
import edu.tigers.sumatra.math.AVector2;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.shapes.circle.Circle;
import edu.tigers.sumatra.shapes.circle.ICircle;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent
public class MovingRobotObstacle implements IObstacle
{
	private final IVector2	pos;
	private final IVector2	vel;
	private final double		vmax;
	private final double		acc;
	private final double		brk;
	private final double		maxtHorz;
	private final double		radius;
	
	private Color				color	= Color.red;
	
	
	static
	{
		ConfigRegistration.registerClass("sisyphus", MovingRobotObstacle.class);
	}
	
	
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
		vmax = tBot.getBot().getDefaultVelocity();
		acc = tBot.getBot().getDefaultAcceleration();
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
			dir = AVector2.X_AXIS;
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
		double radius = GeoMath.distancePP(pBack, pFront) / 2;
		IVector2 center = pos.addNew(pBack.addNew(dir.multiplyNew(radius)));
		return new Circle(center, radius + this.radius + margin);
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
	
	
	@Override
	public IVector2 nearestPointOutsideObstacle(final IVector2 point, final double t)
	{
		return getCircle(t, 30).nearestPointOutside(point);
	}
	
	
	@Override
	public void generateObstacleAvoidancePoints(final IVector2 curBotPos, final Random rnd,
			final List<IVector2> subPoints)
	{
		ICircle circle = getCircle(maxtHorz, 0);
		if (!vel.isZeroVector())
		{
			IVector2 p1 = circle.center().subtractNew(vel.scaleToNew(circle.radius() + 100));
			subPoints.add(p1);
		}
		CircleObstacle c = new CircleObstacle(circle);
		c.generateObstacleAvoidancePoints(curBotPos, rnd, subPoints);
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
