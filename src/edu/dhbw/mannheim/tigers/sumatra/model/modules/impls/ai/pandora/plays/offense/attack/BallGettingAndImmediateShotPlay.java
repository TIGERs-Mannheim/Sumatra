/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 27.06.2011
 * Author(s):
 * FlorianS
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.offense.attack;

import edu.dhbw.mannheim.tigers.sumatra.model.data.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.EBallPossession;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.criteria.global.BallPossessionCrit;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.ArmedBallGetterRole;


/**
 * This play shall be selected if no one or our opponents possess the ball and.
 * It includes one bot trying to get the ball.
 * 
 * @author FlorianS
 * 
 */
public class BallGettingAndImmediateShotPlay extends APlay
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final long		serialVersionUID		= 4948871107407700925L;
	
	private ArmedBallGetterRole	ballGetter;
	
	private BallPossessionCrit		ballPossessionCrit	= null;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * ATTENTION: right now, this play is a SET-PIECE-PLAY only,
	 * check back with Gero or Gunther for more information
	 */
	public BallGettingAndImmediateShotPlay(AIInfoFrame aiFrame)
	{
		super(EPlay.BALL_GETTING_AND_IMMEDIATE_SHOT, aiFrame);
		
		ballPossessionCrit = new BallPossessionCrit(EBallPossession.NO_ONE, EBallPossession.THEY);
		addCriterion(ballPossessionCrit);
		
		Vector2f ballPos = aiFrame.worldFrame.ball.pos;
		
		IVector2 initPosGetter = ballPos;
		
		ballGetter = new ArmedBallGetterRole();
		addAggressiveRole(ballGetter, initPosGetter);
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	protected void beforeUpdate(AIInfoFrame currentFrame)
	{
		super.beforeUpdate(currentFrame);
	}
	

	@Override
	protected void afterUpdate(AIInfoFrame currentFrame)
	{
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public boolean isBallCarrying()
	{
		return true;
	}
}
