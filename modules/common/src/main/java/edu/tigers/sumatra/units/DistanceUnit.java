/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 12, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.units;

import edu.tigers.sumatra.math.IVector2;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
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
	private final double			meters;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * 
	 */
	private DistanceUnit(final double meters)
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
	public double toMeters(final double value)
	{
		return meters * value;
	}
	
	
	/**
	 * @param value
	 * @return
	 */
	public double toMillimeters(final double value)
	{
		return (meters * value) * METER_TO_MILLIMETER;
	}
	
	
	/**
	 * @param value
	 * @return
	 */
	public IVector2 toMeters(final IVector2 value)
	{
		return value.multiplyNew(meters);
	}
	
	
	/**
	 * @param value
	 * @return
	 */
	public IVector2 toMillimeters(final IVector2 value)
	{
		return value.multiplyNew(meters * METER_TO_MILLIMETER);
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @return the meters
	 */
	public final double getMeters()
	{
		return meters;
	}
}
