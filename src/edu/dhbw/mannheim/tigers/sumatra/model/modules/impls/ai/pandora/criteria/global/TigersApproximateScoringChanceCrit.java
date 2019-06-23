/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 22.03.2011
 * Author(s):
 * FlorianS
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.criteria.global;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.ACriterion;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.criteria.ECriterion;


/**
 * This class checks whether there is an approximate scoring chance for us.
 * If the current situation relates to the desired situation the value 1 will be
 * returned. Otherwise the parameter 'penaltyFactor' will be returned.
 * 
 * @author FlorianS
 * 
 */
public class TigersApproximateScoringChanceCrit extends ACriterion
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private boolean	wish	= false;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public TigersApproximateScoringChanceCrit(boolean wish, float penaltyFactor)
	{
		super(ECriterion.TIGERS_APPROXIMATE_SCORING_CHANCE, penaltyFactor);
		
		this.wish = wish;
	}
	

	public TigersApproximateScoringChanceCrit(boolean wish)
	{
		super(ECriterion.TIGERS_APPROXIMATE_SCORING_CHANCE);
		
		this.wish = wish;
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public float doCheckCriterion(AIInfoFrame currentFrame)
	{
		
		if (currentFrame.tacticalInfo.getTigersApproximateScoringChance() == wish)
		{
			return 1.0f;
		} else
		{
			return penaltyFactor;
		}
	}
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
