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

import org.apache.commons.configuration.Configuration;


/**
 * Configuration object for calculators in metis.
 * 
 * @author Oliver Steinbrecher <OST1988@aol.com>
 * 
 */
public class MetisCalculators
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final String	NODE_PATH	= "calculators.";
	
	// ball possession
	private final float				minDistToBot;
	
	// defense goal points
	private int							numberOfThreatPoints;
	private final float				directShootQuantifier;
	private final float				indirectShootQuantifier;
	private float						indirectShootDistance;
	private float						scalingFactorDefense;
	// pattern detection
	private final String				patterLogFile;
	
	private float						ballQuantifer;
	
	private float						toleranceGoal;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param configFile
	 */
	public MetisCalculators(Configuration configFile)
	{
		minDistToBot = configFile.getInt(NODE_PATH + "ballPossession.minDistanceToBot");
		
		numberOfThreatPoints = configFile.getInt(NODE_PATH + "defenseGoalPoints.numberOfThreadPoints");
		directShootQuantifier = configFile.getFloat(NODE_PATH + "defenseGoalPoints.directShootQuantifier");
		indirectShootQuantifier = configFile.getFloat(NODE_PATH + "defenseGoalPoints.indirectShootQuantifier");
		indirectShootDistance = configFile.getFloat(NODE_PATH + "defenseGoalPoints.indirectShootDistance");
		ballQuantifer = configFile.getFloat(NODE_PATH + "defenseGoalPoints.ballQuantifier");
		scalingFactorDefense = configFile.getFloat(NODE_PATH + "defenseGoalPoints.scalingFactorDefense");
		patterLogFile = configFile.getString(NODE_PATH + "patterdetection.logfile");
		toleranceGoal = configFile.getFloat(NODE_PATH + "defenseGoalPoints.toleranceGoal");
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
	public int getNumberOfThreatPoints()
	{
		return numberOfThreatPoints;
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
	
	
	/**
	 * @return the patterLogFile
	 */
	public String getPatterLogFile()
	{
		return patterLogFile;
	}
	
	
	/**
	 * 
	 * @return the indirectshootDistance
	 */
	public float getIndirectShootDistance()
	{
		return indirectShootDistance;
	}
	
	
	/**
	 * 
	 * @return ballQuantifier
	 */
	public float getBallQuantifier()
	{
		return ballQuantifer;
	}
	
	
	/**
	 * Get the factor, the defensePoints while calculated in front of the defenseARea
	 * 
	 * @return
	 */
	public float getScalingFactorDefense()
	{
		return scalingFactorDefense;
	}
	
	
	/**
	 * 
	 * @return toleranceGoal
	 */
	public float getToleranceGoal()
	{
		return toleranceGoal;
	}
	
}
