/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 5, 2016
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.skillsystem.skills;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import edu.tigers.sumatra.ai.data.EShapesLayer;
import edu.tigers.sumatra.botmanager.commands.botskills.BotSkillGlobalVelXyPosW;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Line;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.shapes.circle.Circle;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.wp.data.DynamicPosition;
import edu.tigers.sumatra.wp.data.ShapeMap;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class PushKickTestSkill extends ASkill
{
	private final DynamicPosition	receiver;
	private final ShapeMap			shapeMap	= new ShapeMap();
	
	
	/**
	 * @param receiver
	 */
	public PushKickTestSkill(final DynamicPosition receiver)
	{
		super(ESkill.PUSH_KICK_TEST);
		this.receiver = receiver;
	}
	
	
	private double getVel(final double dist2Dest, final double targetVel, final double maxVel, final double acc)
	{
		double velDiff = maxVel - targetVel;
		double dt = velDiff / acc;
		double breakDist = (Math.min(targetVel, maxVel) + ((Math.abs(velDiff) / 2) * dt)) * 1000;
		double distDiff = breakDist - dist2Dest;
		double vel;
		if (distDiff <= 0)
		{
			vel = maxVel;
		} else
		{
			vel = targetVel + ((dist2Dest / breakDist) * (velDiff));
		}
		return vel;
	}
	
	
	private double getTime(final double vel, final double targetVel, final double acc)
	{
		double velDiff = vel - targetVel;
		double dt = velDiff / acc;
		return dt;
	}
	
	
	private double estimateVel()
	{
		double acc = getBot().getDefaultAcceleration();
		double defVel = getBot().getDefaultVelocity();
		double vel = defVel;
		
		double t = 0;
		for (int i = 0; i < 10; i++)
		{
			IVector2 ballPos = getWorldFrame().getBall().getPosByTime(t);
			double targetVel = getWorldFrame().getBall().getVelByTime(t) + 0.0;
			IVector2 dest = GeoMath.stepAlongLine(ballPos, receiver, -getBot().getCenter2DribblerDist());
			double dist2Dest = GeoMath.distancePP(getPos(), dest);
			vel = getVel(dist2Dest, targetVel, defVel, acc);
			double time = getTime(vel, targetVel, acc);
			if (Math.abs(time - t) < 0.001)
			{
				break;
			}
			t = time;
		}
		
		return vel;
	}
	
	
	@Override
	protected void doCalcActionsBeforeStateUpdate()
	{
		super.doCalcActionsBeforeStateUpdate();
		
		List<IDrawableShape> shapes = new ArrayList<>();
		IVector2 ballPos = getWorldFrame().getBall().getPos();
		IVector2 dest = GeoMath.stepAlongLine(ballPos, receiver, -getBot().getCenter2DribblerDist());
		
		IVector2 supportPoint = dest;
		Circle circle = new Circle(ballPos, getBot().getCenter2DribblerDist());
		if (!circle.isPointInShape(getPos(), 50))
		{
			List<IVector2> interSec = circle.tangentialIntersections(getPos());
			
			// find closer support point
			double dist = Double.MAX_VALUE;
			for (IVector2 p : interSec)
			{
				double pDist = GeoMath.distancePP(dest, p);
				if (pDist < dist)
				{
					dist = pDist;
					// set the point outside of secCircle to avoid that we get into the secCircle, which may slow down
					// movement.
					supportPoint = p;
				}
			}
		}
		
		IVector2 moveDir = supportPoint.subtractNew(getPos());
		if (moveDir.getLength() < 20)
		{
			moveDir = receiver.subtractNew(ballPos);
		}
		
		double vel = estimateVel();
		IVector2 xyVel = moveDir.scaleToNew(vel);
		double targetAngle = getWorldFrame().getBall().getPos().subtractNew(getPos()).getAngle();
		double destAngle = receiver.subtractNew(getPos()).getAngle();
		if (Math.abs(AngleMath.getShortestRotation(targetAngle, destAngle)) < 0.2)
		{
			targetAngle = destAngle;
		}
		
		
		shapes.add(new DrawableLine(new Line(getPos(), new Vector2(targetAngle).scaleTo(1000))));
		shapes.add(new DrawableLine(new Line(getPos(), xyVel.multiplyNew(1000)), Color.magenta));
		shapes.add(new DrawableCircle(circle));
		
		if (getWorldFrame().isInverted())
		{
			xyVel = xyVel.multiplyNew(-1);
			targetAngle = AngleMath.normalizeAngle(targetAngle + AngleMath.PI);
		}
		
		BotSkillGlobalVelXyPosW botSkill = new BotSkillGlobalVelXyPosW(xyVel, targetAngle);
		getMatchCtrl().setSkill(botSkill);
		
		getMatchCtrl().setDribblerSpeed(10000);
		
		shapeMap.put(EShapesLayer.KICK_SKILL, shapes);
	}
	
	
	@Override
	public ShapeMap getShapes()
	{
		return shapeMap;
	}
	
}
