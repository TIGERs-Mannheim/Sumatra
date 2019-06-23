/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 18, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config;

import org.apache.commons.configuration.Configuration;


/**
 * Configs specific to a bot type
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class BotConfig
{
	
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private General		general		= null;
	private Tolerances	tolerances	= null;
	private Skills			skills		= null;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @param newConfig
	 */
	public BotConfig(Configuration newConfig)
	{
		general = new General(newConfig);
		tolerances = new Tolerances(newConfig);
		skills = new Skills(newConfig);
	}
	
	
	/**
	 * @param newConfig
	 * @param base
	 */
	public BotConfig(Configuration newConfig, BotConfig base)
	{
		general = new General(newConfig, base.general);
		tolerances = new Tolerances(newConfig, base.tolerances);
		skills = new Skills(newConfig, base.skills);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @return the general
	 */
	public final General getGeneral()
	{
		return general;
	}
	
	
	/**
	 * @return the tolerances
	 */
	public final Tolerances getTolerances()
	{
		return tolerances;
	}
	
	
	/**
	 * @return the skills
	 */
	public final Skills getSkills()
	{
		return skills;
	}
}
