/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 31.12.2014
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv3;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ECommand;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData.ESerialDataType;


/**
 * Set various kicker/barrier configuration values.
 * 
 * @author AndreR
 */
public class TigerKickerConfig extends ACommand
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/** [V] */
	@SerialData(type = ESerialDataType.UINT16)
	private int					maxCapLevel		= 150;
	
	/** [ms] */
	@SerialData(type = ESerialDataType.UINT16)
	private int					kickerCooldown	= 100;
	
	/** [mV] */
	@SerialData(type = ESerialDataType.UINT16)
	private int					irThreshold		= 20;
	
	/** undershot counter */
	@SerialData(type = ESerialDataType.UINT16)
	private int					irFilterLimit	= 1;
	
	/** */
	public static final int	MAX_LEVEL		= 180;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 */
	public TigerKickerConfig()
	{
		super(ECommand.CMD_KICKER_CONFIG, true);
	}
	
	
	/**
	 * @param max
	 * @param cooldown
	 * @param irThreshold
	 * @param irFilterLimit
	 */
	public TigerKickerConfig(final int max, final float cooldown, final float irThreshold, final int irFilterLimit)
	{
		super(ECommand.CMD_KICKER_CONFIG, true);
		
		setMaxCapLevel(max);
		setKickerCooldown(cooldown);
		setIrThreshold(irThreshold);
		setIrFilterLimit(irFilterLimit);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return the max
	 */
	public int getMaxCapLevel()
	{
		return maxCapLevel;
	}
	
	
	/**
	 * @param max the max to set
	 */
	public void setMaxCapLevel(final int max)
	{
		maxCapLevel = max;
		
		if (max > MAX_LEVEL)
		{
			maxCapLevel = MAX_LEVEL;
		}
	}
	
	
	/**
	 * @return the kickerCooldown
	 */
	public float getKickerCooldown()
	{
		return kickerCooldown * 0.001f;
	}
	
	
	/**
	 * @param kickerCooldown the kickerCooldown to set
	 */
	public void setKickerCooldown(final float kickerCooldown)
	{
		this.kickerCooldown = (int) (kickerCooldown * 1000.0f);
	}
	
	
	/**
	 * @return the irThreshold
	 */
	public float getIrThreshold()
	{
		return irThreshold * 0.001f;
	}
	
	
	/**
	 * @param irThreshold the irThreshold to set
	 */
	public void setIrThreshold(final float irThreshold)
	{
		this.irThreshold = (int) (irThreshold * 1000.0f);
	}
	
	
	/**
	 * @return the irFilterLimit
	 */
	public int getIrFilterLimit()
	{
		return irFilterLimit;
	}
	
	
	/**
	 * @param irFilterLimit the irFilterLimit to set
	 */
	public void setIrFilterLimit(final int irFilterLimit)
	{
		this.irFilterLimit = irFilterLimit;
	}
}
