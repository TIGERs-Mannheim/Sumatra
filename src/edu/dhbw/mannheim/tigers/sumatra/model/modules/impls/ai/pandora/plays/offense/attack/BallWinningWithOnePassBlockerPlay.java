/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 04.02.2011
 * Author(s):
 * FlorianS
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.offense.attack;

import edu.dhbw.mannheim.tigers.sumatra.model.data.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.EBallPossession;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.EGameSituation;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.ETeam;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.criteria.global.BallPossessionCrit;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.criteria.global.TeamClosestToBallCrit;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.criteria.local.OpponentPassReceiverCrit;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.BallGetterRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.other.PassBlockerRole;


/**
 * This play shall be selected if no one possesses the ball and one of the
 * opponent bots is closer to the ball than one of our bots. It includes one bot
 * trying to get the ball and another bot trying block a possible opponent pass
 * receiver.
 * 
 * This play contains an iteration over all opponent bots to determine which bot
 * shall be blocked from receiving a pass. If there are no opponent bots in the
 * worldFrame a default destination will be taken.
 * 
 * @author FlorianS
 * 
 */
public class BallWinningWithOnePassBlockerPlay extends APlay
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final long			serialVersionUID				= 1040191789572606182L;
	
	private BallGetterRole				ballGetter;
	private PassBlockerRole				passBlocker;
	
	private final float					BOT_RADIUS						= AIConfig.getGeometry().getBotRadius();
	private final Vector2f				FIELD_CENTER					= AIConfig.getGeometry().getCenter();
	
	private float							radius							= 2 * BOT_RADIUS + 200;
	private Vector2						direction						= new Vector2(AIConfig.INIT_VECTOR);
	
	private BallPossessionCrit			ballPossessionCrit			= null;
	private TeamClosestToBallCrit		closestToBallCrit				= null;
	private OpponentPassReceiverCrit	opponentPassReceiverCrit	= null;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public BallWinningWithOnePassBlockerPlay(AIInfoFrame aiFrame)
	{
		super(EPlay.BALLWINNING_WITH_ONE_PASS_BLOCKER, aiFrame);
		
		ballPossessionCrit = new BallPossessionCrit(EBallPossession.NO_ONE);
		closestToBallCrit = new TeamClosestToBallCrit(ETeam.OPPONENTS);
		opponentPassReceiverCrit = new OpponentPassReceiverCrit(true);
		addCriterion(ballPossessionCrit);
		addCriterion(closestToBallCrit);
		addCriterion(opponentPassReceiverCrit);
		
		Vector2f ballPos = aiFrame.worldFrame.ball.pos;
		
		Vector2 initPosGetter = new Vector2(ballPos);
		Vector2 initPosBlocker = new Vector2(ballPos);
		
		Vector2 goalCenter = new Vector2(AIConfig.getGeometry().getGoalTheir().getGoalCenter());
		
		ballGetter = new BallGetterRole(goalCenter, EGameSituation.GAME);
		passBlocker = new PassBlockerRole();
		addAggressiveRole(ballGetter, initPosGetter);
		addAggressiveRole(passBlocker, initPosBlocker);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	protected void beforeUpdate(AIInfoFrame currentFrame)
	{
		super.beforeUpdate(currentFrame);
		
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
		if (currentFrame.tacticalInfo.getBallPossesion().getEBallPossession() == EBallPossession.WE)
		{
			changeToSucceeded();
		}
		
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