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
 * This class checks whether there is a scoring chance for us by determining
 * whether our ball carrier can see the baseline between the goal posts on the
 * opponent's side of the field. If the current situation relates to the
 * desired situation the value 1 will be returned. Otherwise the parameter
 * 'penaltyFactor' will be returned.
 * 
 * @author FlorianS
 * 
 */
public class TigersScoringChanceCrit extends ACriterion
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private boolean	wish	= false;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public TigersScoringChanceCrit(boolean wish, float penaltyFactor)
	{
		super(ECriterion.TIGERS_SCORING_CHANCE, penaltyFactor);
		
		this.wish = wish;
	}
	

	public TigersScoringChanceCrit(boolean wish)
	{
		super(ECriterion.TIGERS_SCORING_CHANCE);
		
		this.wish = wish;
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public float doCheckCriterion(AIInfoFrame currentFrame)
	{
		

		if (currentFrame.tacticalInfo.getTigersScoringChance() == wish)
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
