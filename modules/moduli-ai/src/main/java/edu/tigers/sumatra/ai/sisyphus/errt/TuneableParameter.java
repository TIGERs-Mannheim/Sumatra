/*
 * *********************************************************
 * Copyright (c) 2009 - 2012, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 15, 2012
 * Author(s): dirk
 * *********************************************************
 */
package edu.tigers.sumatra.ai.sisyphus.errt;

import org.apache.log4j.Logger;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.sisyphus.analyze.ETuneableParameter;


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
	private double						pDestination						= 0.2;
	@Configurable(comment = "probability to choose a waypoint, BE CAREFUL: pRandom = (1 - pDest - pWaypoint)", defValue = "0.6")
	private double						pWaypoint							= 0.6;
																					
	// Step sizes
	@Configurable(comment = "distance between 2 nodes [mm]", defValue = "100")
	private double						stepSize								= 100;
																					
	@Configurable(comment = "maximum amount of iterations", defValue = "1000")
	private int							maxIterations						= 1000;
																					
	@Configurable(comment = "maximum amount of iterations for fast approximation", defValue = "100")
	private int							maxIterationsFastApprox			= 100;
																					
	@Configurable(comment = "the safety distance for the path smoothing is reduced to this percentage", defValue = "0.2f")
	private double						reduceSafetyForPathSmoothing	= 0.2;
																					
	private boolean					fastApprox							= false;
																					
	// TODO DanielAl switch with class instance to allow more than two implementaions
	@Configurable(comment = "Use SimpleTree or KDTree as Datacontainer for Pathplanning ")
	private static boolean			useKDTree							= false;
																					
																					
	static
	{
		ConfigRegistration.registerClass("sisyphus", TuneableParameter.class);
	}
	
	
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
		stepSize = 1000;
		maxIterations = maxIterationsFastApprox;
	}
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 */
	public TuneableParameter()
	{
		ConfigRegistration.applySpezis(this, "sisyphus", "");
	}
	
	
	/**
	 * @param pGoal
	 * @param pWaypoint
	 * @param stepSize
	 * @param maxIterations
	 */
	public TuneableParameter(final double pGoal, final double pWaypoint, final double stepSize, final int maxIterations)
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
	public double get(final ETuneableParameter param)
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
				return 0;
		}
	}
	
	
	/**
	 * @param param
	 * @param value
	 */
	public void set(final ETuneableParameter param, final double value)
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
	public double getpGoal()
	{
		return pDestination;
	}
	
	
	/**
	 * @param pGoal
	 */
	public void setpGoal(final double pGoal)
	{
		pDestination = pGoal;
	}
	
	
	/**
	 * @return
	 */
	public double getpWaypoint()
	{
		return pWaypoint;
	}
	
	
	/**
	 * @param pWaypoint
	 */
	public void setpWaypoint(final double pWaypoint)
	{
		this.pWaypoint = pWaypoint;
	}
	
	
	/**
	 * @return
	 */
	public double getStepSize()
	{
		return stepSize;
	}
	
	
	/**
	 * @param stepSize
	 */
	public void setStepSize(final double stepSize)
	{
		this.stepSize = stepSize;
	}
	
	
	/**
	 * @return
	 */
	public double getMaxIterations()
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
	 * @return
	 */
	public double getReduceSafetyForPathSmoothing()
	{
		return reduceSafetyForPathSmoothing;
	}
	
	
	/**
	 * @return the useKDTree
	 */
	public boolean isUseKDTree()
	{
		return useKDTree;
	}
	
	
	/**
	 * @param useKDTree the useKDTree to set
	 */
	public void setUseKDTree(final boolean useKDTree)
	{
		TuneableParameter.useKDTree = useKDTree;
	}
	
	
}
