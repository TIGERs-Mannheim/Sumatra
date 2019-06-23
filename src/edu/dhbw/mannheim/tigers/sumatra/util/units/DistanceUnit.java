/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 12, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.util.units;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;


/**
 * 
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public enum DistanceUnit
{
	/**  */
	METERS(1f),
	
	/**  */
	MILLIMETERS(0.001f);
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final int	METER_TO_MILLIMETER	= 1000;
	private final float			meters;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * 
	 */
	private DistanceUnit(float meters)
	{
		this.meters = meters;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param value
	 * @return
	 */
	public float toMeters(float value)
	{
		return meters * value;
	}
	
	
	/**
	 * @param value
	 * @return
	 */
	public float toMillimeters(float value)
	{
		return (meters * value) * METER_TO_MILLIMETER;
	}
	
	
	/**
	 * @param value
	 * @return
	 */
	public IVector2 toMeters(IVector2 value)
	{
		return value.multiplyNew(meters);
	}
	
	
	/**
	 * @param value
	 * @return
	 */
	public IVector2 toMillimeters(IVector2 value)
	{
		return value.multiplyNew(meters * METER_TO_MILLIMETER);
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @return the meters
	 */
	public final float getMeters()
	{
		return meters;
	}
}
