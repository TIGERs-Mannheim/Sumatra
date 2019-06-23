/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 14.06.2013
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.basestation;

import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ECommand;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData.ESerialDataType;


/**
 * Base station statistics.
 * 
 * @author AndreR
 * 
 */
public class BaseStationStats extends ACommand
{
	/** */
	public static final int	NUM_BOTS	= 12;
	
	/** */
	public static class WifiStats
	{
		/** */
		@SerialData(type = ESerialDataType.UINT8)
		private int		botId;
		
		/** */
		@SerialData(type = ESerialDataType.UINT8)
		private int		rxLoss;				// 0 - 200
		/** */
		@SerialData(type = ESerialDataType.UINT8)
		private int		txLoss;				// 0 - 200
		/** */
		@SerialData(type = ESerialDataType.UINT16)
		private int		linkQuality;		// 0 - 40000
		/** */
		@SerialData(type = ESerialDataType.UINT16)
		private int		rxLevel;			// 0 - 40000
		/** */
		@SerialData(type = ESerialDataType.UINT16)
		private int		txLevel;			// 0 - 40000
													
		/** */
		@SerialData(type = ESerialDataType.UINT32)
		private long	txBytesSentRaw;
		/** */
		@SerialData(type = ESerialDataType.UINT32)
		private long	txBytesSentCOBS;
		/** */
		@SerialData(type = ESerialDataType.UINT32)
		private long	rxBytesRecvRaw;
		/** */
		@SerialData(type = ESerialDataType.UINT32)
		private long	rxBytesRecvCOBS;
		
		/** */
		@SerialData(type = ESerialDataType.UINT32)
		private long	txPacketsSent;
		/** */
		@SerialData(type = ESerialDataType.UINT32)
		private long	rxPacketsRecv;
		
		
		/**
		 * @return the botId
		 */
		public BotID getBotId()
		{
			return BaseStationACommand.getBotIdFromBaseStationId(botId);
		}
		
		
		/**
		 * @return the rxLoss
		 */
		public float getRxLoss()
		{
			return rxLoss / 2.0f;
		}
		
		
		/**
		 * @return the txLoss
		 */
		public float getTxLoss()
		{
			return txLoss / 2.0f;
		}
		
		
		/**
		 * @return the linkQuality
		 */
		public float getLinkQuality()
		{
			return linkQuality / 400.0f;
		}
		
		
		/**
		 * @return the rxLevel
		 */
		public float getRxLevel()
		{
			return rxLevel / 400.0f;
		}
		
		
		/**
		 * @return the txLevel
		 */
		public float getTxLevel()
		{
			return txLevel / 400.0f;
		}
		
		
		/**
		 * @return the txBytesSentRaw
		 */
		public long getTxBytesSentRaw()
		{
			return txBytesSentRaw;
		}
		
		
		/**
		 * @return the txBytesSentCOBS
		 */
		public long getTxBytesSentCOBS()
		{
			return txBytesSentCOBS;
		}
		
		
		/**
		 * @return the rxBytesRecvRaw
		 */
		public long getRxBytesRecvRaw()
		{
			return rxBytesRecvRaw;
		}
		
		
		/**
		 * @return the rxBytesRecvCOBS
		 */
		public long getRxBytesRecvCOBS()
		{
			return rxBytesRecvCOBS;
		}
		
		
		/**
		 * @return the txPacketsSent
		 */
		public long getTxPacketsSent()
		{
			return txPacketsSent;
		}
		
		
		/**
		 * @return the rxPacketsRecv
		 */
		public long getRxPacketsRecv()
		{
			return rxPacketsRecv;
		}
	}
	
	/** */
	public static class EthStats
	{
		/** */
		@SerialData(type = ESerialDataType.UINT8)
		private int		rxLevel;		// 0 - 200
		/** */
		@SerialData(type = ESerialDataType.UINT8)
		private int		txLevel;		// 0 - 200
		/** */
		@SerialData(type = ESerialDataType.UINT16)
		private int		rxLoss;			// 0 - 40000
												
		/** */
		@SerialData(type = ESerialDataType.UINT32)
		private long	txBytesSent;
		/** */
		@SerialData(type = ESerialDataType.UINT32)
		private long	rxBytesRecv;
		
		/** */
		@SerialData(type = ESerialDataType.UINT32)
		private long	txFramesSent;
		/** */
		@SerialData(type = ESerialDataType.UINT32)
		private long	rxFramesRecv;
		
		@SerialData(type = ESerialDataType.UINT16)
		private int		updateRate;
		
		
		/**
		 * @return the rxLevel
		 */
		public float getRxLevel()
		{
			return rxLevel / 2.0f;
		}
		
		
		/**
		 * @return the txLevel
		 */
		public float getTxLevel()
		{
			return txLevel / 2.0f;
		}
		
		
		/**
		 * @return the rxLoss
		 */
		public float getRxLoss()
		{
			return rxLoss / 400.0f;
		}
		
		
		/**
		 * @return the txBytesSent
		 */
		public long getTxBytesSent()
		{
			return txBytesSent;
		}
		
		
		/**
		 * @return the rxBytesRecv
		 */
		public long getRxBytesRecv()
		{
			return rxBytesRecv;
		}
		
		
		/**
		 * @return the txFramesSent
		 */
		public long getTxFramesSent()
		{
			return txFramesSent;
		}
		
		
		/**
		 * @return the rxFramesRecv
		 */
		public long getRxFramesRecv()
		{
			return rxFramesRecv;
		}
		
		
		/**
		 * @return the updateRate
		 */
		public int getUpdateRate()
		{
			return updateRate;
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	@SerialData(type = ESerialDataType.EMBEDDED)
	private WifiStats	wifiStats[]	= new WifiStats[NUM_BOTS];
	@SerialData(type = ESerialDataType.EMBEDDED)
	private EthStats	ethStats		= new EthStats();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/** */
	public BaseStationStats()
	{
		super(ECommand.CMD_BASE_STATS);
		
		for (int i = 0; i < NUM_BOTS; i++)
		{
			wifiStats[i] = new WifiStats();
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return the wifiStats
	 */
	public WifiStats[] getWifiStats()
	{
		return wifiStats;
	}
	
	
	/**
	 * 
	 * @param botID
	 * @return null if botID is not found in stats
	 */
	public WifiStats getWifiStats(BotID botID)
	{
		for (WifiStats stats : wifiStats)
		{
			if (stats.getBotId() == botID)
			{
				return stats;
			}
		}
		
		return null;
	}
	
	
	/**
	 * @return the ethStats
	 */
	public EthStats getEthStats()
	{
		return ethStats;
	}
}
