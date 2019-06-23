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
 * This class determines whether there is one of our bots other than our
 * ball getter or ball carrier which can see the ball. If the current situation
 * relates to the desired situation the value 1 will be returned. Otherwise the
 * parameter 'penaltyFactor' will be returned.
 * 
 * @author FlorianS
 * 
 */
public class TigersPassReceiverCrit extends ACriterion
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private BotID		ignored	= new BotID();
	
	private boolean	wish		= false;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 * @param wish
	 */
	public TigersPassReceiverCrit(boolean wish)
	{
		super(ECriterion.TIGERS_PASS_RECEIVER);
		
		this.wish = wish;
	}
	
	
	/**
	 * 
	 * @param wish
	 * @param ignored
	 */
	public TigersPassReceiverCrit(boolean wish, BotID ignored)
	{
		super(ECriterion.TIGERS_PASS_RECEIVER);
		
		this.wish = wish;
		this.ignored = ignored;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public float doCheckCriterion(AIInfoFrame currentFrame)
	{
		final boolean passReceiver = checkPassReceiver(currentFrame.worldFrame, ignored);
		
		if (passReceiver == wish)
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
		
		TrackedBot ballGetter = null;
		final IVector2 ballPos = worldFrame.ball.getPos();
		float closestDistanceToBall = Float.MAX_VALUE;
		
		// identify our ball getter / ball carrier
		for (final TrackedBot currentBot : worldFrame.tigerBotsVisible.values())
		{
			float distanceToBall = GeoMath.distancePP(currentBot, ballPos);
			
			if ((distanceToBall < closestDistanceToBall))
			{
				closestDistanceToBall = distanceToBall;
				ballGetter = currentBot;
			}
		}
		
		if (ballGetter == null)
		{
			return false;
		}
		
		
		// check whether there is a potential pass receiver which also can see our goal
		for (final TrackedBot currentBot : worldFrame.tigerBotsVisible.values())
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
			if (!currentBot.getId().equals(ballGetter.getId()) && ballVisibleForThem)
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