/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 05.09.2016
 * Author(s): rYan
 * *********************************************************
 */
package edu.tigers.sumatra.botmanager.commands.tigerv3;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.botmanager.commands.ECommand;
import edu.tigers.sumatra.botmanager.serial.SerialData;
import edu.tigers.sumatra.botmanager.serial.SerialData.ESerialDataType;


/**
 * Used for robot high-level system identification.
 * Includes real output velocity (after feedforward and control) and vision position with extra timestamp.
 * 
 * @author AndreR
 */
public class TigerDataAcqBotModel extends ACommand
{
	/** [us] */
	@SerialData(type = ESerialDataType.UINT32)
	private long	timestamp		= 0;
	
	/** [us] */
	@SerialData(type = ESerialDataType.UINT32)
	private long	visionTime		= 0;
	
	/** [mm/s, crad/s] */
	@SerialData(type = ESerialDataType.INT16)
	private int[]	outVelocity		= new int[3];
	
	/** [mm, mrad] */
	@SerialData(type = ESerialDataType.INT16)
	private int[]	visionPosition	= new int[3];
	
	
	/** Constructor. */
	public TigerDataAcqBotModel()
	{
		super(ECommand.CMD_DATA_ACQ_BOT_MODEL);
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
	 * @return the outVelocity in [m/s, rad/s]
	 */
	public double[] getOutVelocity()
	{
		double[] outVel = new double[3];
		
		outVel[0] = outVelocity[0] * 0.001;
		outVel[1] = outVelocity[1] * 0.001;
		outVel[2] = outVelocity[2] * 0.01;
		
		return outVel;
	}
	
	
	/**
	 * @return the outVelocity in [m/s, rad/s]
	 */
	public List<Double> getOutVelocityList()
	{
		List<Double> outVel = new ArrayList<>(3);
		
		outVel.add(outVelocity[0] * 0.001);
		outVel.add(outVelocity[1] * 0.001);
		outVel.add(outVelocity[2] * 0.01);
		
		return outVel;
	}
	
	
	/**
	 * @param outVelocity the outVelocity to set in [m/s, rad/s]
	 */
	public void setOutVelocity(final double[] outVelocity)
	{
		this.outVelocity[0] = (int) (outVelocity[0] * 1000.0);
		this.outVelocity[1] = (int) (outVelocity[1] * 1000.0);
		this.outVelocity[2] = (int) (outVelocity[2] * 100.0);
	}
	
	
	/**
	 * @return the visionPosition in [m]
	 */
	public double[] getVisionPosition()
	{
		double[] outPos = new double[3];
		
		outPos[0] = visionPosition[0] * 0.001;
		outPos[1] = visionPosition[1] * 0.001;
		outPos[2] = visionPosition[2] * 0.001;
		
		return outPos;
	}
	
	
	/**
	 * @return the visionPosition in [m]
	 */
	public List<Double> getVisionPositionList()
	{
		return IntStream.of(visionPosition).mapToDouble(m -> m * 0.001).boxed().collect(Collectors.toList());
	}
	
	
	/**
	 * @param visionPosition the visionPosition to set in [m]
	 */
	public void setVisionPosition(final double[] visionPosition)
	{
		this.visionPosition[0] = (int) (visionPosition[0] * 1000.0);
		this.visionPosition[1] = (int) (visionPosition[1] * 1000.0);
		this.visionPosition[2] = (int) (visionPosition[2] * 1000.0);
	}
}
