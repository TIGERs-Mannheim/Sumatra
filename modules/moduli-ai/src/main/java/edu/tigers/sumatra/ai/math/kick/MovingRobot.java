/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.math.kick;

import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.vector.AVector2;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.VectorMath;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
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
	 * @param t the time horizon
	 * @return
	 */
	public ICircle getCircle(final double t)
	{
		double tHorizon = Math.min(maxHorizon, t);
		double v1 = acc * tHorizon;
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
		IVector2 vFront = dir.multiplyNew(Math.min(vMax, v0 + v1));
		IVector2 vBack = dir.multiplyNew(Math.max(-vMax, v0 - v1));
		IVector2 pFront = vFront.multiplyNew(tHorizon * 1000);
		IVector2 pBack = vBack.multiplyNew(tHorizon * 1000);
		double circleRadius = VectorMath.distancePP(pBack, pFront) / 2;
		IVector2 center = pos.addNew(pBack.addNew(dir.multiplyNew(circleRadius)));
		return Circle.createCircle(center, circleRadius + radius);
	}
}
