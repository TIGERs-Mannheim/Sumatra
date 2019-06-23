/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 13.05.2011
 * Author(s): Malte
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration.XMLConfiguration;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.criteria.ECriterion;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EMatchBehavior;


/**
 * Configuration class for tactics parameters.
 * 
 * @author Malte
 * 
 */
public class Tactics
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private final int								minPlayableScore;
	private final int								maxPlayableScore;
	private final int								defaultPlayableScore;
	private final EMatchBehavior				tacticalOrientation;
	
	public final static float					UNINITIALIZED_PENALTY_FACTOR	= -1;
	public final static String					UNITIALIZED_TACTICAL_ORIENTATION	= "NOT_DEFINED";
	private final Map<EPlay, PlayConfig>	plays;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public Tactics(XMLConfiguration config)
	{
		plays = new HashMap<EPlay, PlayConfig>();
		minPlayableScore = config.getInt("minPlayableScore");
		maxPlayableScore = config.getInt("maxPlayableScore");
		defaultPlayableScore = config.getInt("defaultPlayableScore");
		tacticalOrientation = EMatchBehavior.valueOf(config.getString("tacticalOrientation", UNITIALIZED_TACTICAL_ORIENTATION));
		
		for (EPlay play : EPlay.values())
		{
			plays.put(play, new PlayConfig(config, play));
		}
	}
	
	public class PlayConfig
	{
		private final int								basicPlayableScore;
		private static final String				NODE			= "plays.";
		private final Map<ECriterion, Float>	critFactors	= new HashMap<ECriterion, Float>();
		
		
		public PlayConfig(XMLConfiguration config, EPlay type)
		{
			String playPath = NODE + type.name() + ".";
			basicPlayableScore = config.getInt(playPath + "basicPlayableScore", defaultPlayableScore);
			for (ECriterion crit : ECriterion.values())
			{
				critFactors.put(crit,
						config.getFloat(playPath + "criteria." + crit.toString(), UNINITIALIZED_PENALTY_FACTOR));
			}
		}
		

		public int getBasicPlayableScore()
		{
			return this.basicPlayableScore;
		}
		

		public float getPenaltyFactor(ECriterion crit)
		{
			return critFactors.get(crit);
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public int getMinPlayableScore()
	{
		return minPlayableScore;
	}
	

	public int getMaxPlayableScore()
	{
		return maxPlayableScore;
	}
	

	public int getDefaultPlayableScore()
	{
		return defaultPlayableScore;
	}
	

	public PlayConfig getPlay(EPlay type)
	{
		return plays.get(type);
	}


	/**
	 * @return the tacticalOrientation
	 */
	public EMatchBehavior getTacticalOrientation()
	{
		return tacticalOrientation;
	}
	

}
