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

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.SumatraMath;


/**
 * This class contains a Component.POV modifies its behavior as if this POV would be one of 8 Buttons.
 * 
 * @author Lukas
 * 
 */
public class POVToButton implements Component
{
	// --------------------------------------------------------------------------
	// --- instance variables ---------------------------------------------------
	// --------------------------------------------------------------------------
	
	// Logger
	private static final Logger	log	= Logger.getLogger(POVToButton.class.getName());
	private Component					pov	= null;
	private float						value	= 0;
	
	
	// --------------------------------------------------------------------------
	// --- constructor(s) -------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param comp
	 * @param value
	 */
	public POVToButton(Component comp, float value)
	{
		pov = comp;
		this.value = value;
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
			if (SumatraMath.isEqual(value, 0.125f))
			{
				return new Identifier.Button("povNW");
			} else if (SumatraMath.isEqual(value, 0.25f))
			{
				return new Identifier.Button("povN");
			} else if (SumatraMath.isEqual(value, 0.375f))
			{
				return new Identifier.Button("povNE");
			} else if (SumatraMath.isEqual(value, 0.5f))
			{
				return new Identifier.Button("povE");
			} else if (SumatraMath.isEqual(value, 0.625f))
			{
				return new Identifier.Button("povSE");
			} else if (SumatraMath.isEqual(value, 0.75f))
			{
				return new Identifier.Button("povS");
			} else if (SumatraMath.isEqual(value, 0.875f))
			{
				return new Identifier.Button("povSW");
			} else if (SumatraMath.isEqual(value, 1.00f))
			{
				return new Identifier.Button("povW");
			} else
			{
				log.error("Invalid Value for POV (POV to Button)");
			}
		}
		return pov.getIdentifier();
	}
	
	
	@Override
	public String getName()
	{
		if (pov.getIdentifier() == Component.Identifier.Axis.POV)
		{
			if (SumatraMath.isEqual(value, 0.125f))
			{
				return "NorthWest";
			} else if (SumatraMath.isEqual(value, 0.25f))
			{
				return "North";
			} else if (SumatraMath.isEqual(value, 0.375f))
			{
				return "NorthEast";
			} else if (SumatraMath.isEqual(value, 0.5f))
			{
				return "East";
			} else if (SumatraMath.isEqual(value, 0.625f))
			{
				return "SouthEast";
			} else if (SumatraMath.isEqual(value, 0.75f))
			{
				return "South";
			} else if (SumatraMath.isEqual(value, 0.875f))
			{
				return "SouthWest";
			} else if (SumatraMath.isEqual(value, 1.00f))
			{
				return "West";
			} else
			{
				log.error("Invalid Value for POV (POV to Button)");
			}
		}
		return null;
	}
	
	
	@Override
	public float getPollData()
	{
		if (pov.getIdentifier() == Component.Identifier.Axis.POV)
		{
			return (SumatraMath.isEqual(pov.getPollData(), value) ? 1f : 0f);
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
