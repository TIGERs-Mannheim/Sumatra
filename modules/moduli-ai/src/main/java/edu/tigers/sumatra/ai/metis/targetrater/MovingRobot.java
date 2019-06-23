/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.targetrater;

import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.math.vector.VectorMath;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * A moving robot with a moving horizon
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
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
	
	
	/**
	 * This is the old implementation with a 2d calculation approach which is a little bit slower.<br>
	 * Keep this for reference and comparision with the new method for now
	 * 
	 * @param t the time horizon
	 * @return
	 */
	public ICircle getMovingHorizonLegacy2d(final double t)
	{
		double tHorizon = Math.min(maxHorizon, t);
		double v1 = acc * tHorizon;
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
		IVector2 vFront = dir.multiplyNew(Math.min(vMax, v0 + v1));
		IVector2 vBack = dir.multiplyNew(Math.max(-vMax, v0 - v1));
		IVector2 pFront = vFront.multiplyNew(tHorizon * 1000);
		IVector2 pBack = vBack.multiplyNew(tHorizon * 1000);
		double circleRadius = VectorMath.distancePP(pBack, pFront) / 2;
		IVector2 center = pos.addNew(pBack.addNew(dir.multiplyNew(circleRadius)));
		return Circle.createCircle(center, circleRadius + radius);
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
