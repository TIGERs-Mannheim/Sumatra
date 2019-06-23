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

import java.util.EnumMap;
import java.util.Map;

import org.apache.commons.configuration.Configuration;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EMatchBehavior;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;


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
	
	private static final String				NODE_PATH								= "tactics.";
	/** */
	public static final String					UNITIALIZED_TACTICAL_ORIENTATION	= "NOT_DEFINED";
	
	private final EMatchBehavior				tacticalOrientation;
	private final Map<EPlay, PlayConfig>	plays;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param config
	 */
	public Tactics(Configuration config)
	{
		plays = new EnumMap<EPlay, PlayConfig>(EPlay.class);
		tacticalOrientation = EMatchBehavior.valueOf(config.getString(NODE_PATH + "tacticalOrientation",
				UNITIALIZED_TACTICAL_ORIENTATION));
		
		for (final EPlay play : EPlay.values())
		{
			plays.put(play, new PlayConfig(config, play));
		}
	}
	
	/**
	 */
	public static class PlayConfig
	{
		/**
		 * @param config
		 * @param type
		 */
		public PlayConfig(Configuration config, EPlay type)
		{
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param type
	 * @return
	 */
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
