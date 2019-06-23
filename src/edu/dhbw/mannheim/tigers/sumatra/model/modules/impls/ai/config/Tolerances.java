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

import org.apache.commons.configuration.XMLConfiguration;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath;


/**
 * Configuration object for tolerance values.
 * 
 * @author Oliver Steinbrecher <OST1988@aol.com>
 * 
 */
public class Tolerances
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final String	nodePath	= "tolerances.";
	
	/** [mm] */
	private final float	positioning;
	/** [rad] */
	private final float	viewAngle;
	/** [rad] */
	private final float	aiming;
	
	private final float	nearBall;
	/** [mm] */
	private final float	nextToBall;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public Tolerances(XMLConfiguration configFile)
	{
		positioning = configFile.getFloat(nodePath + "positioning");
		viewAngle = getAngle(configFile, nodePath + "viewAngle");
		aiming = getAngle(configFile, nodePath + "aiming");
		nearBall = configFile.getFloat(nodePath + "nearBall");
		nextToBall = configFile.getFloat(nodePath + "nextToBall");
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param configFile
	 * @param value
	 * @return angle [rad]
	 */
	private float getAngle(XMLConfiguration configFile, String value)
	{
		return AIMath.deg2rad(configFile.getFloat(value));
	}
	

	/**
	 * @return {@link #positioning}
	 */
	public float getPositioning()
	{
		return positioning;
	}
	

	/**
	 * @return {@link #viewAngle} [rad]
	 */
	public float getViewAngle()
	{
		return viewAngle;
	}
	

	/**
	 * @return {@link #aiming} [rad]
	 */
	public float getAiming()
	{
		return aiming;
	}
	

	/**
	 * @return the aimRadius
	 */
	public float getNearBall()
	{
		return nearBall;
	}
	

	/**
	 * @return {@link #nextToBall} [mm]
	 */
	public float getNextToBall()
	{
		return nextToBall;
	}
}
