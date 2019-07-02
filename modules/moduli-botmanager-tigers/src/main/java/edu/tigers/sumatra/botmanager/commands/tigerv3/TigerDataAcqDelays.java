/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 05.09.2016
 * Author(s): rYan
 * *********************************************************
 */
package edu.tigers.sumatra.botmanager.commands.tigerv3;

import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.botmanager.commands.ECommand;
import edu.tigers.sumatra.botmanager.serial.SerialData;
import edu.tigers.sumatra.botmanager.serial.SerialData.ESerialDataType;


/**
 * Used for delay between various sensors and control output.
 * Identification only uses rotational components.
 * 
 * @author AndreR
 */
public class TigerDataAcqDelays extends ACommand
{
	/** [us] */
	@SerialData(type = ESerialDataType.UINT32)
	private long	timestamp;
	
	/** [us] */
	@SerialData(type = ESerialDataType.UINT32)
	private long	visionTime			= 0;
	
	/** [mrad/s] */
	@SerialData(type = ESerialDataType.INT16)
	private int		outVelocityW		= 0;
	
	/** [mrad] */
	@SerialData(type = ESerialDataType.INT16)
	private int		visionPositionW	= 0;
	
	/** [mrad/s] */
	@SerialData(type = ESerialDataType.INT16)
	private int		gyroVelocityW		= 0;
	
	
	/** Constructor. */
	public TigerDataAcqDelays()
	{
		super(ECommand.CMD_DATA_ACQ_DELAYS);
	}
	
	
	/**
	 * @return the timestamp in [us]
	 */
	public long getTimestamp()
	{
		return timestamp;
	}
	
	
	/**
	 * @param timestamp the timestamp to set in [us]
	 */
	public void setTimestamp(final long timestamp)
	{
		this.timestamp = timestamp;
	}
	
	
	/**
	 * @return the visionTime in [us]
	 */
	public long getVisionTime()
	{
		return visionTime;
	}
	
	
	/**
	 * @param visionTime the visionTime to set in [us]
	 */
	public void setVisionTime(final long visionTime)
	{
		this.visionTime = visionTime;
	}
	
	
	/**
	 * @return the outVelocityW in [rad/s]
	 */
	public double getOutVelocityW()
	{
		return outVelocityW * 0.001;
	}
	
	
	/**
	 * @param outVelocityW the outVelocityW to set in [rad/s]
	 */
	public void setOutVelocityW(final double outVelocityW)
	{
		this.outVelocityW = (int) (outVelocityW * 1000.0);
	}
	
	
	/**
	 * @return the visionPositionW in [rad/s]
	 */
	public double getVisionPositionW()
	{
		return visionPositionW * 0.001;
	}
	
	
	/**
	 * @param visionPositionW the visionPositionW to set in [rad/s]
	 */
	public void setVisionPositionW(final double visionPositionW)
	{
		this.visionPositionW = (int) (visionPositionW * 1000.0);
	}
	
	
	/**
	 * @return the gyroVelocityW in [rad/s]
	 */
	public double getGyroVelocityW()
	{
		return gyroVelocityW * 0.001;
	}
	
	
	/**
	 * @param gyroVelocityW the gyroVelocityW to set in [rad/s]
	 */
	public void setGyroVelocityW(final double gyroVelocityW)
	{
		this.gyroVelocityW = (int) (gyroVelocityW * 1000.0);
	}
}
