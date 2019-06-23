/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 20.06.2013
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.basestation;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ECommand;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData.ESerialDataType;


/**
 * Configure vision rate and field coordinate inversion.
 * 
 * @author AndreR
 * 
 */
public class BaseStationConfig extends ACommand
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	@SerialData(type = ESerialDataType.UINT8)
	private int							channel				= 100;
	@SerialData(type = ESerialDataType.UINT8)
	private int							positionInverted	= 0;
	@SerialData(type = ESerialDataType.UINT8)
	private int							maxBots				= 0;
	@SerialData(type = ESerialDataType.UINT16)
	private int							visionRate			= 30;
	@SerialData(type = ESerialDataType.UINT16)
	private int							timeout				= 1000;
	
	private static final int		MAX_CHANNEL			= 127;
	
	private static final Logger	log					= Logger.getLogger(BaseStationConfig.class.getName());
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/** */
	public BaseStationConfig()
	{
		super(ECommand.CMD_BASE_CONFIG);
	}
	
	
	/**
	 * 
	 * @param invertPos
	 * @param visionRate
	 * @param maxBots
	 * @param channel
	 * @param timeout
	 */
	public BaseStationConfig(int channel, boolean invertPos, int maxBots, int visionRate, int timeout)
	{
		super(ECommand.CMD_BASE_CONFIG);
		
		positionInverted = invertPos ? 1 : 0;
		this.visionRate = visionRate;
		this.maxBots = maxBots;
		setChannel(channel);
		this.timeout = timeout;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return the positionInverted
	 */
	public boolean isPositionInverted()
	{
		return positionInverted == 1;
	}
	
	
	/**
	 * @param positionInverted the positionInverted to set
	 */
	public void setPositionInverted(boolean positionInverted)
	{
		this.positionInverted = positionInverted ? 1 : 0;
	}
	
	
	/**
	 * @return the visionRate
	 */
	public int getVisionRate()
	{
		return visionRate;
	}
	
	
	/**
	 * @param visionRate the visionRate to set
	 */
	public void setVisionRate(int visionRate)
	{
		this.visionRate = visionRate;
	}
	
	
	/**
	 * @return the channel
	 */
	public int getChannel()
	{
		return channel;
	}
	
	
	/**
	 * @param channel the channel to set
	 */
	public void setChannel(int channel)
	{
		if (channel > MAX_CHANNEL)
		{
			this.channel = MAX_CHANNEL;
			log.error("Invalid channel: " + channel + ", max is: " + MAX_CHANNEL);
		}
		
		this.channel = channel;
	}
	
	
	/**
	 * @return the maxBots
	 */
	public int getMaxBots()
	{
		return maxBots;
	}
	
	
	/**
	 * @param maxBots the maxBots to set
	 */
	public void setMaxBots(int maxBots)
	{
		this.maxBots = maxBots;
	}
	
	
	/**
	 * @return the timeout
	 */
	public int getTimeout()
	{
		return timeout;
	}
	
	
	/**
	 * @param timeout the timeout to set
	 */
	public void setTimeout(int timeout)
	{
		this.timeout = timeout;
	}
}
