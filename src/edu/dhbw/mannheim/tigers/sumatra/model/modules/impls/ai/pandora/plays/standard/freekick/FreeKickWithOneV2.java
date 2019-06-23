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
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.standard.freekick;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.EBallPossession;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.criteria.global.BallPossessionCrit;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.FreeKickerV25;


/**
 * This play shall be selected if no one or our opponents possess the ball and.
 * It includes one bot trying to get the ball.
 * 
 * @author FlorianS
 * 
 */
public class FreeKickWithOneV2 extends APlay
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final long	serialVersionUID		= 4948871107407700925L;
	
	private FreeKickerV25		ballGetter;
	
	private BallPossessionCrit	ballPossessionCrit	= null;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * ATTENTION: right now, this play is a SET-PIECE-PLAY only,
	 * check back with Gero or Gunther for more information
	 */
	public FreeKickWithOneV2(AIInfoFrame aiFrame)
	{
		super(EPlay.FREEKICK_V2, aiFrame);
		
		ballPossessionCrit = new BallPossessionCrit(EBallPossession.NO_ONE, EBallPossession.THEY);
		addCriterion(ballPossessionCrit);
		
		Vector2f ballPos = aiFrame.worldFrame.ball.pos;
		
		Vector2 initPosGetter = new Vector2(ballPos);
		
		ballGetter = new FreeKickerV25();
		addAggressiveRole(ballGetter, initPosGetter);
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	protected void beforeUpdate(AIInfoFrame currentFrame)
	{
		// if (AIMath.distancePP(ballGetter.getPos(currentFrame), currentFrame.worldFrame.ball.pos) > 500)
		// {
		// changeToSucceeded(); // guess we shot the ball, so please just get it over with
		// }
	}
	

	@Override
	protected void afterUpdate(AIInfoFrame currentFrame)
	{
		if (currentFrame.tacticalInfo.getBallPossesion().getEBallPossession() == EBallPossession.THEY)
		{
			changeToFailed();
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
