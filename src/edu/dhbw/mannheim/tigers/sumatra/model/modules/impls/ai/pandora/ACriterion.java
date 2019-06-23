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

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.criteria.ECriterion;


/**
 * This class was originally used as described here:
 * 
 * This class is the abstract class of any Criterion. Those are used to
 * calculate the playableScore of Plays. Every constructor of a Criterion shall
 * include the parameter 'wish' which can be of any type and determines an
 * aspect of a current situation which is more or less necessary for choosing a
 * certain Play. The parameter 'penaltyFactor' is a factor the playableScore
 * will be multiplied by if the condition of 'wish' is not fulfilled.
 * Optionally other parameters can be given for example to define a tolerance,
 * where no penalty should be given to playableScore.
 * 
 * However, now its usage and structure changed a bit.
 * A Criterion will calculate a score between 0-1 to determine from the current frame,
 * if some constraints are fulfilled.
 * Plays can add some criteria to say something about their playable score. If the criteria
 * of a play return a bad score, they won't come into the inner selection of plays.
 * 
 * @author FlorianS
 */
public abstract class ACriterion
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private final ECriterion		type;
	
	/** criterion fulfilled */
	protected static final float	MAX_SCORE	= 1f;
	/** knock-out criterion */
	protected static final float	MIN_SCORE	= 0f;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Create criterion of given type
	 * @param type of criterion
	 */
	public ACriterion(ECriterion type)
	{
		this.type = type;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * Makes sure the penalty factor is between 0 and 1.
	 * 
	 * @param score
	 * @return
	 */
	protected float normalizeScore(float score)
	{
		final float result;
		if (score > MAX_SCORE)
		{
			result = MAX_SCORE;
		} else if (score < MIN_SCORE)
		{
			result = MIN_SCORE;
		} else
		{
			result = score;
		}
		
		return result;
	}
	
	
	/**
	 * 
	 * @param currentFrame
	 * @return
	 */
	public float checkCriterion(AIInfoFrame currentFrame)
	{
		final float score = doCheckCriterion(currentFrame);
		return normalizeScore(score);
	}
	
	
	/**
	 * This method shall be implemented by any Criterion and returns a float
	 * value between 0 and 1 which will be multiplied with the basic playable
	 * score.
	 */
	protected abstract float doCheckCriterion(AIInfoFrame currentFrame);
	
	
	/**
	 * Type of this criterion
	 * 
	 * @return
	 */
	public ECriterion getType()
	{
		return type;
	}
	
	
	@Override
	public String toString()
	{
		return type.name();
	}
}
