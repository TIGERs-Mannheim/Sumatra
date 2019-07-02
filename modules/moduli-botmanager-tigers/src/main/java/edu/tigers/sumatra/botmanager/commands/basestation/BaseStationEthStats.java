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


/**
 * Synopsis of ethernet traffic at the base station.
 * 
 * @author AndreR
 */
public class BaseStationEthStats extends ACommand
{
	@SerialData(type = ESerialDataType.UINT32)
	private long	txFrames;
	
	@SerialData(type = ESerialDataType.UINT32)
	private long	txBytes;
	
	@SerialData(type = ESerialDataType.UINT32)
	private long	rxFrames;
	
	@SerialData(type = ESerialDataType.UINT32)
	private long	rxBytes;
	
	@SerialData(type = ESerialDataType.UINT16)
	private int		rxFramesDmaOverrun;
	
	@SerialData(type = ESerialDataType.UINT8)
	private int		ntpSync;
	
	
	/**
	 * Constructor.
	 */
	public BaseStationEthStats()
	{
		super(ECommand.CMD_BASE_ETH_STATS);
	}
	
	
	/**
	 * Create a new BaseStationEthStats object from the operation a-b
	 * 
	 * @param a
	 * @param b
	 */
	public BaseStationEthStats(final BaseStationEthStats a, final BaseStationEthStats b)
	{
		super(ECommand.CMD_BASE_ETH_STATS);
		
		txFrames = a.txFrames - b.txFrames;
		txBytes = a.txBytes - b.txBytes;
		rxFrames = a.rxFrames - b.rxFrames;
		rxBytes = a.rxBytes - b.rxBytes;
		rxFramesDmaOverrun = a.rxFramesDmaOverrun - b.rxFramesDmaOverrun;
		ntpSync = a.ntpSync;
	}
	
	
	/**
	 * @return the txFrames
	 */
	public long getTxFrames()
	{
		return txFrames;
	}
	
	
	/**
	 * @return the txBytes
	 */
	public long getTxBytes()
	{
		return txBytes;
	}
	
	
	/**
	 * @return the rxFrames
	 */
	public long getRxFrames()
	{
		return rxFrames;
	}
	
	
	/**
	 * @return the rxBytes
	 */
	public long getRxBytes()
	{
		return rxBytes;
	}
	
	
	/**
	 * @return the rxFramesDmaOverrun
	 */
	public int getRxFramesDmaOverrun()
	{
		return rxFramesDmaOverrun;
	}
	
	
	/**
	 * @return
	 */
	public double getRxLoss()
	{
		double goodFrames = rxFrames;
		double badFrames = rxFramesDmaOverrun;
		
		return badFrames / (badFrames + goodFrames + 1);
	}
	
	
	/**
	 * @return the ntpSync
	 */
	public boolean isNtpSync()
	{
		return ntpSync == 1;
	}
}
