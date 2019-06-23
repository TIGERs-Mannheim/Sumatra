/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 11.12.2014
 * Author(s): AndreR
 * *********************************************************
 */
package edu.tigers.sumatra.botmanager.commands.basestation;

import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.botmanager.commands.ECommand;
import edu.tigers.sumatra.botmanager.serial.SerialData;
import edu.tigers.sumatra.botmanager.serial.SerialData.ESerialDataType;
import edu.tigers.sumatra.ids.BotID;


/**
 * Synopsis of all Wifi traffic and feedback from bot.
 * 
 * @author AndreR
 */
public class BaseStationWifiStats extends ACommand
{
	/** */
	public static final int	NUM_BOTS	= 24;
	
	/** */
	public static class BotStats
	{
		/** NRF wifi module IO stats */
		public static class NRF24IOStats
		{
			/** */
			@SerialData(type = ESerialDataType.UINT16)
			public int	txPackets;
			/** */
			@SerialData(type = ESerialDataType.UINT16)
			public int	txBytes;
			/** */
			@SerialData(type = ESerialDataType.UINT16)
			public int	rxPackets;
			/** */
			@SerialData(type = ESerialDataType.UINT16)
			public int	rxBytes;
			
			/** */
			@SerialData(type = ESerialDataType.UINT16)
			public int	rxPacketsLost;
			/** */
			@SerialData(type = ESerialDataType.UINT16)
			public int	txPacketsMaxRT;
			/** */
			@SerialData(type = ESerialDataType.UINT16)
			public int	txPacketsAcked;
			
			
			/**
			 * return this-rhs
			 * 
			 * @param rhs
			 * @return
			 */
			public NRF24IOStats subtractNew(final NRF24IOStats rhs)
			{
				NRF24IOStats ret = new NRF24IOStats();
				
				ret.txPackets = txPackets - rhs.txPackets;
				ret.txBytes = txBytes - rhs.txBytes;
				ret.rxPackets = rxPackets - rhs.rxPackets;
				ret.rxBytes = rxBytes - rhs.rxBytes;
				
				ret.rxPacketsLost = rxPacketsLost - rhs.rxPacketsLost;
				ret.txPacketsMaxRT = txPacketsMaxRT - rhs.txPacketsMaxRT;
				ret.txPacketsAcked = txPacketsAcked - rhs.txPacketsAcked;
				
				ret.txPackets &= 0xFFFF;
				ret.txBytes &= 0xFFFF;
				ret.rxPackets &= 0xFFFF;
				ret.rxBytes &= 0xFFFF;
				ret.rxPacketsLost &= 0xFFFF;
				ret.txPacketsMaxRT &= 0xFFFF;
				ret.txPacketsAcked &= 0xFFFF;
				
				return ret;
			}
			
			
			/**
			 * Compute link quality from RX and TX losses.
			 * 
			 * @return Quality from 1.0 (good) to 0.0 (bad)
			 */
			public double getLinkQuality()
			{
				double allGoodPackets = txPacketsAcked + rxPackets + 1; // +1 to prevent div by zero
				double allLostPackets = rxPacketsLost + txPacketsMaxRT;
				
				return allGoodPackets / (allGoodPackets + allLostPackets);
			}
			
			
			/**
			 * Compute TX losses.
			 * 
			 * @return
			 */
			public double getTxLoss()
			{
				double txAll = txPackets + 1;
				double txBad = txPacketsMaxRT;
				
				return txBad / txAll;
			}
			
			
			/**
			 * Compute RX losses.
			 * 
			 * @return
			 */
			public double getRxLoss()
			{
				double rxGood = rxPackets + 1;
				double rxBad = rxPacketsLost;
				
				return rxBad / (rxBad + rxGood);
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
			/** */
			@SerialData(type = ESerialDataType.UINT16)
			public int	txPackets;
			/** */
			@SerialData(type = ESerialDataType.UINT16)
			public int	txBytes;
			/** */
			@SerialData(type = ESerialDataType.UINT16)
			public int	rxPackets;
			/** */
			@SerialData(type = ESerialDataType.UINT16)
			public int	rxBytes;
			
			/** */
			@SerialData(type = ESerialDataType.UINT16)
			public int	txPacketsLost;
			/** */
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
				double txGood = txPackets + 1;
				double txBad = txPacketsLost;
				
				return txBad / (txGood + txBad);
			}
			
			
			/**
			 * Get RX loss ratio (good 0.0 - 1.0 bad)
			 * 
			 * @return
			 */
			public double getRxLoss()
			{
				double rxGood = rxPackets + 1;
				double rxBad = rxPacketsLost;
				
				return rxBad / (rxGood + rxBad);
			}
		}
		
		/** */
		@SerialData(type = ESerialDataType.UINT8)
		private int				botId	= 0xFF;
		
		/** */
		@SerialData(type = ESerialDataType.EMBEDDED)
		public NRF24IOStats	nrf	= new NRF24IOStats();
		
		/** */
		@SerialData(type = ESerialDataType.EMBEDDED)
		public QueueIOStats	queue	= new QueueIOStats();
		
		
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
			ret.nrf = nrf.subtractNew(rhs.nrf);
			ret.queue = queue.subtractNew(rhs.queue);
			
			return ret;
		}
		
		
		/**
		 * @return
		 */
		public BotID getBotId()
		{
			return BotID.createBotIdFromIdWithColorOffsetBS(botId);
		}
	}
	
	@SerialData(type = ESerialDataType.EMBEDDED)
	private final BotStats	bots[]		= new BotStats[NUM_BOTS];
	
	@SerialData(type = ESerialDataType.UINT16)
	private int					updateRate	= 1;
	
	
	/** */
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
