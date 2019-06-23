/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 16.11.2010
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.CommandConstants;


/**
 * Set maximum auto-load cap level.
 * 
 * @author AndreR
 * 
 */
public class TigerKickerChargeAuto extends ACommand
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger	log			= Logger.getLogger(TigerKickerChargeAuto.class.getName());
	
	private int							max;
	/** */
	public static final int			MAX_LEVEL	= 180;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 */
	public TigerKickerChargeAuto()
	{
	}
	
	
	/**
	 * 
	 * @param max
	 */
	public TigerKickerChargeAuto(int max)
	{
		setMax(max);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void setData(byte[] data)
	{
		max = byteArray2UShort(data, 0);
	}
	
	
	@Override
	public byte[] getData()
	{
		final byte[] data = new byte[getDataLength()];
		
		short2ByteArray(data, 0, max);
		
		return data;
	}
	
	
	@Override
	public int getCommand()
	{
		return CommandConstants.CMD_KICKER_CHARGE_AUTO;
	}
	
	
	@Override
	public int getDataLength()
	{
		return 2;
	}
	
	
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
	public void setMax(int max)
	{
		this.max = max * 10;
		
		if (max > 350)
		{
			log.warn("Level above " + MAX_LEVEL + ", cut off");
			this.max = 350 * 10;
		}
	}
}
