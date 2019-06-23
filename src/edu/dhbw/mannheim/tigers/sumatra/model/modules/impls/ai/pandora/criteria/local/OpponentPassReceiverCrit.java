/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 19.03.2011
 * Author(s):
 * FlorianS
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.criteria.local;

import edu.dhbw.mannheim.tigers.sumatra.model.data.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.ACriterion;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.criteria.ECriterion;


/**
 * This class determines whether there is an opponent bot other than the
 * opponent ball getter or the opponent ball carrier which can see the ball. If
 * the current situation relates to the desired situation the value 1 will be
 * returned. Otherwise the parameter 'penaltyFactor' will be returned.
 * 
 * @author FlorianS
 * 
 */
public class OpponentPassReceiverCrit extends ACriterion
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final int	UNINITIALIZED_ID	= -1;
	private int						ignored				= UNINITIALIZED_ID;
	
	private boolean				wish					= false;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public OpponentPassReceiverCrit(boolean wish, float penaltyFactor)
	{
		super(ECriterion.OPPONENT_PASS_RECEIVER, penaltyFactor);
		
		this.wish = wish;
		this.penaltyFactor = normalizePenaltyFactor(penaltyFactor);
	}
	

	public OpponentPassReceiverCrit(boolean wish, float penaltyFactor, int ignored)
	{
		super(ECriterion.OPPONENT_PASS_RECEIVER, penaltyFactor);
		
		this.wish = wish;
		this.penaltyFactor = penaltyFactor;
		this.ignored = ignored;
	}
	

	public OpponentPassReceiverCrit(boolean wish)
	{
		super(ECriterion.OPPONENT_PASS_RECEIVER);
		
		this.wish = wish;
	}
	

	public OpponentPassReceiverCrit(boolean wish, int ignored)
	{
		super(ECriterion.OPPONENT_PASS_RECEIVER);
		
		this.wish = wish;
		this.ignored = ignored;
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public float doCheckCriterion(AIInfoFrame currentFrame)
	{
		boolean passReceiver = checkPassReceiver(currentFrame.worldFrame, ignored);
		
		if (passReceiver == wish)
		{
			return 1;
		} else
		{
			return penaltyFactor;
		}
	}
	

	public static boolean checkPassReceiver(WorldFrame worldFrame, int ignored)
	{
		boolean passReceiver = false;
		
		TrackedBot opponentBallGetter = null;
		Vector2f ballPos = worldFrame.ball.pos;
		float distanceToBall = UNINITIALIZED_ID;
		float closestDistanceToBall = UNINITIALIZED_ID;
		
		// identify their ball getter / ball carrier
		for (TrackedBot currentBot : worldFrame.foeBots.values())
		{
			distanceToBall = AIMath.distancePP(currentBot, ballPos);
			
			if (closestDistanceToBall == UNINITIALIZED_ID || distanceToBall < closestDistanceToBall)
			{
				closestDistanceToBall = distanceToBall;
				opponentBallGetter = currentBot;
			}
		}
		

		// check whether there is a potential opponent pass receiver which also can see our goal
		for (TrackedBot currentBot : worldFrame.foeBots.values())
		{
			boolean ballVisibleForThem;
			if (ignored != UNINITIALIZED_ID)
			{
				ballVisibleForThem = AIMath.p2pVisibility(worldFrame, currentBot.pos, ballPos, ignored);
			} else
			{
				ballVisibleForThem = AIMath.p2pVisibility(worldFrame, currentBot.pos, ballPos);
			}
			

			// exclude opponent ball getter and check whether the bot can see the ball
			if (currentBot.id != opponentBallGetter.id && ballVisibleForThem)
			{
				passReceiver = true;
			}
		}
		
		return passReceiver;
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}