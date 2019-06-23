/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 20.01.2011
 * Author(s): Oliver Steinbrecher <OST1988@aol.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration.XMLConfiguration;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;


/**
 * Configuration object for the scoring system.
 * 
 * @author Oliver Steinbrecher <OST1988@aol.com>
 * 
 */
public class ScoringSystem
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final String	nodePath	= "scoringSystem.";
	
	private final int		minPlayableScore;
	private final int		maxPlayableScore;
	private final int		defaultPlayableScore;
	
	private final Map<EPlay,Integer> basicPlayableScores;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public ScoringSystem(XMLConfiguration configFile)
	{
		minPlayableScore = configFile.getInt(nodePath + "minPlayableScore");
		maxPlayableScore = configFile.getInt(nodePath + "maxPlayableScore");
		defaultPlayableScore = configFile.getInt(nodePath + "defaultPlayableScore");
		
		basicPlayableScores = new HashMap<EPlay,Integer>();
		for(EPlay play : EPlay.values())
		{
			basicPlayableScores.put(play,configFile.getInt(nodePath+"plays."+play.toString()+".basicPlayableScore",defaultPlayableScore));
		}
	}
	

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


	/**
	 * Returns the basic playable score for a given play.
	 * 
	 * @author Malte
	 */
	public int getBasicPlayableScore(EPlay type)
	{
		return basicPlayableScores.get(type);
	}
	
}
