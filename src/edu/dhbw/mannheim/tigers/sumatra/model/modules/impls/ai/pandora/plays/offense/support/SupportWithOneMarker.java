/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 21.06.2011
 * Author(s):
 * FlorianS
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.offense.support;

import edu.dhbw.mannheim.tigers.sumatra.model.data.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.EBallPossession;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.criteria.global.BallPossessionCrit;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.EWAI;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.defense.ManToManMarkerRole;


/**
 * This play could be selected to support our ball getter / carrier when the
 * number of bots in our defense play and our offense play does not sum up five.
 * Only one pass blocker will get between the ball and the opponent bot which is
 * second closest to ball
 * 
 * @author FlorianS
 * 
 */
public class SupportWithOneMarker extends APlay
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------

	private static final long	serialVersionUID	= 938304148943773422L;

	private ManToManMarkerRole		marker;
	
	
	private BallPossessionCrit	ballPossessionCrit	= null;
	

	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public SupportWithOneMarker(AIInfoFrame aiFrame)
	{
		super(EPlay.SUPPORT_WITH_ONE_MARKER, aiFrame);
		
		ballPossessionCrit = new BallPossessionCrit(EBallPossession.WE, EBallPossession.THEY, EBallPossession.BOTH);
		addCriterion(ballPossessionCrit);

		IVector2 initPos = aiFrame.worldFrame.ball.pos;	//aiFrame.tacticalInfo.getOpponentPassReceiver().pos;
		
		marker = new ManToManMarkerRole(EWAI.LEFT);
		addAggressiveRole(marker, initPos);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	protected void beforeUpdate(AIInfoFrame currentFrame)
	{
		marker.updateTarget(currentFrame.tacticalInfo.getOpponentPassReceiver());
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
		return false;
	}
}