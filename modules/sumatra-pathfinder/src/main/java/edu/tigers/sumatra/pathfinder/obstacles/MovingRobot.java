/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.obstacles;

import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * A moving robot with a moving horizon
 */
public class MovingRobot
{
	private final IVector2 pos;
	private final IVector2 vel;
	private final double vMax;
	private final double acc;
	private final double maxHorizon;
	private final double radius;


	/**
	 * @param tBot
	 * @param maxHorizon
	 * @param radius
	 */
	public MovingRobot(final ITrackedBot tBot, final double maxHorizon, final double radius)
	{
		pos = tBot.getPos();
		vel = tBot.getVel();
		vMax = tBot.getRobotInfo().getBotParams().getMovementLimits().getVelMax();
		acc = tBot.getRobotInfo().getBotParams().getMovementLimits().getAccMax();
		this.maxHorizon = maxHorizon;
		this.radius = radius;
	}


	public boolean isPointInRobot(IVector2 point, double t)
	{
		return getMovingHorizon(t).isPointInShape(point);
	}


	/**
	 * Get the horizon for possible movement of the robot for a given time horizon
	 *
	 * @param tHorizon the time horizon
	 * @return a circle specifying the horizon
	 */
	public ICircle getMovingHorizon(final double tHorizon)
	{
		double tLimitedHorizon = Math.max(0, Math.min(maxHorizon, tHorizon));
		double v1 = acc * tLimitedHorizon;
		double v0 = vel.getLength2();

		double vFront = Math.min(vMax, v0 + v1);
		double vBack = Math.max(-vMax, v0 - v1);
		double pFront = vFront * tLimitedHorizon * 1000;
		double pBack = vBack * tLimitedHorizon * 1000;

		double circleRadius = Math.abs(pFront - pBack) / 2 + radius;
		IVector2 center = pos;
		if (!vel.isZeroVector())
		{
			center = pos.addNew(vel.scaleToNew(pBack + circleRadius - radius));
		}
		return Circle.createCircle(center, circleRadius);
	}


	public IVector2 getPos()
	{
		return pos;
	}


	public IVector2 getVel()
	{
		return vel;
	}


	public double getRadius()
	{
		return radius;
	}
}
