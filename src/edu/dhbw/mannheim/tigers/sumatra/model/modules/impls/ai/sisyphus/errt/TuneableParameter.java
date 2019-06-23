/*
 * *********************************************************
 * Copyright (c) 2009 - 2012, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 15, 2012
 * Author(s): dirk
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.errt;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.analyze.ETuneableParameter;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * a data class containing the parameters, which should be tested
 * 
 * @author Dirkk
 */
public class TuneableParameter
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger	log									= Logger.getLogger(TuneableParameter.class.getName());
	
	// Parameters for changing the goal
	@Configurable(comment = "probability to choose targetNode, BE CAREFUL: pRandom = (1 - pDest - pWaypoint)", defValue = "0.2")
	private float						pDestination						= 0.2f;
	@Configurable(comment = "probability to choose a waypoint, BE CAREFUL: pRandom = (1 - pDest - pWaypoint)", defValue = "0.6")
	private float						pWaypoint							= 0.6f;
	
	// Step sizes
	@Configurable(comment = "distance between 2 nodes [mm]", defValue = "100")
	private float						stepSize								= 100;
	
	@Configurable(comment = "maximum amount of iterations", defValue = "1000")
	private int							maxIterations						= 1000;
	
	@Configurable(comment = "maximum amount of iterations for fast approximation", defValue = "100")
	private int							maxIterationsFastApprox			= 100;
	
	@Configurable(comment = "the safety distance for the path smoothing is reduced to this percentage", defValue = "0.2f")
	private float						reduceSafetyForPathSmoothing	= 0.2f;
	
	private boolean					fastApprox							= false;
	
	
	/**
	 * @return the fastApprox
	 */
	public boolean isFastApprox()
	{
		return fastApprox;
	}
	
	
	/**
	 * @param fastApprox the fastApprox to set
	 */
	public void setFastApprox(final boolean fastApprox)
	{
		this.fastApprox = fastApprox;
	}
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 */
	public TuneableParameter()
	{
		AIConfig.getSisyphusClient().applyConfigToObject(this, "");
	}
	
	
	/**
	 * creates path planning parameters which are not so accurate but enable a faster path planning
	 * 
	 * @return
	 */
	public static TuneableParameter getParamsForApproximation()
	{
		TuneableParameter tune = new TuneableParameter();
		tune.fastApprox = true;
		tune.stepSize = 1000;
		tune.maxIterations = tune.maxIterationsFastApprox;
		return tune;
	}
	
	
	/**
	 * @param pGoal
	 * @param pWaypoint
	 * @param stepSize
	 * @param maxIterations
	 */
	public TuneableParameter(final float pGoal, final float pWaypoint, final float stepSize, final int maxIterations)
	{
		pDestination = pGoal;
		this.pWaypoint = pWaypoint;
		
		this.stepSize = stepSize;
		
		this.maxIterations = maxIterations;
		
	}
	
	
	/**
	 * @param tp
	 */
	public TuneableParameter(final TuneableParameter tp)
	{
		this(tp.pDestination, tp.pWaypoint, tp.stepSize, tp.maxIterations);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	public String toString()
	{
		String output = "";
		output += pDestination + " " + pWaypoint + " " + stepSize + " " + maxIterations;
		return output;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param param
	 * @return
	 */
	public float get(final ETuneableParameter param)
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
	 * @param param
	 * @param value
	 */
	public void set(final ETuneableParameter param, final float value)
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
	 * @return
	 */
	public float getpGoal()
	{
		return pDestination;
	}
	
	
	/**
	 * @param pGoal
	 */
	public void setpGoal(final float pGoal)
	{
		pDestination = pGoal;
	}
	
	
	/**
	 * @return
	 */
	public float getpWaypoint()
	{
		return pWaypoint;
	}
	
	
	/**
	 * @param pWaypoint
	 */
	public void setpWaypoint(final float pWaypoint)
	{
		this.pWaypoint = pWaypoint;
	}
	
	
	/**
	 * @return
	 */
	public float getStepSize()
	{
		return stepSize;
	}
	
	
	/**
	 * @param stepSize
	 */
	public void setStepSize(final float stepSize)
	{
		this.stepSize = stepSize;
	}
	
	
	/**
	 * @return
	 */
	public float getMaxIterations()
	{
		return maxIterations;
	}
	
	
	/**
	 * @param maxIterations
	 */
	public void setMaxIterations(final int maxIterations)
	{
		this.maxIterations = maxIterations;
	}
	
	
	/**
	 * TODO dirk, add comment!
	 * 
	 * @return
	 */
	public float getReduceSafetyForPathSmoothing()
	{
		return reduceSafetyForPathSmoothing;
	}
	
	
}
