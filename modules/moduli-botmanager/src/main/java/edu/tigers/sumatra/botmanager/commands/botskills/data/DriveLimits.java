/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - Tigers Mannheim
 */
package edu.tigers.sumatra.botmanager.commands.botskills.data;

/**
 * Velocity, acceleration, jerk drive limits for XY and W.
 * 
 * @author AndreR <andre@ryll.cc>
 */
public class DriveLimits
{
	// These limits have counterparts in the robot firmware!
	// DO NOT CHANGE!
	public static final int	MAX_VEL		= 5;
	public static final int	MAX_VEL_W	= 30;
	public static final int	MAX_ACC		= 10;
	public static final int	MAX_ACC_W	= 100;
	public static final int	MAX_JERK		= 100;
	public static final int	MAX_JERK_W	= 1000;
	
	
	private DriveLimits()
	{
	}
	
	
	/**
	 * Convert double to int with scaling.
	 * 
	 * @param value
	 * @param max
	 * @return
	 */
	public static int toUInt8(final double value, final double max)
	{
		return (int) ((value / max) * 255);
	}
	
	
	/**
	 * Convert int to double and unfold scaling.
	 * 
	 * @param value
	 * @param max
	 * @return
	 */
	public static double toDouble(final int value, final double max)
	{
		return (value / 255.0) * max;
	}
}