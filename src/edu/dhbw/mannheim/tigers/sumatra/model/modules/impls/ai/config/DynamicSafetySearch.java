/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 19.01.2011
 * Author(s): Oliver Steinbrecher <OST1988@aol.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config;

import org.apache.commons.configuration.XMLConfiguration;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2f;


/**
 * 
 * Configuration object for dynamic safety search (pathplaning).
 * 
 * @author Oliver Steinbrecher <OST1988@aol.com>
 * 
 */
public class DynamicSafetySearch
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private final String		nodePath	= "pathplanning.dynamicSafetySearch.";
	
	private final Vector2f	maxAcceleration;
	private final Vector2f	maxDeceleration;
	private final Vector2f	maxVelocity;
	
	private final float		botRadiusExtra;
	private final float		randomCirclesMax;
	private final float		epsilon;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public DynamicSafetySearch(XMLConfiguration configFile)
	{
		this.maxAcceleration = getVector(configFile, nodePath + "accMax");
		this.maxDeceleration = getVector(configFile, nodePath + "decMax");
		this.maxVelocity = getVector(configFile, nodePath + "velMax");
		
		this.botRadiusExtra = configFile.getFloat(nodePath + "botRadiusExtra");
		this.randomCirclesMax = configFile.getFloat(nodePath + "randomCirclesMax");
		this.epsilon = configFile.getFloat(nodePath + "epsilon");
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public float getEpsilon()
	{
		return epsilon;
	}
	

	private Vector2f getVector(XMLConfiguration config, String value)
	{
		float x = config.getFloat(value + ".vector.x");
		float y = config.getFloat(value + ".vector.y");
		return new Vector2f(x, y);
	}
	

	public Vector2f getMaxAcceleration()
	{
		return maxAcceleration;
	}
	

	public Vector2f getMaxDeceleration()
	{
		return maxDeceleration;
	}
	

	public Vector2f getMaxVelocity()
	{
		return maxVelocity;
	}
	

	public float getBotRadiusExtra()
	{
		return botRadiusExtra;
	}
	

	public float getRandomCirclesMax()
	{
		return randomCirclesMax;
	}
	

}
