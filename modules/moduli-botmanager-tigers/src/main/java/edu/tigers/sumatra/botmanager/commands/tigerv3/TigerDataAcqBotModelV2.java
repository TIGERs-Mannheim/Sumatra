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

import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.botmanager.commands.ECommand;
import edu.tigers.sumatra.botmanager.serial.SerialData;
import edu.tigers.sumatra.botmanager.serial.SerialData.ESerialDataType;


/**
 * Used for robot high-level system identification.
 * 
 * @author AndreR
 */
public class TigerDataAcqBotModelV2 extends ACommand
{
	/** [us] */
	@SerialData(type = ESerialDataType.UINT32)
	private long timestamp = 0;
	
	/** [mm/s, crad/s] */
	@SerialData(type = ESerialDataType.INT16)
	private int[] stateVelocity = new int[3];
	
	/** [mm/s, crad/s] */
	@SerialData(type = ESerialDataType.INT16)
	private int[] encoderVelocity = new int[3];
	
	/** [cN, mNm] */
	@SerialData(type = ESerialDataType.INT16)
	private int[] outputForce = new int[3];
	
	/** [factor*1e4] */
	@SerialData(type = ESerialDataType.UINT8)
	private int[] efficiency = new int[3];
	
	/**
	 * move mode
	 * 0 = unknown
	 * 1 = local force
	 * 2 = stopped
	 * 3 = plateau
	 * 4 = acc
	 * 5 = dec
	 */
	@SerialData(type = ESerialDataType.UINT8)
	private int mode = 0;
	
	
	/** Constructor. */
	public TigerDataAcqBotModelV2()
	{
		super(ECommand.CMD_DATA_ACQ_BOT_MODEL_V2);
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
	 * @return the stateVelocity in [m/s, rad/s]
	 */
	public double[] getStateVelocity()
	{
		double[] stateVel = new double[3];
		
		stateVel[0] = stateVelocity[0] * 0.001;
		stateVel[1] = stateVelocity[1] * 0.001;
		stateVel[2] = stateVelocity[2] * 0.01;
		
		return stateVel;
	}
	
	
	/**
	 * @return the stateVelocity in [m/s, rad/s]
	 */
	public List<Double> getStateVelocityList()
	{
		List<Double> stateVel = new ArrayList<>(3);
		
		stateVel.add(stateVelocity[0] * 0.001);
		stateVel.add(stateVelocity[1] * 0.001);
		stateVel.add(stateVelocity[2] * 0.01);
		
		return stateVel;
	}
	
	
	/**
	 * @param stateVelocity the stateVelocity to set in [m/s, rad/s]
	 */
	public void setStateVelocity(final double[] stateVelocity)
	{
		this.stateVelocity[0] = (int) (stateVelocity[0] * 1000.0);
		this.stateVelocity[1] = (int) (stateVelocity[1] * 1000.0);
		this.stateVelocity[2] = (int) (stateVelocity[2] * 100.0);
	}
	
	
	/**
	 * @return the encoderVelocity in [m/s, rad/s]
	 */
	public double[] getEncoderVelocity()
	{
		double[] encVel = new double[3];
		
		encVel[0] = encoderVelocity[0] * 0.001;
		encVel[1] = encoderVelocity[1] * 0.001;
		encVel[2] = encoderVelocity[2] * 0.01;
		
		return encVel;
	}
	
	
	/**
	 * @return the encoderVelocity in [m/s, rad/s]
	 */
	public List<Double> getEncoderVelocityList()
	{
		List<Double> encVel = new ArrayList<>(3);
		
		encVel.add(encoderVelocity[0] * 0.001);
		encVel.add(encoderVelocity[1] * 0.001);
		encVel.add(encoderVelocity[2] * 0.01);
		
		return encVel;
	}
	
	
	/**
	 * @param encVelocity the encoderVelocity to set in [m/s, rad/s]
	 */
	public void setEncoderVelocity(final double[] encVelocity)
	{
		encoderVelocity[0] = (int) (encVelocity[0] * 1000.0);
		encoderVelocity[1] = (int) (encVelocity[1] * 1000.0);
		encoderVelocity[2] = (int) (encVelocity[2] * 100.0);
	}
	
	
	/**
	 * @return the outputForce in [N, Nm]
	 */
	public double[] getOutputForce()
	{
		double[] outForce = new double[3];
		
		outForce[0] = outputForce[0] * 0.01;
		outForce[1] = outputForce[1] * 0.01;
		outForce[2] = outputForce[2] * 0.001;
		
		return outForce;
	}
	
	
	/**
	 * @return the outputForce in [N, Nm]
	 */
	public List<Double> getOutputForceList()
	{
		List<Double> outForce = new ArrayList<>(3);
		
		outForce.add(outputForce[0] * 0.01);
		outForce.add(outputForce[1] * 0.01);
		outForce.add(outputForce[2] * 0.001);
		
		return outForce;
	}
	
	
	/**
	 * @param outputForce the outputForce to set in [N, Nm]
	 */
	public void setOutputForce(final double[] outputForce)
	{
		this.outputForce[0] = (int) (outputForce[0] * 100.0);
		this.outputForce[1] = (int) (outputForce[1] * 100.0);
		this.outputForce[2] = (int) (outputForce[2] * 1000.0);
	}
	
	
	/**
	 * @return the efficiencyXY
	 */
	public double getEfficiencyXY()
	{
		int eff = (efficiency[0] & 0xFF) | ((efficiency[1] & 0x0F) << 8);
		return (eff * 1.0) / 4096.0;
	}
	
	
	/**
	 * @param efficiencyXY the efficiencyXY to set
	 */
	public void setEfficiencyXY(final double efficiencyXY)
	{
		int eff = (int) (efficiencyXY * 4096.0);
		efficiency[0] = eff & 0xFF;
		efficiency[1] &= 0xF0;
		efficiency[1] |= (eff & 0xF00) >> 8;
	}
	
	
	/**
	 * @return the efficiencyW
	 */
	public double getEfficiencyW()
	{
		int eff = ((efficiency[1] & 0xF0) >> 4) | ((efficiency[2] & 0xFF) << 4);
		return (eff * 1.0) / 4096.0;
	}
	
	
	/**
	 * @param efficiencyW the efficiencyW to set
	 */
	public void setEfficiencyW(final double efficiencyW)
	{
		int eff = (int) (efficiencyW * 4096.0);
		efficiency[1] &= 0x0F;
		efficiency[1] |= (eff & 0x0F) << 4;
		efficiency[2] = (eff & 0xFF0) >> 4;
	}
	
	
	/**
	 * @return modeXY
	 */
	public int getModeXY()
	{
		return (mode & 0x0F);
	}
	
	
	/**
	 * @param modeXY
	 */
	public void setModeXY(final int modeXY)
	{
		mode &= 0xF0;
		mode |= modeXY & 0x0F;
	}
	
	
	/**
	 * @return modeW
	 */
	public int getModeW()
	{
		return ((mode & 0xF0) >> 4);
	}
	
	
	/**
	 * @param modeW
	 */
	public void setModeW(final int modeW)
	{
		mode &= 0x0F;
		mode |= ((modeW & 0x0F) << 4);
	}
}
