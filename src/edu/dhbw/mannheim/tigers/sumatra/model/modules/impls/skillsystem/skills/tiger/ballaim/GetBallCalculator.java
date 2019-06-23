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
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.tiger.AMoveSkillV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.tiger.ballmove.EBallMoveState;


/**
 * Calculates turn speed and move vector for getting ball.
 * 
 * @author Oliver Steinbrecher
 * 
 */
public class GetBallCalculator extends ABallMoveCalculator
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final float			BALL_RADIUS				= AIConfig.getGeometry().getBallRadius();


	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param dest ,destination the bot should move to
	 */
	public GetBallCalculator()
	{
		super(AIConfig.getSkills().getRotatePIDConf(), EBallMoveState.GET_BALL);
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	

	@Override
	public IVector2 calcMoveVector(double deltaT)
	{
		Vector2 moveVec = new Vector2(ballPos);
		moveVec.subtract(getBot().pos);
		
		moveVec.scaleTo(moveVec.getLength2() - BALL_RADIUS);
		

		// ##### Velocity
		// get distance to target
		final float distanceToNextPoint = moveVec.getLength2() / 1000; // [m]
		
		// calculate the appropriate velocity
		// [m/s]
		float velocity = calcVelocity(distanceToNextPoint, 0, deltaT);
		velocity *= 1.65f;
		// apply velocity
//		moveVec.scaleTo(velocity);
		moveVec.set(AMoveSkillV2.accelerateComplex(moveVec, velocity, getBot()));
		
		// ##### Convert to local bot-system
		moveVec.turn(AIMath.PI_HALF - getBot().angle);
		
		return moveVec;
	}
	
	
	@Override
	public float doCalcTargetOrientation()
	{
		return AIMath.angleBetweenXAxisAndLine(getBot().pos, ballPos);
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
