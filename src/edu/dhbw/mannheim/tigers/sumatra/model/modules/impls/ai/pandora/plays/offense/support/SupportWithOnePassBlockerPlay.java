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

import edu.dhbw.mannheim.tigers.sumatra.model.data.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.EBallPossession;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.criteria.global.BallPossessionCrit;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.other.PassBlockerRole;


/**
 * This play could be selected to support our ball getter / carrier when the
 * number of bots in our defense play and our offense play does not sum up five.
 * Only one pass blocker will get between the ball and the opponent bot which is
 * second closest to ball
 * 
 * @author FlorianS
 * 
 */
public class SupportWithOnePassBlockerPlay extends APlay
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final long	serialVersionUID		= -3157417737261279490L;
	
	private PassBlockerRole		passBlocker;
	
	private final float			BOT_RADIUS				= AIConfig.getGeometry().getBotRadius();
	private final float			radius					= 2 * BOT_RADIUS + 200;
	private Vector2				direction				= new Vector2(AIConfig.INIT_VECTOR);
	
	private final Vector2f		FIELD_CENTER			= AIConfig.getGeometry().getCenter();
	
	private BallPossessionCrit	ballPossessionCrit	= null;
	
	
	// private boolean ballVisibleForThem = false;
	// private boolean goalVisibleForThem = false;
	
	
	// private OpponentPassReceiverCrit opponentPassReceiverCrit = null;
	// private TeamClosestToBallCrit teamClosestToBallCrit = null;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public SupportWithOnePassBlockerPlay(AIInfoFrame aiFrame)
	{
		super(EPlay.SUPPORT_WITH_ONE_PASS_BLOCKER, aiFrame);
		
		ballPossessionCrit = new BallPossessionCrit(EBallPossession.WE, EBallPossession.THEY, EBallPossession.BOTH);
		// teamClosestToBallCrit = new TeamClosestToBallCrit(ETeam.OPPONENTS);
		// opponentPassReceiverCrit = new OpponentPassReceiverCrit(true);
		addCriterion(ballPossessionCrit);
		// addCriterion(teamClosestToBallCrit);
		// addCriterion(opponentPassReceiverCrit);
		
		Vector2f goalCenter = AIConfig.getGeometry().getGoalTheir().getGoalCenter();
		
		Vector2 initPosPassBlocker = new Vector2(goalCenter);
		
		passBlocker = new PassBlockerRole();
		addAggressiveRole(passBlocker, initPosPassBlocker);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	protected void beforeUpdate(AIInfoFrame currentFrame)
	{
		WorldFrame worldFrame = currentFrame.worldFrame;
		
		Vector2 ballPos = new Vector2(worldFrame.ball.pos);
		TrackedBot opponentPassReceiver = currentFrame.tacticalInfo.getOpponentPassReceiver();
		
		if (opponentPassReceiver != null)
		{
			direction = ballPos.subtractNew(opponentPassReceiver.pos);
			passBlocker.updateCirclePos(opponentPassReceiver.pos, radius, direction);
		} else
		{
			direction = new Vector2(AIConfig.INIT_VECTOR);
			passBlocker.updateCirclePos(FIELD_CENTER, radius, direction);
		}
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