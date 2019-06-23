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
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.BallGetterRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.other.MoveRole;


/**
 * This play shall be selected if no one possesses the ball and one of our bots
 * is closer to the ball than an opponent bot. It includes one bot trying to
 * get the ball and another bot trying to prevent our opponent from getting the
 * ball by moving between ball and the opponent ball getter with the shortest
 * distance to the ball.
 * 
 * This play contains an iteration over all opponent bots to determine which bot
 * shall be blocked from getting the ball. If there are no opponent bots in the
 * worldFrame a default destination will be taken.
 * 
 * @author FlorianS
 * 
 */
public class BallWinningWithOneBlockerPlay extends APlay
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final long		serialVersionUID		= -4745805080004584804L;
	
	private BallGetterRole			ballGetter;
	private MoveRole					blocker;
	
	private float						radius;
	private final float				BOT_RADIUS				= AIConfig.getGeometry().getBotRadius();
	private final float				MIN_RADIUS				= 3 * BOT_RADIUS;
	private Vector2					direction				= new Vector2(AIConfig.INIT_VECTOR);
	
	private BallPossessionCrit		ballPossessionCrit	= null;
	private TeamClosestToBallCrit	closestToBallCrit		= null;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public BallWinningWithOneBlockerPlay(AIInfoFrame aiFrame)
	{
		super(EPlay.BALLWINNING_WITH_ONE_BLOCKER, aiFrame);
		
		ballPossessionCrit = new BallPossessionCrit(EBallPossession.NO_ONE);
		closestToBallCrit = new TeamClosestToBallCrit(ETeam.TIGERS);
		addCriterion(ballPossessionCrit);
		addCriterion(closestToBallCrit);
		
		Vector2f ballPos = aiFrame.worldFrame.ball.pos;
		
		Vector2 initPosGetter = new Vector2(ballPos);
		Vector2 initPosBlocker = new Vector2(ballPos);
		
		Vector2 goalCenter = new Vector2(AIConfig.getGeometry().getGoalTheir().getGoalCenter());
		
		ballGetter = new BallGetterRole(goalCenter, EGameSituation.GAME);
		blocker = new MoveRole();
		addAggressiveRole(ballGetter, initPosGetter);
		addAggressiveRole(blocker, initPosBlocker);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	protected void beforeUpdate(AIInfoFrame currentFrame)
	{
		super.beforeUpdate(currentFrame);
		
		WorldFrame worldFrame = currentFrame.worldFrame;
		
		Vector2f ballPos = worldFrame.ball.pos;
		TrackedBot opponentBallGetter = currentFrame.tacticalInfo.getOpponentBallGetter();
		
		if (opponentBallGetter != null)
		{
			radius = ballPos.subtractNew(opponentBallGetter.pos).getLength2() / 2;
			direction = opponentBallGetter.pos.subtractNew(ballPos);
		} else
		{
			radius = MIN_RADIUS;
			direction = new Vector2(AIConfig.INIT_VECTOR);
		}
		
		if (radius < MIN_RADIUS)
		{
			radius = MIN_RADIUS;
		}
		
		blocker.updateCirclePos(ballPos, radius, direction);
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
