/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - RCU
 * Date: 20.10.2010
 * Author(s): Lukas
 * 
 * *********************************************************
 */

package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm.inputDevice.local;

import net.java.games.input.Component;


/**
 * This class contains a Component.Axis and modifies its behavior as if this Axis would
 * only be the positive part of Component.Axis.
 * 
 * @author Lukas
 * 
 */
public class PositiveAxis implements Component
{
	// --------------------------------------------------------------------------
	// --- instance variables ---------------------------------------------------
	// --------------------------------------------------------------------------
	private Component	axis	= null;
	
	
	// --------------------------------------------------------------------------
	// --- constructor(s) -------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param comp
	 */
	public PositiveAxis(Component comp)
	{
		axis = comp;
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
		if (poll > 0)
		{
			return axis.getPollData();
		}
		return 0f;
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
