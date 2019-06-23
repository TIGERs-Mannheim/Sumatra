/*
 * *********************************************************
 * Copyright (c) 2009 - 2012, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 15, 2012
 * Author(s): dirk
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.analyze;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;


/**
 * a data class containing the parameters, which should be tested
 * 
 * @author Dirkk
 * 
 */
public class TuneableParameter
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger	log							= Logger.getLogger(TuneableParameter.class.getName());
	
	// Parameters for changing the goal
	/** possibility to choose targetNode as next goal */
	private float						pGoal							= AIConfig.getErrt().getpDestination();
	/** possibility to choose a waypoint as next goal */
	private float						pWaypoint					= AIConfig.getErrt().getpWaypoint();
	
	// Step sizes
	/** distance between 2 nodes */
	private float						stepSize						= AIConfig.getErrt().getStepSize();
	
	// safety distance
	/** distance, bots have to keep away from obstacles */
	private float						safetyDistance				= AIConfig.getErrt().getSafetyDistance();
	/** distance, bots have to keep away from obstacles when checking old path */
	private float						secondSafetyDistance		= AIConfig.getErrt().getSafetyDistance2Try();
	/** distance, bots have to keep away from the ball, needs to be bigger than to bots */
	private float						safetyDistanceBall		= AIConfig.getErrt().getSafetyDistanceBall();
	
	/** defines how much iterations will at most be created */
	private int							maxIterations				= AIConfig.getErrt().getMaxIterations();
	
	// WPC
	/** size of waypointcache */
	private int							wpcSize						= AIConfig.getErrt().getSizeWaypointCache();
	/** how much target can differ from target of last cycle, so that oldPath still is checked */
	private float						tollerableTargetShift	= AIConfig.getErrt().getTollerableTargetShiftWPC();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 */
	public TuneableParameter()
	{
		
	}
	
	
	/**
	 * 
	 * @param pGoal
	 * @param pWaypoint
	 * @param stepSize
	 * @param safetyDistance
	 * @param secondSafetyDistance
	 * @param maxIterations
	 * @param wpcSize
	 * @param tollerableTargetShift
	 */
	public TuneableParameter(float pGoal, float pWaypoint, float stepSize, float safetyDistance,
			float secondSafetyDistance, int maxIterations, int wpcSize, float tollerableTargetShift)
	{
		this.pGoal = pGoal;
		this.pWaypoint = pWaypoint;
		
		this.stepSize = stepSize;
		
		this.safetyDistance = safetyDistance;
		this.secondSafetyDistance = secondSafetyDistance;
		
		this.maxIterations = maxIterations;
		
		this.wpcSize = wpcSize;
		this.tollerableTargetShift = tollerableTargetShift;
	}
	
	
	/**
	 * @param tp
	 */
	public TuneableParameter(TuneableParameter tp)
	{
		this(tp.pGoal, tp.pWaypoint, tp.stepSize, tp.safetyDistance, tp.secondSafetyDistance, tp.maxIterations,
				tp.wpcSize, tp.tollerableTargetShift);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	public String toString()
	{
		String output = "";
		output += pGoal + " " + pWaypoint + " " + stepSize + " " + safetyDistance + " " + secondSafetyDistance + " "
				+ maxIterations + " " + wpcSize + " " + tollerableTargetShift;
		return output;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 * @param param
	 * @return
	 */
	public float get(ETuneableParameter param)
	{
		switch (param)
		{
			case pGoal:
				return getpGoal();
			case stepSize:
				return getStepSize();
			case maxIterations:
				return getMaxIterations();
			default:
				log.warn("Please implement the getter for this enum type " + param.name());
				return 0f;
		}
	}
	
	
	/**
	 * 
	 * @param param
	 * @param value
	 */
	public void set(ETuneableParameter param, float value)
	{
		switch (param)
		{
			case pGoal:
				setpGoal(value);
			case stepSize:
				setStepSize(value);
			case maxIterations:
				setMaxIterations((int) value);
			case probabilities:
				setpGoal(value);
				setpWaypoint(value);
				break;
			default:
				log.warn("Please implement the setter for this enum type " + param.name());
		}
	}
	
	
	/**
	 * 
	 * @return
	 */
	public float getpGoal()
	{
		return pGoal;
	}
	
	
	/**
	 * 
	 * @param pGoal
	 */
	public void setpGoal(float pGoal)
	{
		this.pGoal = pGoal;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public float getpWaypoint()
	{
		return pWaypoint;
	}
	
	
	/**
	 * 
	 * @param pWaypoint
	 */
	public void setpWaypoint(float pWaypoint)
	{
		this.pWaypoint = pWaypoint;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public float getStepSize()
	{
		return stepSize;
	}
	
	
	/**
	 * 
	 * @param stepSize
	 */
	public void setStepSize(float stepSize)
	{
		this.stepSize = stepSize;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public float getSafetyDistance()
	{
		return safetyDistance;
	}
	
	
	/**
	 * 
	 * @param safetyDistance
	 */
	public void setSafetyDistance(float safetyDistance)
	{
		this.safetyDistance = safetyDistance;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public float getSecondSafetyDistance()
	{
		return secondSafetyDistance;
	}
	
	
	/**
	 * 
	 * @param secondSafetyDistance
	 */
	public void setSecondSafetyDistance(float secondSafetyDistance)
	{
		this.secondSafetyDistance = secondSafetyDistance;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public float getMaxIterations()
	{
		return maxIterations;
	}
	
	
	/**
	 * 
	 * @param maxIterations
	 */
	public void setMaxIterations(int maxIterations)
	{
		this.maxIterations = maxIterations;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public int getWpcSize()
	{
		return wpcSize;
	}
	
	
	/**
	 * 
	 * @param wpcSize
	 */
	public void setWpcSize(int wpcSize)
	{
		this.wpcSize = wpcSize;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public float getTollerableTargetShift()
	{
		return tollerableTargetShift;
	}
	
	
	/**
	 * 
	 * @param tollerableTargetShift
	 */
	public void setTollerableTargetShift(float tollerableTargetShift)
	{
		this.tollerableTargetShift = tollerableTargetShift;
	}
	
	
	/**
	 * @return the safetyDistanceBall
	 */
	public float getSafetyDistanceBall()
	{
		return safetyDistanceBall;
	}
	
	
	/**
	 * @param safetyDistanceBall the safetyDistanceBall to set
	 */
	public void setSafetyDistanceBall(float safetyDistanceBall)
	{
		this.safetyDistanceBall = safetyDistanceBall;
	}
	
	
}
