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

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.CommandConstants;


/**
 * Base station statistics.
 * 
 * @author AndreR
 * 
 */
public class BaseStationStats extends ACommand
{
	/** */
	public static class WifiStats
	{
		/** */
		public long	txBytesSent;
		/** */
		public long	txBytesLost;
		/** */
		public long	rxBytesRecv;
		/** */
		public long	rxBytesLost;
		
		/** */
		public long	txPacketsSent;
		/** */
		public long	txPacketsLost;
		/** */
		public long	rxPacketsRecv;
		/** */
		public long	rxPacketsLost;
		
		/** */
		public long	retransmissions;
	}
	
	/** */
	public static class EthStats
	{
		/** */
		public long	txBytesSent;
		/** */
		public long	rxBytesRecv;
		/** */
		public long	rxBytesLost;
		
		/** */
		public long	txFramesSent;
		/** */
		public long	rxFramesRecv;
		/** */
		public long	rxFramesLost;
		
	}
	
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private WifiStats	wifiStats	= new WifiStats();
	private EthStats	ethStats		= new EthStats();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void setData(byte[] data)
	{
		wifiStats.txBytesSent = byteArray2UInt(data, 0);
		wifiStats.txBytesLost = byteArray2UInt(data, 4);
		wifiStats.rxBytesRecv = byteArray2UInt(data, 8);
		wifiStats.rxBytesLost = byteArray2UInt(data, 12);
		
		wifiStats.txPacketsSent = byteArray2UInt(data, 16);
		wifiStats.txPacketsLost = byteArray2UInt(data, 20);
		wifiStats.rxPacketsRecv = byteArray2UInt(data, 24);
		wifiStats.rxPacketsLost = byteArray2UInt(data, 28);
		
		wifiStats.retransmissions = byteArray2UInt(data, 32);
		
		ethStats.txBytesSent = byteArray2UInt(data, 36);
		ethStats.rxBytesRecv = byteArray2UInt(data, 40);
		ethStats.rxBytesLost = byteArray2UInt(data, 44);
		
		ethStats.txFramesSent = byteArray2UInt(data, 48);
		ethStats.rxFramesRecv = byteArray2UInt(data, 52);
		ethStats.rxFramesLost = byteArray2UInt(data, 56);
	}
	
	
	@Override
	public byte[] getData()
	{
		final byte data[] = new byte[getDataLength()];
		
		int2ByteArray(data, 0, (int) wifiStats.txBytesSent);
		int2ByteArray(data, 4, (int) wifiStats.txBytesLost);
		int2ByteArray(data, 8, (int) wifiStats.rxBytesRecv);
		int2ByteArray(data, 12, (int) wifiStats.rxBytesLost);
		
		int2ByteArray(data, 16, (int) wifiStats.txPacketsSent);
		int2ByteArray(data, 20, (int) wifiStats.txPacketsLost);
		int2ByteArray(data, 24, (int) wifiStats.rxPacketsRecv);
		int2ByteArray(data, 28, (int) wifiStats.rxPacketsLost);
		
		int2ByteArray(data, 32, (int) wifiStats.retransmissions);
		
		int2ByteArray(data, 36, (int) ethStats.txBytesSent);
		int2ByteArray(data, 40, (int) ethStats.rxBytesRecv);
		int2ByteArray(data, 44, (int) ethStats.rxBytesLost);
		
		int2ByteArray(data, 48, (int) ethStats.txFramesSent);
		int2ByteArray(data, 52, (int) ethStats.rxFramesRecv);
		int2ByteArray(data, 56, (int) ethStats.rxFramesLost);
		
		return data;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public int getCommand()
	{
		return CommandConstants.CMD_BASE_STATS;
	}
	
	
	@Override
	public int getDataLength()
	{
		return 60;
	}
	
	
	/**
	 * @return the wifiStats
	 */
	public WifiStats getWifiStats()
	{
		return wifiStats;
	}
	
	
	/**
	 * @param wifiStats the wifiStats to set
	 */
	public void setWifiStats(WifiStats wifiStats)
	{
		this.wifiStats = wifiStats;
	}
	
	
	/**
	 * @return the ethStats
	 */
	public EthStats getEthStats()
	{
		return ethStats;
	}
	
	
	/**
	 * @param ethStats the ethStats to set
	 */
	public void setEthStats(EthStats ethStats)
	{
		this.ethStats = ethStats;
	}
}
