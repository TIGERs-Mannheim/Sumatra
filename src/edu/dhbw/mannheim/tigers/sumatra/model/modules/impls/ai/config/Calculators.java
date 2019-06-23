/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 03.02.2011
 * Author(s): Oliver Steinbrecher <OST1988@aol.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config;

import org.apache.commons.configuration.XMLConfiguration;


/**
 * Configuration object for calculators in metis.
 * 
 * @author Oliver Steinbrecher <OST1988@aol.com>
 * 
 */
public class Calculators
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final String	nodePath	= "calculators.";
	
	// ball possession
	private final float		minDistToBot;
	
	// defense goal points
	private final int			numberOfThreadPoints;
	private final float		directShootQuantifier;
	private final float		indirectShootQuantifier;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public Calculators(XMLConfiguration configFile)
	{
		minDistToBot = configFile.getInt(nodePath + "ballPossession.minDistanceToBot");
		
		numberOfThreadPoints = configFile.getInt(nodePath + "defenseGoalPoints.numberOfThreadPoints");
		directShootQuantifier = configFile.getFloat(nodePath + "defenseGoalPoints.directShootQuantifier");
		indirectShootQuantifier = configFile.getFloat(nodePath + "defenseGoalPoints.indirectShootQuantifier");
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @return the minDistToBot
	 */
	public float getMinDistToBot()
	{
		return minDistToBot;
	}
	

	/**
	 * @return the numberOfThreadPoints
	 */
	public int getNumberOfThreadPoints()
	{
		return numberOfThreadPoints;
	}
	

	/**
	 * @return the directShootQuantifier
	 */
	public float getDirectShootQuantifier()
	{
		return directShootQuantifier;
	}
	

	/**
	 * @return the indirectShootQuantifier
	 */
	public float getIndirectShootQuantifier()
	{
		return indirectShootQuantifier;
	}
	
}
