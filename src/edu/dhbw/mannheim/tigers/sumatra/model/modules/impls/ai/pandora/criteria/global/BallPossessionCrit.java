/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 10.03.2011
 * Author(s):
 * FlorianS
 * ChristianK
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.criteria.global;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ballpossession.EBallPossession;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.ACriterion;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.criteria.ECriterion;


/**
 * This class determines which team possesses the ball. If the current
 * situation relates to the desired situation the value 1 will be returned.
 * Otherwise the parameter 'penaltyFactor' will be returned.
 * 
 * @author FlorianS
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class BallPossessionCrit extends ACriterion
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private final Set<EBallPossession>	ballPossessions;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Create a ballPossession criterion with given ballPossession.
	 * Create additional criteria, if you need more than one ballPossession
	 * 
	 * @param ballPossessions
	 */
	public BallPossessionCrit(EBallPossession... ballPossessions)
	{
		super(ECriterion.BALL_POSSESSION);
		
		this.ballPossessions = new HashSet<EBallPossession>(Arrays.asList(ballPossessions));
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public float doCheckCriterion(AIInfoFrame currentFrame)
	{
		for (final EBallPossession ballPossession : ballPossessions)
		{
			if (ballPossession == currentFrame.tacticalInfo.getBallPossession().getEBallPossession())
			{
				return MAX_SCORE;
			}
		}
		return MIN_SCORE;
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
