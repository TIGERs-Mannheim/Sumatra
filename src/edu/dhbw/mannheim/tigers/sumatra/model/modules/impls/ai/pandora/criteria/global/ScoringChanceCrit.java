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

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeam;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.ACriterion;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.criteria.ECriterion;


/**
 * This class checks whether there is an approximate scoring chance for the opponent.
 * 
 * 
 * @author FlorianS
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class ScoringChanceCrit extends ACriterion
{
	/**
	 */
	public enum EPrecision
	{
		/** */
		APPROXIMATE,
		/** */
		DEFINITE;
	}
	
	/**
	 */
	public enum EScoringChance
	{
		/** */
		YES,
		/** */
		NO;
	}
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private final EScoringChance	scoringChance;
	private final EPrecision		approximate;
	private final ETeam				team;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param team
	 * @param approximate
	 * @param scoringChance
	 */
	public ScoringChanceCrit(ETeam team, EPrecision approximate, EScoringChance scoringChance)
	{
		super(ECriterion.SCORING_CHANCE);
		this.scoringChance = scoringChance;
		this.approximate = approximate;
		this.team = team;
	}
	
	
	/**
	 * @param team
	 * @param approximate
	 */
	public ScoringChanceCrit(ETeam team, EPrecision approximate)
	{
		this(team, approximate, EScoringChance.YES);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public float doCheckCriterion(AIInfoFrame currentFrame)
	{
		boolean result = false;
		switch (team)
		{
			case TIGERS:
				if (approximate == EPrecision.APPROXIMATE)
				{
					result = currentFrame.tacticalInfo.getTigersApproximateScoringChance();
				} else
				{
					result = currentFrame.tacticalInfo.getTigersScoringChance();
				}
				break;
			case OPPONENTS:
				if (approximate == EPrecision.APPROXIMATE)
				{
					result = currentFrame.tacticalInfo.getOpponentApproximateScoringChance();
				} else
				{
					result = currentFrame.tacticalInfo.getOpponentScoringChance();
				}
				break;
			default:
				throw new IllegalArgumentException("Team must be one of TIGERS or OPPONENTS");
		}
		
		if ((result && (scoringChance == EScoringChance.YES)) || (!result && (scoringChance == EScoringChance.NO)))
		{
			return MAX_SCORE;
		}
		return MIN_SCORE;
	}
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
