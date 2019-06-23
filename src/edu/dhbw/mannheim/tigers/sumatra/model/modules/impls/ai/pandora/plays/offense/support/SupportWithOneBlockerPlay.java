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
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.other.MoveRole;


/**
 * This play could be selected to support our ball getter / carrier when the
 * number of bots in our defense play and our offense play does not sum up five.
 * Only one blocker will get between the ball and the opponent bot which is
 * closest to ball
 * 
 * @author FlorianS
 * 
 */
public class SupportWithOneBlockerPlay extends APlay
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final long	serialVersionUID		= -4745805080004584804L;
	
	private MoveRole				blocker;
	
	private float					radius;
	private final float			BOT_RADIUS				= AIConfig.getGeometry().getBotRadius();
	private final float			MIN_RADIUS				= 3 * BOT_RADIUS;
	private Vector2				direction				= new Vector2(AIConfig.INIT_VECTOR);
	
	private BallPossessionCrit	ballPossessionCrit	= null;
	
	
	// private TeamClosestToBallCrit closestToBallCrit = null;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public SupportWithOneBlockerPlay(AIInfoFrame aiFrame)
	{
		super(EPlay.SUPPORT_WITH_ONE_BLOCKER, aiFrame);
		
		ballPossessionCrit = new BallPossessionCrit(EBallPossession.NO_ONE);
		// closestToBallCrit = new TeamClosestToBallCrit(ETeam.TIGERS);
		addCriterion(ballPossessionCrit);
		// addCriterion(closestToBallCrit);
		
		Vector2f goalCenter = AIConfig.getGeometry().getGoalTheir().getGoalCenter();
		
		Vector2 initPosBlocker = new Vector2(goalCenter);
		
		blocker = new MoveRole();
		addAggressiveRole(blocker, initPosBlocker);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	protected void beforeFirstUpdate(AIInfoFrame frame)
	{
	}
	
	
	@Override
	protected void beforeUpdate(AIInfoFrame currentFrame)
	{
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
