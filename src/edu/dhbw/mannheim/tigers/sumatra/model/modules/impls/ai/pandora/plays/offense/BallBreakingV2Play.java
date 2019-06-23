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
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.offense;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ballpossession.EBallPossession;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.criteria.global.BallPossessionCrit;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.BallBreakerRole;


/**
 * This play should try to break the ball clear.<br>
 * Dribble and arm chip-dribble
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class BallBreakingV2Play extends ABallGetterPlay
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private BallBreakerRole	ballBreaker;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param aiFrame
	 * @param numAssignedRoles
	 */
	public BallBreakingV2Play(AIInfoFrame aiFrame, int numAssignedRoles)
	{
		super(aiFrame, numAssignedRoles);
		
		addCriterion(new BallPossessionCrit(EBallPossession.THEY, EBallPossession.BOTH));
		
		if (!aiFrame.tacticalInfo.getBallPossession().getOpponentsId().isBot())
		{
			changeToFailed();
		}
		ballBreaker = new BallBreakerRole();
		addAggressiveRole(ballBreaker, aiFrame.worldFrame.ball.getPos());
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	protected void beforeFirstUpdate(AIInfoFrame aiFrame)
	{
	}
	
	
	@Override
	protected void beforeUpdate(AIInfoFrame frame)
	{
		if (frame.tacticalInfo.getBallPossession().getEBallPossession() == EBallPossession.WE)
		{
			changeToFinished();
			return;
		}
	}
	
	
	@Override
	protected void afterUpdate(AIInfoFrame currentFrame)
	{
		if (ballBreaker.isCompleted())
		{
			if (currentFrame.tacticalInfo.getBallPossession().getEBallPossession() == EBallPossession.WE)
			{
				changeToSucceeded();
			} else
			{
				changeToFailed();
			}
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
