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
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.criteria.global.BallPossessionCrit;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.BallGetterRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.BallGetterRole.EBallContact;


/**
 * This play shall be selected if no one or our opponents possess the ball
 * It includes one bot trying to get the ball.
 * 
 * @author FlorianS
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class BallGettingPlay extends ABallGetterPlay
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private final BallGetterRole	ballGetter;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param aiFrame
	 * @param numAssignedRoles
	 */
	public BallGettingPlay(AIInfoFrame aiFrame, int numAssignedRoles)
	{
		super(aiFrame, numAssignedRoles);
		
		addCriterion(new BallPossessionCrit(EBallPossession.NO_ONE));
		
		final IVector2 initPosGetter = new Vector2(aiFrame.worldFrame.ball.getPos());
		
		ballGetter = new BallGetterRole(aiFrame.worldFrame.ball.getPos(), EBallContact.DISTANCE);
		addAggressiveRole(ballGetter, initPosGetter);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	protected void beforeUpdate(AIInfoFrame currentFrame)
	{
		if (currentFrame.tacticalInfo.getBallPossession().getEBallPossession() == EBallPossession.WE)
		{
			changeToSucceeded();
		}
		if ((currentFrame.tacticalInfo.getBallPossession().getEBallPossession() == EBallPossession.THEY))
		{
			changeToFailed();
		}
	}
	
	
	@Override
	protected void afterUpdate(AIInfoFrame currentFrame)
	{
		if (ballGetter.isCompleted())
		{
			changeToFinished();
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
	
	
	@Override
	protected void timedOut(AIInfoFrame currentFrame)
	{
		changeToFailed();
	}
}
