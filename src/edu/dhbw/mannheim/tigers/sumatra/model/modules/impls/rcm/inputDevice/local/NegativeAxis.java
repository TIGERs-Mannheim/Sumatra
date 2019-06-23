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
 * only be the negative part of Component.Axis.
 * 
 * @author Lukas
 * 
 */

public class NegativeAxis implements Component
{
	
	// --------------------------------------------------------------------------
	// --- instance variables ---------------------------------------------------
	// --------------------------------------------------------------------------
	Component	axis	= null;
	
	
	// --------------------------------------------------------------------------
	// --- constructor(s) -------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param comp
	 */
	public NegativeAxis(Component comp)
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
		// Identifier.Button-Constructor chosen, because Identifier.Axis-Constructor is not
		// visible... shouldn't have any consequences
		return new Identifier.Button("-" + axis.getIdentifier().toString());
	}
	
	
	@Override
	public String getName()
	{
		return "-" + axis.getName();
	}
	
	
	@Override
	public float getPollData()
	{
		float poll = axis.getPollData();
		if (poll < 0.0f)
		{
			poll = (-1f) * (axis.getPollData());
			return poll;
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
