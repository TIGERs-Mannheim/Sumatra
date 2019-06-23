/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 01.06.2011
 * Author(s): osteinbrecher
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.tiger.ballaim;

import edu.dhbw.mannheim.tigers.sumatra.model.data.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.tiger.ballmove.EBallMoveState;
import edu.dhbw.mannheim.tigers.sumatra.util.controller.PIDControllerConfig;


/**
 * Calculates turn speed and move vector for turning with ball.
 * 
 * @author Oliver Steinbrecher
 * 
 */
public class AimBallCalculator extends ABallMoveCalculator
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private float			aimVelocity	= 0.0f;
	
	/** radius for turn around ball */
	private final float	radius;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param target The target the bot should look at
	 */
	public AimBallCalculator()
	{
		super(new PIDControllerConfig(0.045f, 0, 0, 0.25f), EBallMoveState.AIM);
		
		this.radius = AIConfig.getGeometry().getBallRadius() + AIConfig.getGeometry().getBotRadius() - 20;
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public void update(WorldFrame wFrame, int botId, IVector2 target)
	{
		super.update(wFrame, botId, target);
		
		float currentAngle = getBot().angle; // aballPos.subtractNew(botPos).getAngle();
		float targetAngle = target.subtractNew(getBot().pos).getAngle();
		

		aimVelocity = pidRotateSpeed.process(currentAngle, targetAngle, 0);
//		angleDiff = Math.abs(currentAngle - targetAngle);
	}
	

	@Override
	public IVector2 calcMoveVector(double deltaT)
	{
//		if (AIMath.isZero(angleDiff, AIConfig.getTolerances().getAiming())
//				&& getBot().vel.getLength2() < AIConfig.getSkills().getMoveSpeedThreshold())
//		{
//			return Vector2.ZERO_VECTOR;
//			
//		} else
//		{
			return new Vector2(3f * aimVelocity, 0);
//		}
		
	}
	
	
	@Override
	protected float doCalcTargetOrientation()
	{
		return AIMath.angleBetweenXAxisAndLine(getBot().pos, target);
	}
	

	@Override
	public float calcTurnSpeed()
	{
//		if (AIMath.isZero(angleDiff, AIConfig.getTolerances().getAiming())
//				&& getBot().vel.getLength2() < AIConfig.getSkills().getMoveSpeedThreshold())
//		{
//			return 0;
//		} else
//		{
			return 3f * aimVelocity / (radius / 1000);
//		}
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	

}
