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
 * Configure wifi modules and other base station parameters.
 * 
 * @author AndreR
 */
@SuppressWarnings("squid:S1192")
public class BaseStationConfigV2 extends ACommand
{
	/**
	 * Wifi speed.
	 */
	public enum EWifiSpeed
	{
		/** */
		WIFI_SPEED_250K(0),
		/** */
		WIFI_SPEED_1M(1),
		/** */
		WIFI_SPEED_2M(2),
		/** */
		WIFI_SPEED_UNKNOWN(255);
		
		private final int id;
		
		
		private EWifiSpeed(final int id)
		{
			this.id = id;
		}
		
		
		/**
		 * @return
		 */
		public int getId()
		{
			return id;
		}
		
		
		/**
		 * Convert an id to an enum.
		 * 
		 * @param id 1 (MAIN) or 2 (MEDIA)
		 * @return enum
		 */
		public static EWifiSpeed getSpeedConstant(final int id)
		{
			for (EWifiSpeed s : values())
			{
				if (s.getId() == id)
				{
					return s;
				}
			}
			
			return WIFI_SPEED_UNKNOWN;
		}
	}
	
	/** Config for a single WiFi module */
	public static final class BSModuleConfig
	{
		@SerialData(type = ESerialDataType.UINT8)
		private int		channel			= 100;
		@SerialData(type = ESerialDataType.UINT8)
		private int		speed				= 2;
		@SerialData(type = ESerialDataType.UINT8)
		private int		maxBots			= 8;
		@SerialData(type = ESerialDataType.UINT8)
		private int		fixedRuntime	= 0;
		@SerialData(type = ESerialDataType.UINT32)
		private long	timeout			= 1000;
		
		
		/**
		 * @return the channel
		 */
		public int getChannel()
		{
			return channel;
		}
		
		
		/**
		 * @param channel the channel to set (0 - 127)
		 */
		public void setChannel(final int channel)
		{
			if (channel > MAX_CHANNEL)
			{
				log.error("Invalid max channel: " + channel + ". Max is: " + MAX_CHANNEL);
				return;
			}
			
			this.channel = channel;
		}
		
		
		/**
		 * @return the speed
		 */
		public EWifiSpeed getSpeed()
		{
			return EWifiSpeed.getSpeedConstant(speed);
		}
		
		
		/**
		 * @param speed the speed to set
		 */
		public void setSpeed(final EWifiSpeed speed)
		{
			this.speed = speed.getId();
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
		
		
		/**
		 * @return the timeout
		 */
		public long getTimeout()
		{
			return timeout;
		}
		
		
		/**
		 * @param timeout the timeout to set
		 */
		public void setTimeout(final long timeout)
		{
			this.timeout = timeout;
		}
	}
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	@SerialData(type = ESerialDataType.UINT8)
	private final int[]					visionIp		= new int[4];
	@SerialData(type = ESerialDataType.UINT16)
	private int								visionPort	= 10002;
	@SerialData(type = ESerialDataType.EMBEDDED)
	private final BSModuleConfig[]	modules		= new BSModuleConfig[NUM_MODULES];
	@SerialData(type = ESerialDataType.UINT8)
	private int								rstEnabled	= 0;
	@SerialData(type = ESerialDataType.UINT8)
	private int								rstRate		= 50;
	@SerialData(type = ESerialDataType.UINT16)
	private int								rstPort		= 10010;
	
	private static final int			MAX_CHANNEL	= 127;
	private static final int			MAX_BOTS		= 24;
	private static final int			NUM_MODULES	= 2;
	
	private static final Logger		log			= Logger.getLogger(BaseStationConfigV2.class.getName());
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Constructor.
	 */
	public BaseStationConfigV2()
	{
		super(ECommand.CMD_BASE_CONFIG_V2);
		
		for (int i = 0; i < NUM_MODULES; i++)
		{
			modules[i] = new BSModuleConfig();
		}
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
	 * @return the rstPort
	 */
	public int getRstPort()
	{
		return rstPort;
	}
	
	
	/**
	 * @param rstPort the rstPort to set
	 */
	public void setRstPort(final int rstPort)
	{
		this.rstPort = rstPort;
	}
	
	
	/**
	 * @param index
	 * @return
	 */
	public BSModuleConfig getModuleConfig(final int index)
	{
		if (index > NUM_MODULES)
		{
			log.error("Invalid module index: " + index + ". Max is: " + NUM_MODULES);
			return null;
		}
		
		return modules[index];
	}
	
	
	/**
	 * @return
	 */
	public int getNumModules()
	{
		return NUM_MODULES;
	}
	
	
	/**
	 * @return the rstEnabled
	 */
	public int getRstEnabled()
	{
		return rstEnabled;
	}
	
	
	/**
	 * @param rstEnabled the rstEnabled to set
	 */
	public void setRstEnabled(final boolean rstEnabled)
	{
		this.rstEnabled = rstEnabled ? 1 : 0;
	}
	
	
	/**
	 * @return the rstRate
	 */
	public int getRstRate()
	{
		return rstRate;
	}
	
	
	/**
	 * @param rstRate the rstRate to set
	 */
	public void setRstRate(final int rstRate)
	{
		this.rstRate = rstRate;
	}
}
