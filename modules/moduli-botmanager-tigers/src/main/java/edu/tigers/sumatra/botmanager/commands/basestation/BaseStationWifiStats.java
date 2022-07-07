/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botmanager.commands.basestation;

import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.botmanager.commands.ECommand;
import edu.tigers.sumatra.botmanager.serial.SerialData;
import edu.tigers.sumatra.botmanager.serial.SerialData.ESerialDataType;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.SumatraMath;


/**
 * Synopsis of all Wifi traffic and feedback from bot.
 * 
 * @author AndreR
 */
public class BaseStationWifiStats extends ACommand
{
	/** */
	public static final int NUM_BOTS = 32;

	/**
	 * Bot network statistics.
	 */
	public static class BotStats
	{
		/** Wifi module IO stats */
		public static class RFIOStats
		{
			/** Transmitted packets. */
			@SerialData(type = ESerialDataType.UINT16)
			public int txPackets;
			/** Transmitted bytes. */
			@SerialData(type = ESerialDataType.UINT16)
			public int txBytes;
			/** Received packets. */
			@SerialData(type = ESerialDataType.UINT16)
			public int rxPackets;
			/** Received bytes. */
			@SerialData(type = ESerialDataType.UINT16)
			public int rxBytes;
			
			/** Lost packets in reception. */
			@SerialData(type = ESerialDataType.UINT16)
			public int	rxPacketsLost;


			/**
			 * return this-rhs
			 *
			 * @param rhs
			 * @return
			 */
			public RFIOStats subtractNew(final RFIOStats rhs)
			{
				RFIOStats ret = new RFIOStats();

				ret.txPackets = txPackets - rhs.txPackets;
				ret.txBytes = txBytes - rhs.txBytes;
				ret.rxPackets = rxPackets - rhs.rxPackets;
				ret.rxBytes = rxBytes - rhs.rxBytes;

				ret.rxPacketsLost = rxPacketsLost - rhs.rxPacketsLost;

				ret.txPackets &= 0xFFFF;
				ret.txBytes &= 0xFFFF;
				ret.rxPackets &= 0xFFFF;
				ret.rxBytes &= 0xFFFF;
				ret.rxPacketsLost &= 0xFFFF;

				return ret;
			}
			
			
			/**
			 * Compute RX losses.
			 * 
			 * @return
			 */
			public double getRxLoss()
			{
				if (rxPackets == 0)
					return 0.0;

				return (double) rxPackets / (rxPackets + rxPacketsLost);
			}
			
			
			/**
			 * Compute TX link saturation based on base station update rate.
			 * 
			 * @param rate Base station update rate.
			 * @return
			 */
			public double getTxSaturation(final double rate)
			{
				return txBytes / (rate * 32);
			}
			
			
			/**
			 * Compute RX link saturation based on base station update rate.
			 * 
			 * @param rate Base station update rate.
			 * @return
			 */
			public double getRxSaturation(final double rate)
			{
				return rxBytes / (rate * 32);
			}
		}
		
		/** Queue IO stats */
		public static class QueueIOStats
		{
			/** Transmitted packets. */
			@SerialData(type = ESerialDataType.UINT16)
			public int	txPackets;
			/** Transmitted bytes. */
			@SerialData(type = ESerialDataType.UINT16)
			public int	txBytes;
			/** Received packets. */
			@SerialData(type = ESerialDataType.UINT16)
			public int	rxPackets;
			/** Received bytes. */
			@SerialData(type = ESerialDataType.UINT16)
			public int	rxBytes;
			
			/** Lost transmitted packets. */
			@SerialData(type = ESerialDataType.UINT16)
			public int	txPacketsLost;
			/** Lost received packets. */
			@SerialData(type = ESerialDataType.UINT16)
			public int	rxPacketsLost;
			
			
			/**
			 * return this-rhs;
			 * 
			 * @param rhs
			 * @return
			 */
			public QueueIOStats subtractNew(final QueueIOStats rhs)
			{
				QueueIOStats ret = new QueueIOStats();
				
				ret.txPackets = txPackets - rhs.txPackets;
				ret.txBytes = txBytes - rhs.txBytes;
				ret.rxPackets = rxPackets - rhs.rxPackets;
				ret.rxBytes = rxBytes - rhs.rxBytes;
				ret.txPacketsLost = txPacketsLost - rhs.txPacketsLost;
				ret.rxPacketsLost = rxPacketsLost - rhs.rxPacketsLost;
				
				ret.txPackets &= 0xFFFF;
				ret.txBytes &= 0xFFFF;
				ret.rxPackets &= 0xFFFF;
				ret.rxBytes &= 0xFFFF;
				ret.txPacketsLost &= 0xFFFF;
				ret.rxPacketsLost &= 0xFFFF;
				
				return ret;
			}
			
			
			/**
			 * Get TX loss ratio (good 0.0 - 1.0 bad)
			 * 
			 * @return
			 */
			public double getTxLoss()
			{
				if (txPacketsLost == 0)
					return 0.0;

				return (double) txPacketsLost / (txPackets + txPacketsLost);
			}
			
			
			/**
			 * Get RX loss ratio (good 0.0 - 1.0 bad)
			 *
			 * @return
			 */
			public double getRxLoss()
			{
				if (rxPacketsLost == 0)
					return 0.0;

				return (double) rxPacketsLost / (rxPackets + rxPacketsLost);
			}
		}

		/**
		 * Bot ID.
		 */
		@SerialData(type = ESerialDataType.UINT8)
		private int botId = 0xFF;

		/**
		 * RF stats.
		 */
		@SerialData(type = ESerialDataType.EMBEDDED)
		public RFIOStats rf = new RFIOStats();

		/**
		 * Queue stats.
		 */
		@SerialData(type = ESerialDataType.EMBEDDED)
		public QueueIOStats queue = new QueueIOStats();

		/**
		 * RSSI of robot in dBm*0.1
		 */
		@SerialData(type = ESerialDataType.INT16)
		private int botRssi;

		/**
		 * RSSI of base station in dBm*0.1
		 */
		@SerialData(type = ESerialDataType.INT16)
		private int bsRssi;


		/**
		 * return this-rhs
		 * botId stays the same
		 *
		 * @param rhs
		 * @return
		 */
		public BotStats subtractNew(final BotStats rhs)
		{
			BotStats ret = new BotStats();

			ret.botId = rhs.botId;
			ret.rf = rf.subtractNew(rhs.rf);
			ret.queue = queue.subtractNew(rhs.queue);
			ret.botRssi = rhs.botRssi;
			ret.bsRssi = rhs.bsRssi;

			return ret;
		}


		/**
		 * @return
		 */
		public BotID getBotId()
		{
			return BotID.createBotIdFromIdWithColorOffsetBS(botId);
		}


		public double getBotRssi()
		{
			return botRssi * 0.1;
		}


		public double getBsRssi()
		{
			return bsRssi * 0.1;
		}


		/**
		 * Compute link quality from RSSI.
		 *
		 * @return Quality from 1.0 (good) to 0.0 (bad)
		 */
		public double getLinkQuality()
		{
			return SumatraMath.relative((getBotRssi() + getBsRssi()) / 2, -90.0, -12.0);
		}
	}

	@SerialData(type = ESerialDataType.EMBEDDED)
	private final BotStats[] bots = new BotStats[NUM_BOTS];
	
	@SerialData(type = ESerialDataType.UINT16)
	private int						updateRate	= 1;
	
	
	/** Constructor. */
	public BaseStationWifiStats()
	{
		super(ECommand.CMD_BASE_WIFI_STATS);
		
		for (int i = 0; i < NUM_BOTS; i++)
		{
			bots[i] = new BotStats();
		}
	}
	
	
	/**
	 * Create a new BaseStationWifiStats object from the operation a-b.
	 * updateRate is taken from a.
	 * 
	 * @param a
	 * @param b
	 */
	public BaseStationWifiStats(final BaseStationWifiStats a, final BaseStationWifiStats b)
	{
		super(ECommand.CMD_BASE_WIFI_STATS);
		
		updateRate = a.updateRate;
		
		for (int i = 0; i < NUM_BOTS; i++)
		{
			bots[i] = a.getBotStats()[i].subtractNew(b.getBotStats()[i]);
		}
	}
	
	
	/**
	 * @return
	 */
	public BotStats[] getBotStats()
	{
		return bots;
	}
	
	
	/**
	 * @return
	 */
	public int getUpdateRate()
	{
		return updateRate;
	}
	
	
	/**
	 * Check if the given bot exists in this statistics messages.
	 * 
	 * @param id
	 * @return
	 */
	public boolean isBotConnected(final BotID id)
	{
		int number = id.getNumberWithColorOffsetBS();
		
		for (BotStats s : bots)
		{
			if (s.botId == number)
			{
				return true;
			}
		}
		
		return false;
	}
}
