/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 05.09.2016
 * Author(s): rYan
 * *********************************************************
 */
package edu.tigers.sumatra.botmanager.commands.tigerv3;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.botmanager.commands.ECommand;
import edu.tigers.sumatra.botmanager.serial.SerialData;
import edu.tigers.sumatra.botmanager.serial.SerialData.ESerialDataType;


/**
 * Used for motor model system identification.
 * Includes motor output voltage and encoder velocity.
 * 
 * @author AndreR
 */
public class TigerDataAcqMotorModel extends ACommand
{
	/** [us] */
	@SerialData(type = ESerialDataType.UINT32)
	private long	timestamp		= 0;
	
	/** [mV] */
	@SerialData(type = ESerialDataType.INT16)
	private int[]	motorVoltage	= new int[4];
	
	/** [rad/s*40] */
	@SerialData(type = ESerialDataType.INT16)
	private int[]	motorVelocity	= new int[4];
	
	
	/** Constructor. */
	public TigerDataAcqMotorModel()
	{
		super(ECommand.CMD_DATA_ACQ_MOTOR_MODEL);
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
	 * @return the motorVoltage in [V]
	 */
	public double[] getMotorVoltage()
	{
		double[] motVol = new double[motorVoltage.length];
		
		for (int i = 0; i < motorVoltage.length; i++)
		{
			motVol[i] = motorVoltage[i] * 0.001;
		}
		
		return motVol;
	}
	
	
	/**
	 * @return the motorVoltage in [V]
	 */
	public List<Double> getMotorVoltageList()
	{
		return IntStream.of(motorVoltage).mapToDouble(m -> m * 0.001).boxed().collect(Collectors.toList());
	}
	
	
	/**
	 * @param motVol the motorVoltage to set in [V]
	 */
	public void setMotorVoltage(final double[] motVol)
	{
		for (int i = 0; i < motorVoltage.length; i++)
		{
			motorVoltage[i] = (int) (motVol[i] * 1000.0);
		}
	}
	
	
	/**
	 * @return the motorVelocity in [rad/s]
	 */
	public double[] getMotorVelocity()
	{
		double[] motVel = new double[motorVelocity.length];
		
		for (int i = 0; i < motorVelocity.length; i++)
		{
			motVel[i] = motorVelocity[i] * 0.025;
		}
		
		return motVel;
	}
	
	
	/**
	 * @return the motorVelocity in [rad/s]
	 */
	public List<Double> getMotorVelocityList()
	{
		return IntStream.of(motorVelocity).mapToDouble(m -> m * 0.025).boxed().collect(Collectors.toList());
	}
	
	
	/**
	 * @param motVel the motorVelocity to set in [rad/s]
	 */
	public void setMotorVelocity(final double[] motVel)
	{
		for (int i = 0; i < motorVelocity.length; i++)
		{
			motorVelocity[i] = (int) (motVel[i] * 40.0);
		}
	}
}
