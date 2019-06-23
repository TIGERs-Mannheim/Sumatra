/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 14.12.2014
 * Author(s): AndreR
 * *********************************************************
 */
package edu.tigers.sumatra.botmanager.commands.basestation;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.botmanager.commands.ECommand;
import edu.tigers.sumatra.botmanager.serial.SerialData;
import edu.tigers.sumatra.botmanager.serial.SerialData.ESerialDataType;


/**
 * Configure wifi module and other base station parameters.
 * 
 * @author AndreR
 */
@SuppressWarnings("squid:S1192")
public class BaseStationConfigV3 extends ACommand
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	@SerialData(type = ESerialDataType.UINT8)
	private final int[] visionIp = new int[4];
	@SerialData(type = ESerialDataType.UINT16)
	private int visionPort = 10002;
	@SerialData(type = ESerialDataType.UINT8)
	private int channel = 100;
	@SerialData(type = ESerialDataType.UINT8)
	private int maxBots = 8;
	@SerialData(type = ESerialDataType.UINT8)
	private int fixedRuntime = 0;
	
	private static final int MAX_BOTS = 24;
	
	private static final Logger log = Logger.getLogger(BaseStationConfigV3.class.getName());
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Constructor.
	 */
	public BaseStationConfigV3()
	{
		super(ECommand.CMD_BASE_CONFIG_V3);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param ip IPv4 address string
	 */
	public void setVisionIp(final String ip)
	{
		InetAddress addr;
		try
		{
			addr = InetAddress.getByName(ip);
		} catch (UnknownHostException err)
		{
			log.error("Unknown host: " + ip, err);
			return;
		}
		
		visionIp[0] = addr.getAddress()[0] & 0xFF;
		visionIp[1] = addr.getAddress()[1] & 0xFF;
		visionIp[2] = addr.getAddress()[2] & 0xFF;
		visionIp[3] = addr.getAddress()[3] & 0xFF;
	}
	
	
	/**
	 * @return IPv4 Address string
	 */
	public String getVisionIp()
	{
		return new String(visionIp[0] + "." + visionIp[1] + "." + visionIp[2] + "." + visionIp[3]);
	}
	
	
	/**
	 * @return the visionPort
	 */
	public int getVisionPort()
	{
		return visionPort;
	}
	
	
	/**
	 * @param visionPort the visionPort to set
	 */
	public void setVisionPort(final int visionPort)
	{
		this.visionPort = visionPort;
	}
	
	
	/**
	 * @return the channel
	 */
	public int getChannel()
	{
		return channel;
	}
	
	
	/**
	 * @param channel the channel to set (0 - 255)
	 */
	public void setChannel(final int channel)
	{
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
	public void setMaxBots(final int maxBots)
	{
		if ((maxBots > MAX_BOTS) || (maxBots == 0))
		{
			log.error("Invalid max bots: " + maxBots + ". Max is: " + MAX_BOTS);
			return;
		}
		
		this.maxBots = maxBots;
	}
	
	
	/**
	 * @return the fixedRuntime
	 */
	public boolean isFixedRuntime()
	{
		return fixedRuntime > 0;
	}
	
	
	/**
	 * @param fixedRuntime the fixedRuntime to set
	 */
	public void setFixedRuntime(final boolean fixedRuntime)
	{
		this.fixedRuntime = fixedRuntime ? 1 : 0;
	}
}
