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
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.EBallPossession;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.criteria.global.BallPossessionCrit;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.EWAI;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.defense.ManToManMarkerRole;


/**
 * This play shall be selected if our opponent possesses the ball and there is
 * no possible pass receiver for the opponent ball carrier. It includes two bots
 * man-marking the opponent ball carrier.
 * 
 * @author FlorianS
 * 
 */
public class BallCapturingWithDoublingPlay extends APlay
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final long			serialVersionUID				= -1307313321394848076L;
	private static final int			UNINITIALIZED_ID				= -1;
	
	private ManToManMarkerRole			markerOne;
	private ManToManMarkerRole			markerTwo;
	
	private BallPossessionCrit			ballPossessionCrit			= null;
//	private OpponentPassReceiverCrit	opponentPassReceiverCrit	= null;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public BallCapturingWithDoublingPlay(AIInfoFrame aiFrame)
	{
		super(EPlay.BALLCAPTURING_WITH_DOUBLING, aiFrame);
		
		ballPossessionCrit = new BallPossessionCrit(EBallPossession.THEY);
//		opponentPassReceiverCrit = new OpponentPassReceiverCrit(false);
		addCriterion(ballPossessionCrit);
//		addCriterion(opponentPassReceiverCrit);
		
		Vector2f ballPos = aiFrame.worldFrame.ball.pos;
		
		Vector2 initPosMarkerFirst = new Vector2(ballPos);
		Vector2 initPosMarkerSecond = new Vector2(ballPos);
		
		markerOne = new ManToManMarkerRole(EWAI.FIRST);
		markerTwo = new ManToManMarkerRole(EWAI.SECOND);
		addAggressiveRole(markerOne, initPosMarkerFirst);
		addAggressiveRole(markerTwo, initPosMarkerSecond);
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	protected void beforeUpdate(AIInfoFrame currentFrame)
	{
		super.beforeUpdate(currentFrame);
		
		TrackedBot opponentBallCarrier = null;
		
		float distance = UNINITIALIZED_ID;
		float closestDistance = UNINITIALIZED_ID;
		Vector2f ballPos = currentFrame.worldFrame.ball.pos;
		
		if (!currentFrame.worldFrame.foeBots.isEmpty())
		{
			// identify the opponent ball carrier
			for (TrackedBot currentBot : currentFrame.worldFrame.foeBots.values())
			{
				distance = AIMath.distancePP(currentBot, ballPos);
				if (closestDistance == UNINITIALIZED_ID || distance < closestDistance)
				{
					closestDistance = distance;
					opponentBallCarrier = currentBot;
				}
			}
		}
		
		if (opponentBallCarrier != null)
		{
			markerOne.updateTarget(opponentBallCarrier);
			markerTwo.updateTarget(opponentBallCarrier);
		} else
		{
			markerOne.updateTarget(ballPos);
			markerTwo.updateTarget(ballPos);
		}
		
	}
	

	@Override
	protected void afterUpdate(AIInfoFrame currentFrame)
	{
		if (currentFrame.tacticalInfo.getBallPossesion().getEBallPossession() != EBallPossession.THEY)
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