/* 
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 11.02.2011
 * Author(s): König
 *
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config;

import org.apache.commons.configuration.XMLConfiguration;

/**
 * Configuration object for extended rapidly-exploring random trees (errt, pathplaning).
 * 
 * @author Christian
 * 
 */
public class Errt
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final String		nodePath	= "pathplanning.errt.";
	
	/** distance between 2 nodes */
	private final float stepSize;
	/** step size of the final path */
	private final float finalStepSize;
	/** defines how much iterations will at most be created */
	private final int	maxIterations;
	/** defines how much clearance the path has to have to obstacles [mm] */
	private final float safetyDistance;
	/** defines how much clearance the path has to have to obstacles when checking old path[mm] */
	private final float safetyDistanceOldPath;
	/** distance, bots have to keep away from obstacles when in second round */
	private final float safetyDistance2Try;
	/** possibility to choose targetNode as next goal */
	private final float pDestination;
	/** possibility to choose a waypoint as next goal */
	private final float pWaypoint;
	/** how much target can differ from target of last cycle, so that oldPath still is checked */
	private final float tollerableTargetShift;
	/** how much target can differ from target of last cycle to use WPC */
	private final float tollerableTargetShiftWPC;
	/** size of waypointcache */
	private final int sizeWaypointCache;

	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public Errt(XMLConfiguration configFile)
	{
		stepSize = configFile.getFloat(nodePath + "stepSize");
		finalStepSize = configFile.getFloat(nodePath + "finalStepSize");
//		targetSqDiscrepancy = configFile.getInt(nodePath + "targetSqDiscrepancyDefault");
		maxIterations = configFile.getInt(nodePath + "maxIterations");
		safetyDistance = configFile.getInt(nodePath + "safetyDistance");
		safetyDistanceOldPath = configFile.getInt(nodePath + "safetyDistanceOldPath");
		safetyDistance2Try = configFile.getInt(nodePath + "safetyDistance2Try");
		pDestination = configFile.getFloat(nodePath + "pDestination");
		pWaypoint = configFile.getFloat(nodePath + "pWaypoint");	
		tollerableTargetShift = configFile.getFloat(nodePath + "tollerableTargetShift");
		tollerableTargetShiftWPC = configFile.getFloat(nodePath + "tollerableTargetShiftWPC");
		sizeWaypointCache = configFile.getInt(nodePath +  "tollerableTargetShift");
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	public float getStepSize()
	{
		return stepSize;
	}
	
	public float getFinalStepSize()
	{
		return finalStepSize;
	}

	public int getMaxIterations()
	{
		return maxIterations;
	}

	public float getSafetyDistance()
	{
		return safetyDistance;
	}
	
	public float getSafetyDistanceOldPath()
	{
		return safetyDistanceOldPath;
	}
	
	public float getSafetyDistance2Try()
	{
		return safetyDistance2Try;
	}
	
	public float getpDestination()
	{
		return pDestination;
	}


	public float getpWaypoint()
	{
		return pWaypoint;
	}

	public int getWPCSize()
	{
		return sizeWaypointCache;
	}

	public float getTollerableTargetShift()
	{
		return tollerableTargetShift;
	}
	
	public float getTollerableTargetShiftWPC()
	{
		return tollerableTargetShiftWPC;
	}
}
