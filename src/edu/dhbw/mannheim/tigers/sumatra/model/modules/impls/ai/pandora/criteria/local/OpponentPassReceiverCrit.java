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

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
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
	
	private final BotID		ignored;
	
	private final boolean	canPass;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 * @param canPass
	 */
	public OpponentPassReceiverCrit(boolean canPass)
	{
		this(canPass, new BotID());
	}
	
	
	/**
	 * 
	 * @param canPass
	 * @param ignored
	 */
	public OpponentPassReceiverCrit(boolean canPass, BotID ignored)
	{
		super(ECriterion.OPPONENT_PASS_RECEIVER);
		
		this.canPass = canPass;
		this.ignored = ignored;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public float doCheckCriterion(AIInfoFrame currentFrame)
	{
		final boolean passReceiver = checkPassReceiver(currentFrame.worldFrame, ignored);
		
		if (passReceiver == canPass)
		{
			return MAX_SCORE;
		}
		return MIN_SCORE;
	}
	
	
	/**
	 * 
	 * @param worldFrame
	 * @param ignored
	 * @return
	 */
	public static boolean checkPassReceiver(WorldFrame worldFrame, BotID ignored)
	{
		boolean passReceiver = false;
		
		TrackedBot opponentBallGetter = null;
		final IVector2 ballPos = worldFrame.ball.getPos();
		float closestDistanceToBall = Float.MAX_VALUE;
		
		// identify their ball getter / ball carrier
		for (final TrackedBot currentBot : worldFrame.foeBots.values())
		{
			float distanceToBall = GeoMath.distancePP(currentBot, ballPos);
			
			if ((distanceToBall < closestDistanceToBall))
			{
				closestDistanceToBall = distanceToBall;
				opponentBallGetter = currentBot;
			}
		}
		
		if (opponentBallGetter == null)
		{
			return false;
		}
		
		
		// check whether there is a potential opponent pass receiver which also can see our goal
		for (final TrackedBot currentBot : worldFrame.foeBots.values())
		{
			boolean ballVisibleForThem;
			if (!ignored.isUninitializedID())
			{
				ballVisibleForThem = GeoMath.p2pVisibility(worldFrame, currentBot.getPos(), ballPos, ignored);
			} else
			{
				ballVisibleForThem = GeoMath.p2pVisibility(worldFrame, currentBot.getPos(), ballPos);
			}
			
			
			// exclude opponent ball getter and check whether the bot can see the ball
			if (!currentBot.getId().equals(opponentBallGetter.getId()) && ballVisibleForThem)
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