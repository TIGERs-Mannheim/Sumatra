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

import edu.tigers.sumatra.drawable.DrawableEllipse;
import edu.tigers.sumatra.drawable.IDrawableTool;
import edu.tigers.sumatra.math.AVector2;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.shapes.ellipse.Ellipse;
import edu.tigers.sumatra.shapes.ellipse.IEllipse;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent
public class MovingRobotEllObstacle implements IObstacle
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
		ConfigRegistration.registerClass("sisyphus", MovingRobotEllObstacle.class);
	}
	
	
	@SuppressWarnings("unused")
	private MovingRobotEllObstacle()
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
	public MovingRobotEllObstacle(final ITrackedBot tBot, final double tHorz, final double radius)
	{
		pos = tBot.getPos();
		vel = tBot.getVel();
		vmax = tBot.getBot().getDefaultVelocity();
		acc = 3;
		brk = 6;
		maxtHorz = tHorz;
		this.radius = radius;
	}
	
	
	private IEllipse getCircle(final double t, final double margin)
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
		
		double relFront = Math.min(1, vel.getLength2() / 2);
		double baseRadius = Geometry.getBotRadius() + margin;
		radius += (this.radius - Geometry.getBotRadius());
		double xradius = baseRadius + radius;
		double yradius = baseRadius + ((1 - relFront) * radius);
		Ellipse ell = new Ellipse(center, xradius, yradius, dir.getAngle());
		return ell;
	}
	
	
	@Override
	public void paintShape(final Graphics2D g, final IDrawableTool tool, final boolean invert)
	{
		new DrawableEllipse(getCircle(maxtHorz, 0), color).paintShape(g, tool, invert);
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
		IEllipse circle = getCircle(maxtHorz, 0);
		if (!vel.isZeroVector())
		{
			IVector2 p1 = circle.getCenter().subtractNew(vel.scaleToNew(circle.getRadiusX() + 100));
			subPoints.add(p1);
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
	
	
	@Override
	public boolean isPointCollidingWithObstacle(final IVector2 point, final double t, final double margin)
	{
		return getCircle(t, margin).isPointInShape(point);
	}
	
}
