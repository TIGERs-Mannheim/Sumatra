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

import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.EBallPossession;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.EGameSituation;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.criteria.global.BallPossessionCrit;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.BallGetterRole;


/**
 * This play shall be selected if no one or our opponents possess the ball and.
 * It includes one bot trying to get the ball.
 * 
 * @author FlorianS
 * 
 */
public class BallGettingPlay extends APlay
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final long	serialVersionUID	= 4948871107407700925L;

	private BallGetterRole			ballGetter;
	
	private BallPossessionCrit		ballPossessionCrit	= null;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public BallGettingPlay(AIInfoFrame aiFrame)
	{
		super(EPlay.BALL_GETTING, aiFrame);
		
		ballPossessionCrit = new BallPossessionCrit(EBallPossession.NO_ONE, EBallPossession.THEY);
		addCriterion(ballPossessionCrit);
		
		Vector2f ballPos = aiFrame.worldFrame.ball.pos;
		
		Vector2 initPosGetter = new Vector2(ballPos);
		
		Vector2 goalCenter = new Vector2(AIConfig.getGeometry().getGoalTheir().getGoalCenter());
		
		ballGetter = new BallGetterRole(goalCenter, EGameSituation.GAME);
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
		if (ballGetter.checkAllConditions(currentFrame) && currentFrame.tacticalInfo.getBallPossesion().getEBallPossession() == EBallPossession.WE)
		{
			changeToSucceeded();
		}
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
