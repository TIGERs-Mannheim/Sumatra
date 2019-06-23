/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 10.03.2011
 * Author(s):
 * FlorianS
 * ChristianK
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.criteria.ECriterion;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.criteria.ICriterion;


/**
 * This class is the abstract class of any Criterion. Those are used to 
 * calculate the playableScore of Plays. Every constructor of a Criterion shall 
 * include the parameter 'wish' which can be of any type and determines an 
 * aspect of a current situation which is more or less necessary for choosing a 
 * certain Play. The parameter 'penaltyFactor' is a factor the playableScore 
 * will be multiplied by if the condition of 'wish' is not fulfilled. 
 * Optionally other parameters can be given for example to define a tolerance, 
 * where no penalty should be given to playableScore.
 * 
 * @author FlorianS
 */
public abstract class ACriterion implements ICriterion
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	protected final ECriterion		type;
	
	// no influence
	public static final float	MAX_PENALTY_FACTOR	= 1f;
	// knock-out criterion
	public static final float	MIN_PENALTY_FACTOR	= 0f;
	
	protected float penaltyFactor;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public ACriterion(ECriterion type, float penaltyFactor)
	{
		this.type = type;
		this.penaltyFactor = normalizePenaltyFactor(penaltyFactor);
	}
	
	/**
	  * Creates a criterion with penaltyFactor = 1.
	  * So it doesn't has any impact.
	  * Penalty factor can be overwritten later via a setter function
	  * or the entry in the tactics xml file.
	  */
	public ACriterion(ECriterion type)
	{
		this(type, MAX_PENALTY_FACTOR);
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public float normalizePenaltyFactor(float penaltyFactor)
	{	
		if (penaltyFactor > MAX_PENALTY_FACTOR)
		{
			penaltyFactor = MAX_PENALTY_FACTOR;
		} else if (penaltyFactor < MIN_PENALTY_FACTOR)
		{
			penaltyFactor = MIN_PENALTY_FACTOR;
		}
		
		return penaltyFactor;
	}
	
	@Override
	public ECriterion getType()
	{
		return this.type;
	}
	
	@Override
	public void setPenaltyFactor(float factor)
	{
		this.penaltyFactor = factor;	
	}
	
	@Override
	public float getPenaltyFactor()
	{
		return this.penaltyFactor;
	}
	
	
}
