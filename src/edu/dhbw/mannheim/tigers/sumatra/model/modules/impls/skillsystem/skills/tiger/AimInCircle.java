/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 09.04.2011
 * Author(s): OliverS
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.tiger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ASkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;
import edu.dhbw.mannheim.tigers.sumatra.util.controller.PIDControllerConfig;


/**
 * this Skill makes
 * the TigerBot move around the ball, looking at it all the time and ending the skill
 * facing the target coordinates with the ball in front of it.
 * 
 * 
 * @author DanielW, OliverS, GuntherB
 */
public class AimInCircle extends AMoveSkill
{
	

	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private final IVector2		targetPosition;
	private final float			radius;
	private final float			aimTolerance;
	private final float			ANGLE_STEP							= AIMath.deg2rad(10);
	
	private final float			MAX_VELOCITY						= AIConfig.getSkills().getMaxVelocity();						// 3000;
																																							// //
																																							// m/s
	private final float			MAX_BREAKING_DIST					= AIConfig.getSkills().getMaxBreakingDistance();			// 400;
																																							
	private final float			MINIMUM_DISTANCE					= AIConfig.getGeometry().getBotRadius()
																						+ AIConfig.getGeometry().getBallRadius() + 30;
	private final float			MINIMUM_DISTANCE_SQR				= MINIMUM_DISTANCE * MINIMUM_DISTANCE;
	
	private final float			MAXIMUM_MOVE_OUT_DISTANCE		= AIConfig.getGeometry().getBotRadius()
																						+ AIConfig.getGeometry().getBallRadius() + 50;
	private final float			MAXIMUM_MOVE_OUT_DISTANCE_SQR	= MAXIMUM_MOVE_OUT_DISTANCE * MAXIMUM_MOVE_OUT_DISTANCE;
	
	private static final float	TURN_RADIUS							= 40;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param target
	 * @param
	 * @param objectId
	 */
	public AimInCircle(IVector2 target, float aimTolerance)
	{
		super(ESkillName.AIM, new PIDControllerConfig(1.7f, 0.01f, 0, 10));
		
		this.targetPosition = target;
		this.aimTolerance = aimTolerance;
		
		this.radius = AIConfig.getGeometry().getBallRadius() + AIConfig.getGeometry().getBotRadius() + TURN_RADIUS;
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	protected IVector2 calcMovementStraight(WorldFrame wFrame, double deltaT)
	{
		final Vector2f centerBall = wFrame.ball.pos;
		
		// b indicates current bot position,
		// c indicates center
		// cb center->bot
		// next indicates next step in execution
		
		Vector2f b = getBot().pos;
		Vector2 cb = b.subtractNew(centerBall);
		cb.scaleTo(radius);
		
		// current position on circumference
		float currentAngle = cb.getAngle();
		// relative part of circle to go: positive means counter-clockwise
		float arc = AIMath.normalizeAngle(AIMath.angleBetweenXAxisAndLine(targetPosition, centerBall) - currentAngle);
		float arclen = radius * arc;
		
		Vector2 cb_next;
		if (arc > ANGLE_STEP)
			cb_next = cb.turnNew(ANGLE_STEP * AIMath.sign(arc));
		else
			cb_next = cb.turnNew(arc);
		
		// (absoluter) Stützvektor zum nächsten anzufahrenden punkt
		Vector2 b_next = centerBall.addNew(cb_next);
		
		// bb_next is the absolute vector the bot shall move now
		Vector2 bb_next = b_next.subtractNew(b);
		
		// convert bb_next to bot-relative vector
		Vector2 move = bb_next.turnNew(AIMath.PI_HALF - getBot().angle);
		
		// get current velocity
		// if (arclen > MAX_BREAKING_DIST)
		// velocity = MAX_VELOCITY;
		// else
		// velocity = Math.abs(arclen) * SLOPE;
		
		arclen /= 1000; // convert to m
		float velocity;
		if (arclen <= MAX_BREAKING_DIST)
		{
			velocity = MAX_VELOCITY * Math.abs(arclen) / MAX_BREAKING_DIST;
		} else
		{
			velocity = MAX_VELOCITY;
		}
		
		// apply velocity
		move.scaleTo(velocity);
		
		if (aimTolerance < Math.abs(arc))
		{
			// oh shit, we're to close!
			if (AIMath.distancePPSqr(wFrame.ball.pos, getBot().pos) < MAXIMUM_MOVE_OUT_DISTANCE_SQR)
			{
				return new Vector2(0, -0.25f);
			}
			return move;
		} else
		{
			// position on circle arc is alright, lets move closer to the ball
			if (AIMath.distancePPSqr(wFrame.ball.pos, getBot().pos) > MINIMUM_DISTANCE_SQR)
			{
				return new Vector2(0, 0.175f);
			}
			
			complete();
			
			return new Vector2(0, 0);
		}
	}
	

	@Override
	protected IVector2 calcMovementSpline(WorldFrame wFrame, double deltaT)
	{
		return calcMovementStraight(wFrame, deltaT);
	}
	

	@Override
	protected float calcTargetOrientation(IVector2 move)
	{
		return AIMath.angleBetweenXAxisAndLine(getBot().pos, targetPosition);
	}
	

	@Override
	protected float calcTurnspeed(TrackedTigerBot bot, float targetOrientation)
	{
		return pidRotateSpeed.process(bot.angle, targetOrientation, 0);
	}
	

	@Override
	protected IVector2 getTarget()
	{
		return targetPosition;
	}
	

	@Override
	protected boolean compareContent(ASkill newSkill)
	{
		AimInCircle move = (AimInCircle) newSkill;
		return targetPosition == move.targetPosition;
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
