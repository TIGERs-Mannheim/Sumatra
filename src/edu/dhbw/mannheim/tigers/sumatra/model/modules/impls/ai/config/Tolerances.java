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

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;


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
	private static final String	NODE_PATH	= "tolerances.";
	
	/** [mm] */
	private final float				positioning;
	/** [rad] */
	private final float				viewAngle;
	/** [rad] */
	private final float				aiming;
	
	private final float				nearBall;
	/** [mm] */
	private final float				nextToBall;
	/** [mm] */
	private final float				destEqualRadius;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 * @param configFile
	 */
	public Tolerances(Configuration configFile)
	{
		positioning = configFile.getFloat(NODE_PATH + "positioning");
		viewAngle = getAngle(configFile, NODE_PATH + "viewAngle");
		aiming = getAngle(configFile, NODE_PATH + "aiming");
		nearBall = configFile.getFloat(NODE_PATH + "nearBall");
		nextToBall = configFile.getFloat(NODE_PATH + "nextToBall");
		destEqualRadius = configFile.getFloat(NODE_PATH + "destEqualRadius");
	}
	
	
	/**
	 * 
	 * @param configFile
	 * @param base
	 */
	public Tolerances(Configuration configFile, final Tolerances base)
	{
		positioning = configFile.getFloat(NODE_PATH + "positioning", base.positioning);
		viewAngle = getAngle(configFile, NODE_PATH + "viewAngle", base.viewAngle);
		aiming = getAngle(configFile, NODE_PATH + "aiming", base.aiming);
		nearBall = configFile.getFloat(NODE_PATH + "nearBall", base.nearBall);
		nextToBall = configFile.getFloat(NODE_PATH + "nextToBall", base.nextToBall);
		destEqualRadius = configFile.getFloat(NODE_PATH + "destEqualRadius", base.destEqualRadius);
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param configFile
	 * @param value
	 * @return angle [rad]
	 */
	private float getAngle(Configuration configFile, String value, float defaultValue)
	{
		return AngleMath.deg2rad(configFile.getFloat(value, AngleMath.rad2deg(defaultValue)));
	}
	
	
	/**
	 * @param configFile
	 * @param value
	 * @return angle [rad]
	 */
	private float getAngle(Configuration configFile, String value)
	{
		return AngleMath.deg2rad(configFile.getFloat(value));
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
	
	
	/**
	 * Tolerance radius between the center of two bots, up to which the destination
	 * of the two bots is considered to be equal
	 * 
	 * @return the destEqualRadius [mm]
	 */
	public float getDestEqualRadius()
	{
		return destEqualRadius;
	}
}
