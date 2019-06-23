/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 24.10.2010
 * Author(s):DanielW, GuntherB
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions;

import edu.dhbw.mannheim.tigers.sumatra.model.data.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.ACondition;


/**
 * condition
 * checks if aimingTarget, ball and bot are on a straight line and bot is behind ball
 * checks if bot is looking at the ball
 * @author DanielW, GuntherB
 */
public class AimingCon extends ACondition
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private float		targetViewAngle	= 0;
	private float		aimTolerance		= AIConfig.getTolerances().getAiming();
	private float		nearTolerance		= AIConfig.getTolerances().getNearBall();
	private Vector2	aimingTarget		= new Vector2(0, 0);
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public AimingCon()
	{
		super(ECondition.AIMING);
		
	}
	

	public AimingCon(float tolerance)
	{
		super(ECondition.AIMING);
		this.aimTolerance = tolerance;
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public boolean doCheckCondition(WorldFrame worldFrame, int botID)
	{
		TrackedTigerBot bot = worldFrame.tigerBots.get(botID);
		IVector2 ballPos = worldFrame.ball.pos;
		
		// check for correct view angle
		final float targetAngle = aimingTarget.subtractNew(bot.pos).getAngle();
		targetViewAngle = targetAngle;
		final float botOrientation = bot.angle;
		
		final float botBallAngle = ballPos.subtractNew(bot.pos).getAngle();
		
		final float targetBotAngleDiff = Math.abs(targetAngle - botOrientation);
		@SuppressWarnings("unused")
		final float ballBotAngleDiff = Math.abs(botBallAngle - botOrientation);
		
		boolean looksAtBall = true;	//aimTolerance > ballBotAngleDiff;
		
		// check for correct position "behind" ball
		// angle(ball-target) =~ angle(bot-ball)
		boolean isBehindBall = aimTolerance > targetBotAngleDiff;
		
		// do not move too fast
		boolean isNotTooFast = bot.vel.getLength2() < AIConfig.getSkills().getMoveSpeedThreshold();
		
		// right distance to ball
		boolean isNearBall = AIMath.distancePP(bot.pos, ballPos) < nearTolerance
				+ AIConfig.getGeometry().getBallRadius() + AIConfig.getGeometry().getBotRadius();
		
		// System.out.println("AimingCon-----------------");
		// System.out.println("LooksAtBall  " + looksAtBall);
		// System.out.println("IsBehindBall " + isBehindBall);
		// System.out.println("IsNotTooFast " + isNotTooFast);
		// System.out.println("IsNearBall   " + isNearBall);
		

		return looksAtBall && isBehindBall && isNotTooFast && isNearBall;
	}
	

	/**
	 * updates the aiming target. note: the target view angle is updated in doCheckCondition
	 * @param newTarget
	 */
	public void updateAimingTarget(IVector2 newTarget)
	{
		this.aimingTarget = new Vector2(newTarget);
		resetCache();
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return the targetViewAngle
	 */
	public float getTargetViewAngle()
	{
		return targetViewAngle;
	}
	

	/**
	 * @return the aimingTarget
	 */
	public Vector2 getAimingTarget()
	{
		return aimingTarget;
	}
	

	/**
	 * @return The current aiming-tolerance
	 */
	public float getAimingTolerance()
	{
		return aimTolerance;
	}
	

	public void setAimTolerance(float newTolerance)
	{
		this.aimTolerance = newTolerance;
	}
	
	
	/**
	 * Remember: Bot and ball radius exclusive!!!
	 * 
	 * @param newNearTolerance
	 */
	public void setNearTolerance(float newNearTolerance)
	{
		this.nearTolerance = newNearTolerance;
	}
}