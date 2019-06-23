/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - RCU
 * Date: 20.10.2010
 * Author(s): Lukas
 * *********************************************************
 */

package edu.tigers.sumatra.rcm;

import java.util.HashMap;
import java.util.Map;

import edu.tigers.sumatra.math.SumatraMath;
import net.java.games.input.Component;


/**
 * This class contains a Component.POV modifies its behavior as if this POV would be one of 8 Buttons.
 * 
 * @author Lukas
 */
public class POVToButton implements Component
{
	// --------------------------------------------------------------------------
	// --- instance variables ---------------------------------------------------
	// --------------------------------------------------------------------------
	
	private Component							pov			= null;
	private double								value			= 0;
	private static Map<Integer, String>	val2IdMap	= new HashMap<Integer, String>();
	
	
	static
	{
		val2IdMap.put(1, "povNW");
		val2IdMap.put(2, "povN");
		val2IdMap.put(3, "povNE");
		val2IdMap.put(4, "povE");
		val2IdMap.put(5, "povSE");
		val2IdMap.put(6, "povS");
		val2IdMap.put(7, "povSW");
		val2IdMap.put(8, "povW");
	}
	
	
	// --------------------------------------------------------------------------
	// --- constructor(s) -------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param comp
	 * @param value
	 */
	public POVToButton(final Component comp, final double value)
	{
		pov = comp;
		this.value = value;
	}
	
	
	/**
	 * @param comp
	 * @param identifier
	 */
	public POVToButton(final Component comp, final String identifier)
	{
		pov = comp;
		value = 0;
		for (Map.Entry<Integer, String> entry : val2IdMap.entrySet())
		{
			if (entry.getValue().equals(identifier))
			{
				value = entry.getKey() / 8.0;
				break;
			}
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public float getDeadZone()
	{
		return 0;
	}
	
	
	@Override
	public Identifier getIdentifier()
	{
		if (pov.getIdentifier() == Component.Identifier.Axis.POV)
		{
			int id = (int) (value * 8);
			return new Identifier.Button(val2IdMap.get(id));
		}
		return pov.getIdentifier();
	}
	
	
	@Override
	public String getName()
	{
		if (pov.getIdentifier() == Component.Identifier.Axis.POV)
		{
			return getIdentifier().getName();
		}
		return null;
	}
	
	
	@Override
	public float getPollData()
	{
		if (pov.getIdentifier() == Component.Identifier.Axis.POV)
		{
			return (SumatraMath.isEqual(pov.getPollData(), value) ? 1 : 0);
		}
		return 0;
	}
	
	
	@Override
	public boolean isAnalog()
	{
		return false;
	}
	
	
	@Override
	public boolean isRelative()
	{
		if (pov.getIdentifier() == Component.Identifier.Axis.POV)
		{
			return pov.isRelative();
		}
		return false;
	}
	
}
