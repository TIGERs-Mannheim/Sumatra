/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 24.10.2010
 * Author(s):DanielW, GuntherB
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.aiming;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.ACondition;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.ECondition;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EBotType;


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
	
	private Vector2	aimingTarget	= new Vector2(0, 0);
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public AimingCon()
	{
		super(ECondition.AIMING);
		
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	protected EConditionState doCheckCondition(WorldFrame worldFrame, BotID botID)
	{
		final float aimTolerance = getBotConfig().getTolerances().getAiming();
		final float nearTolerance = getBotConfig().getTolerances().getNearBall();
		
		final TrackedTigerBot bot = worldFrame.tigerBotsVisible.get(botID);
		final IVector2 ballPos = worldFrame.ball.getPos();
		
		// check for correct view angle
		final float targetViewAngle = aimingTarget.subtractNew(bot.getPos()).getAngle();
		final float botOrientation = bot.getAngle();
		
		final float targetBotAngleDiff = Math.abs(targetViewAngle - botOrientation);
		
		final boolean looksAtBall = true;
		
		// check for correct position "behind" ball
		// angle(ball-target) =~ angle(bot-ball)
		final boolean isBehindBall = aimTolerance > targetBotAngleDiff;
		
		// do not move too fast
		final boolean isNotTooFast = bot.getVel().getLength2() < AIConfig.getSkills(EBotType.TIGER)
				.getMoveSpeedThreshold();
		
		// right distance to ball
		final boolean isNearBall = GeoMath.distancePP(bot.getPos(), ballPos) < (nearTolerance
				+ AIConfig.getGeometry().getBallRadius() + AIConfig.getGeometry().getBotRadius());
		
		setCondition("looksAtBall:" + looksAtBall + "\nisBehindBall:" + isBehindBall
				+ "\n(aimTolerance >\ntargetBotAngleDiff:\n" + aimTolerance + " >\n" + targetBotAngleDiff
				+ ")\nisNotTooFast:" + isNotTooFast + "\nisNearBall:" + isNearBall);
		if (looksAtBall && isBehindBall && isNotTooFast && isNearBall)
		{
			return EConditionState.FULFILLED;
		}
		return EConditionState.PENDING;
	}
	
	
	/**
	 * updates the aiming target. note: the target view angle is updated in doCheckCondition
	 * @param newTarget
	 */
	public void updateAimingTarget(IVector2 newTarget)
	{
		if (!aimingTarget.equals(newTarget))
		{
			aimingTarget = new Vector2(newTarget);
			resetCache();
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @return the aimingTarget
	 */
	public Vector2 getAimingTarget()
	{
		return aimingTarget;
	}
	
	
	@Override
	protected boolean compareContent(ACondition condition)
	{
		final AimingCon con = (AimingCon) condition;
		if (aimingTarget.equals(con.getAimingTarget()))
		{
			return true;
		}
		return false;
	}
}