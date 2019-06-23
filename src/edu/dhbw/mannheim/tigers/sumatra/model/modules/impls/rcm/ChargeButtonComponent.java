/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Sep 2, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm;

import net.java.games.input.Component;
import edu.dhbw.mannheim.tigers.sumatra.util.clock.SumatraClock;


/**
 * Chargeable button. The button will increase the output data during specified charge time
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class ChargeButtonComponent implements Component
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final float	THRESHOLD			= 0.1f;
	private final Component		comp;
	private final float			chargeTime;
	private long					timeLastPressed	= 0;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @param comp
	 * @param chargeTime
	 */
	public ChargeButtonComponent(final Component comp, final float chargeTime)
	{
		this.comp = comp;
		this.chargeTime = chargeTime;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public float getDeadZone()
	{
		return comp.getDeadZone();
	}
	
	
	@Override
	public Identifier getIdentifier()
	{
		return comp.getIdentifier();
	}
	
	
	@Override
	public String getName()
	{
		return comp.getName();
	}
	
	
	@Override
	public float getPollData()
	{
		final float poll = comp.getPollData();
		if (chargeTime < 0.001)
		{
			return poll;
		}
		if (poll > THRESHOLD)
		{
			if (timeLastPressed == 0)
			{
				timeLastPressed = SumatraClock.nanoTime();
			}
			return 0;
		} else if (timeLastPressed != 0)
		{
			float timeDiff = (SumatraClock.nanoTime() - timeLastPressed) / 1e9f;
			timeDiff = Math.min(timeDiff, chargeTime);
			timeLastPressed = 0;
			return timeDiff / chargeTime;
		}
		timeLastPressed = 0;
		return 0;
	}
	
	
	@Override
	public boolean isAnalog()
	{
		return comp.isAnalog();
	}
	
	
	@Override
	public boolean isRelative()
	{
		return comp.isRelative();
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
