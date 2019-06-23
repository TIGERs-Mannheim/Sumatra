/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 18, 2013
 * Author(s): Dirk Klostermann <klostermannn@googlemail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config;

import org.apache.commons.configuration.Configuration;


/**
 * config parameters for the optimization of a path
 * 
 * @author Dirk Klostermann <klostermannn@googlemail.com>
 * 
 */
public class OptimizationConfig
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final String	NODE_PATH				= "pathplanning.optimization.";
	
	private static final String	NODE_PATH_Collision	= "pathplanning.optimization.collisionDetection.";
	
	/** Points on a path with a combined angle*distance score below this value will be removed */
	private final float				pathReductionScore;
	
	/** how much is the bot allowed to differ from the current spline until a new spline is calculated[mm] */
	private final int					allowedDistanceToSpline;
	
	/** if the new calculated path is faster by this amount of time it will replace the old path [s] */
	private final float				useShorterPathIfFaster;
	
	
	/** collision detection **/
	private final float				ignoreFirstSeconds;
	private final float				ignoreLastSeconds;
	private final int					collisionIterationsMaximum;
	private final float				collisionIterationsStepSize;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param configFile
	 */
	public OptimizationConfig(Configuration configFile)
	{
		pathReductionScore = configFile.getFloat(NODE_PATH + "pathReductionScore");
		allowedDistanceToSpline = configFile.getInt(NODE_PATH + "allowedDistanceToSpline");
		useShorterPathIfFaster = configFile.getFloat(NODE_PATH + "useShorterPathIfFaster");
		ignoreFirstSeconds = configFile.getFloat(NODE_PATH_Collision + "ignoreFirstSeconds");
		ignoreLastSeconds = configFile.getFloat(NODE_PATH_Collision + "ignoreLastSeconds");
		collisionIterationsMaximum = configFile.getInt(NODE_PATH_Collision + "collisionIterationsMaximum");
		collisionIterationsStepSize = configFile.getFloat(NODE_PATH_Collision + "collisionIterationsStepSize");
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @return the pathReductionScore
	 */
	public float getPathReductionScore()
	{
		return pathReductionScore;
	}
	
	
	/**
	 * @return the allowedDistanceToSpline
	 */
	public int getAllowedDistanceToSpline()
	{
		return allowedDistanceToSpline;
	}
	
	
	/**
	 * @return the ignoreFirstSeconds
	 */
	public float getIgnoreFirstSeconds()
	{
		return ignoreFirstSeconds;
	}
	
	
	/**
	 * @return the ignoreLastSeconds
	 */
	public float getIgnoreLastSeconds()
	{
		return ignoreLastSeconds;
	}
	
	
	/**
	 * @return the collisionIterationsMaximum
	 */
	public int getCollisionIterationsMaximum()
	{
		return collisionIterationsMaximum;
	}
	
	
	/**
	 * @return the collisionIterationsStepSize
	 */
	public float getCollisionIterationsStepSize()
	{
		return collisionIterationsStepSize;
	}
	
	
	/**
	 * @return the useShorterPathIfFaster
	 */
	public float getUseShorterPathIfFaster()
	{
		return useShorterPathIfFaster;
	}
	
	
}
