/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 01.06.2011
 * Author(s): osteinbrecher
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.tiger.ballmove;

import edu.dhbw.mannheim.tigers.sumatra.model.data.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;


/**
 * Calculates turn speed and move vector for getting ball.
 * 
 * @author Oliver Steinbrecher
 * 
 */
public class GetBall extends ABallMoveCalculator
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private final float				BALL_RADIUS	= AIConfig.getGeometry().getBallRadius();
	
	private final EBallMoveState	followState;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param dest ,destination the bot should move to
	 */
	public GetBall(EBallMoveState followState)
	{
		super(AIConfig.getSkills().getRotatePIDConf());
		this.followState = followState;
		this.distanceTolerance = 100;
		this.angleTolerance = AIMath.deg2rad(8);
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	protected EBallMoveState doCheckState()
	{
		return followState;
	}
	

	@Override
	public IVector2 calcMoveVector(double deltaT)
	{
		Vector2 moveVec = new Vector2(ballPos);
		moveVec.subtract(getBot().pos); // FIXME getBot kann null sein beim start
		
		moveVec.scaleTo(moveVec.getLength2() - BALL_RADIUS);
		

		// ##### Velocity
		// get distance to target
		final float distanceToNextPoint = moveVec.getLength2() / 1000; // [m]
		
		// calculate the appropriate velocity
		// [m/s]
		float velocity = calcVelocity(distanceToNextPoint, 0, deltaT);
		
		// apply velocity
		moveVec.scaleTo(velocity);
		
		// ##### Convert to local bot-system
		moveVec.turn(AIMath.PI_HALF - getBot().angle);
		
		return moveVec;
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
