/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 16.11.2010
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ECommand;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData.ESerialDataType;


/**
 * Set maximum auto-load cap level.
 * 
 * @author AndreR
 */
public class TigerKickerChargeAuto extends ACommand
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger	log			= Logger.getLogger(TigerKickerChargeAuto.class.getName());
	
	@SerialData(type = ESerialDataType.UINT16)
	private int							max;
	/** */
	public static final int			MAX_LEVEL	= 200;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 */
	public TigerKickerChargeAuto()
	{
		super(ECommand.CMD_KICKER_CHARGE_AUTO, true);
	}
	
	
	/**
	 * @param max
	 */
	public TigerKickerChargeAuto(final int max)
	{
		super(ECommand.CMD_KICKER_CHARGE_AUTO, true);
		setMax(max);
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
	public int getMax()
	{
		return max / 10;
	}
	
	
	/**
	 * @param max the max to set
	 */
	public void setMax(final int max)
	{
		this.max = max * 10;
		
		if (max > MAX_LEVEL)
		{
			log.warn("Level above " + MAX_LEVEL + ", cut off");
			this.max = MAX_LEVEL * 10;
		}
	}
}
