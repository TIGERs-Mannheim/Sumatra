/* 
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 15.04.2011
 * Author(s):
 * MalteM
 *
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.criteria;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;

/**
 * Interface for all criteria.
 * 
 * @author MalteM, FlorianM
 * 
 */
public interface ICriterion
{
	/**
	 * This method shall be implemented by any Criterion and returns a float
	 * value between 0 and 1 which will be multiplied with the basic playable
	 * score.
	 */
	public float doCheckCriterion(AIInfoFrame currentFrame);
	
	/**
	 * Makes sure the penalty factor is between 0 and 1.
	 * 
	 * @param penaltyFactor
	 * @return
	 */
	public float normalizePenaltyFactor(float penaltyFactor);
	
	/**
	 * Returns the ECriterion type.
	 */
	public ECriterion getType();
	
	/**
	 * Sets the penalty factor for the given criterion.
	 * 
	 * @param factor
	 */
	public void setPenaltyFactor(float factor);
	
	public float getPenaltyFactor();
}
