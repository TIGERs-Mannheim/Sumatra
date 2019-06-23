/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 11.02.2011
 * Author(s): K�nig
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config;

import org.apache.commons.configuration.Configuration;


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
	private static final String	NODE_PATH	= "pathplanning.errt.";
	
	/** distance between 2 nodes */
	private final float				stepSize;
	/** defines how much iterations will at most be created */
	private final int					maxIterations;
	/** defines how much clearance the path has to have to obstacles [mm] */
	private final float				safetyDistance;
	/** defines how much clearance the path has to have to obstacles when checking old path[mm] */
	private final float				safetyDistanceOldPath;
	/** distance, bots have to keep away from obstacles when in second round */
	private final float				safetyDistance2Try;
	/** distance, bots have to keep away from the ball, needs to be bigger than to bots */
	private final float				safetyDistanceBall;
	/** possibility to choose targetNode as next goal */
	private final float				pDestination;
	/** possibility to choose a waypoint as next goal */
	private final float				pWaypoint;
	/** how much target can differ from target of last cycle, so that oldPath still is checked */
	private final float				tollerableTargetShift;
	/** how much target can differ from target of last cycle to use WPC */
	private final float				tollerableTargetShiftWPC;
	/** size of waypointcache */
	private final int					sizeWaypointCache;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param configFile
	 */
	public Errt(Configuration configFile)
	{
		stepSize = configFile.getFloat(NODE_PATH + "stepSize");
		maxIterations = configFile.getInt(NODE_PATH + "maxIterations");
		safetyDistance = configFile.getFloat(NODE_PATH + "safetyDistance");
		safetyDistanceOldPath = configFile.getFloat(NODE_PATH + "safetyDistanceOldPath");
		safetyDistance2Try = configFile.getFloat(NODE_PATH + "safetyDistance2Try");
		safetyDistanceBall = configFile.getFloat(NODE_PATH + "safetyDistanceBall");
		pDestination = configFile.getFloat(NODE_PATH + "pDestination");
		pWaypoint = configFile.getFloat(NODE_PATH + "pWaypoint");
		tollerableTargetShift = configFile.getFloat(NODE_PATH + "tollerableTargetShift");
		tollerableTargetShiftWPC = configFile.getFloat(NODE_PATH + "tollerableTargetShiftWPC");
		sizeWaypointCache = configFile.getInt(NODE_PATH + "sizeWaypointCache");
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return
	 */
	public float getStepSize()
	{
		return stepSize;
	}
	
	
	/**
	 * @return
	 */
	public int getMaxIterations()
	{
		return maxIterations;
	}
	
	
	/**
	 * @return
	 */
	public float getSafetyDistance()
	{
		return safetyDistance;
	}
	
	
	/**
	 * @return
	 */
	public float getSafetyDistanceOldPath()
	{
		return safetyDistanceOldPath;
	}
	
	
	/**
	 * @return
	 */
	public float getSafetyDistance2Try()
	{
		return safetyDistance2Try;
	}
	
	
	/**
	 * @return
	 */
	public float getpDestination()
	{
		return pDestination;
	}
	
	
	/**
	 * @return
	 */
	public float getpWaypoint()
	{
		return pWaypoint;
	}
	
	
	/**
	 * @return
	 */
	public int getSizeWaypointCache()
	{
		return sizeWaypointCache;
	}
	
	
	/**
	 * @return
	 */
	public float getTollerableTargetShift()
	{
		return tollerableTargetShift;
	}
	
	
	/**
	 * @return
	 */
	public float getTollerableTargetShiftWPC()
	{
		return tollerableTargetShiftWPC;
	}
	
	
	/**
	 * @return the safetyDistanceBall
	 */
	public float getSafetyDistanceBall()
	{
		return safetyDistanceBall;
	}
	
}
