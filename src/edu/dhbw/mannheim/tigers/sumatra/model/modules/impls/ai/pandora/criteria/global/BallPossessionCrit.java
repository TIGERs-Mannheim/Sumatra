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

import java.util.ArrayList;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.EBallPossession;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.ACriterion;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.criteria.ECriterion;


/**
 * This class determines which team possesses the ball. If the current
 * situation relates to the desired situation the value 1 will be returned.
 * Otherwise the parameter 'penaltyFactor' will be returned.
 * 
 * @author FlorianS
 * 
 */
public class BallPossessionCrit extends ACriterion
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private final List<EBallPossession>	wishes	= new ArrayList<EBallPossession>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public BallPossessionCrit(float penaltyFactor, EBallPossession... wishes)
	{
		super(ECriterion.BALL_POSSESSION, penaltyFactor);
		
		// add wishes to wish list
		for (EBallPossession input : wishes)
		{
			this.wishes.add(input);
		}
	}
	

	public BallPossessionCrit(EBallPossession... wishes)
	{
		super(ECriterion.BALL_POSSESSION);
		
		// add wishes to wish list
		for (EBallPossession input : wishes)
		{
			this.wishes.add(input);
		}
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public float doCheckCriterion(AIInfoFrame currentFrame)
	{
		for(EBallPossession wish : wishes)
		{
			if (wish == (currentFrame.tacticalInfo.getBallPossesion().getEBallPossession()))
			{
				return 1.0f;
			}
		}
		
		return penaltyFactor;
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
