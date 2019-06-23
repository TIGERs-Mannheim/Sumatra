/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 20.01.2011
 * Author(s): Oliver Steinbrecher <OST1988@aol.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config;

import org.apache.commons.configuration.Configuration;


/**
 * Configuration object for general parameters.
 * 
 * @author Oliver Steinbrecher <OST1988@aol.com>
 * 
 */
public class General
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final String	NODE_PATH	= "general.";
	
	/** [mm] */
	private final float				maxBreakingDist;
	
	private final float				breakCurve;
	
	private final float				breakEndOfPath;
	
	private final int					pathplanningInterval;
	
	private final float				dribblingDistance;
	private final float				positioningPreAiming;
	private final float				positioningPostAiming;
	private final int					minFramesHaveBall;
	private final float				stepSizePullBackAngle;
	private final float				stepSizePullBack;
	private final float				ballDampFactor;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param configFile
	 * @param base
	 */
	public General(Configuration configFile, final General base)
	{
		maxBreakingDist = configFile.getFloat(NODE_PATH + "maxBreakingDist", base.maxBreakingDist);
		
		breakCurve = configFile.getFloat(NODE_PATH + "breakInCurve", base.breakCurve);
		breakEndOfPath = configFile.getFloat(NODE_PATH + "breakStrengthAtEndOfPath", base.breakEndOfPath);
		
		pathplanningInterval = configFile.getInt((NODE_PATH) + "pathPlanningInterval", base.pathplanningInterval);
		
		dribblingDistance = configFile.getFloat(NODE_PATH + "DRIBBLING_DISTANCE", base.dribblingDistance);
		positioningPreAiming = configFile.getFloat(NODE_PATH + "POSITIONING_PRE_AIMING", base.positioningPreAiming);
		positioningPostAiming = configFile.getFloat(NODE_PATH + "POSITIONING_POST_AIMING", base.positioningPostAiming);
		minFramesHaveBall = configFile.getInt(NODE_PATH + "MIN_FRAMES_HAVE_BALL", base.minFramesHaveBall);
		stepSizePullBack = configFile.getFloat(NODE_PATH + "STEP_SIZE_PULL_BACK", base.stepSizePullBack);
		stepSizePullBackAngle = configFile.getFloat(NODE_PATH + "STEP_SIZE_PULL_BACK_ANGLE", base.stepSizePullBackAngle);
		ballDampFactor = configFile.getFloat(NODE_PATH + "ballDampFactor", base.ballDampFactor);
	}
	
	
	/**
	 * @param configFile
	 */
	public General(Configuration configFile)
	{
		maxBreakingDist = configFile.getFloat(NODE_PATH + "maxBreakingDist");
		
		breakCurve = configFile.getFloat(NODE_PATH + "breakInCurve");
		breakEndOfPath = configFile.getFloat(NODE_PATH + "breakStrengthAtEndOfPath");
		
		pathplanningInterval = configFile.getInt((NODE_PATH) + "pathPlanningInterval");
		
		dribblingDistance = configFile.getFloat(NODE_PATH + "DRIBBLING_DISTANCE");
		positioningPreAiming = configFile.getFloat(NODE_PATH + "POSITIONING_PRE_AIMING");
		positioningPostAiming = configFile.getFloat(NODE_PATH + "POSITIONING_POST_AIMING");
		minFramesHaveBall = configFile.getInt(NODE_PATH + "MIN_FRAMES_HAVE_BALL");
		stepSizePullBack = configFile.getFloat(NODE_PATH + "STEP_SIZE_PULL_BACK");
		stepSizePullBackAngle = configFile.getFloat(NODE_PATH + "STEP_SIZE_PULL_BACK_ANGLE");
		ballDampFactor = configFile.getFloat(NODE_PATH + "ballDampFactor");
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return the maxBreakingDist [mm]
	 */
	public float getMaxBreakingDist()
	{
		return maxBreakingDist;
	}
	
	
	/**
	 * @return the break strength in a curve
	 */
	public float getBreakCurve()
	{
		return breakCurve;
	}
	
	
	/**
	 * @return the break strength at the end of the path
	 */
	public float getBreakEndOfPath()
	{
		return breakEndOfPath;
	}
	
	
	/**
	 * @return the pathplanningInterval
	 */
	public final int getPathplanningInterval()
	{
		return pathplanningInterval;
	}
	
	
	/**
	 * @return dribblingDistance
	 */
	public float getDribblingDistance()
	{
		return dribblingDistance;
	}
	
	
	/**
	 * @return the positioningPreAiming
	 */
	public float getPositioningPreAiming()
	{
		return positioningPreAiming;
	}
	
	
	/**
	 * @return the positioningPostAiming
	 */
	public float getPositioningPostAiming()
	{
		return positioningPostAiming;
	}
	
	
	/**
	 * @return the minFramesHaveBall
	 */
	public int getMinFramesHaveBall()
	{
		return minFramesHaveBall;
	}
	
	
	/**
	 * @return the stepSizePullBackAngle
	 */
	public float getStepSizePullBackAngle()
	{
		return stepSizePullBackAngle;
	}
	
	
	/**
	 * @return the stepSizePullBack
	 */
	public float getStepSizePullBack()
	{
		return stepSizePullBack;
	}
	
	
	/**
	 * @return the ballDampFactor
	 */
	public final float getBallDampFactor()
	{
		return ballDampFactor;
	}
	
	
}
