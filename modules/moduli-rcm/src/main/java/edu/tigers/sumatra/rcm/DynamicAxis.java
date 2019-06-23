/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - RCU
 * Date: 20.10.2010
 * Author(s): Lukas
 * *********************************************************
 */

package edu.tigers.sumatra.rcm;

import net.java.games.input.Component;


/**
 * This class contains a Component.Axis and modifies its behavior as if this Axis would
 * only be the positive part of Component.Axis.
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class DynamicAxis implements Component
{
	// --------------------------------------------------------------------------
	// --- instance variables ---------------------------------------------------
	// --------------------------------------------------------------------------
	private final Component	axis;
	private final double		min;
	private final double		max;
	
	
	// --------------------------------------------------------------------------
	// --- constructor(s) -------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param comp
	 * @param min
	 * @param max
	 */
	public DynamicAxis(final Component comp, final double min, final double max)
	{
		axis = comp;
		this.min = min;
		this.max = max;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public float getDeadZone()
	{
		return axis.getDeadZone();
	}
	
	
	@Override
	public Identifier getIdentifier()
	{
		return axis.getIdentifier();
	}
	
	
	@Override
	public String getName()
	{
		return axis.getName();
	}
	
	
	@Override
	public float getPollData()
	{
		final float poll = axis.getPollData();
		float poll2 = (float) ((poll - min) / (max - min));
		poll2 = Math.max(0, poll2);
		poll2 = Math.min(1, poll2);
		return poll2;
	}
	
	
	@Override
	public boolean isAnalog()
	{
		return axis.isAnalog();
	}
	
	
	@Override
	public boolean isRelative()
	{
		return axis.isRelative();
	}
}
