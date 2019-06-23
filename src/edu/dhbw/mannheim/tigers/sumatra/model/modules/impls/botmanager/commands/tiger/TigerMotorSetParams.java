/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 18.04.2011
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.CommandConstants;


/**
 * Set all motor params.
 * 
 * @author AndreR
 * 
 */
public class TigerMotorSetParams extends ACommand
{
	/**
	 */
	public enum MotorMode
	{
		/** */
		MANUAL,
		/** */
		PID,
		/** */
		AUTOMATIC,
		/** */
		@Deprecated
		TWO_PID,
	}
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private int			mode			= 0;
	private final int	kP[]			= new int[5];
	private final int	kI[]			= new int[5];
	private final int	kD[]			= new int[5];
	private final int	slewMax[]	= new int[5];
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 */
	public TigerMotorSetParams()
	{
		mode = MotorMode.MANUAL.ordinal();
		
		for (int i = 0; i < 5; i++)
		{
			kP[i] = 0;
			kI[i] = 0;
			kD[i] = 0;
			slewMax[i] = 0;
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void setData(byte[] data)
	{
		mode = byteArray2UByte(data, 0);
		
		for (int i = 0; i < 5; i++)
		{
			kP[i] = byteArray2Int(data, 1 + 0 + (i * 4));
			kI[i] = byteArray2Int(data, 1 + 20 + (i * 4));
			kD[i] = byteArray2Int(data, 1 + 40 + (i * 4));
			slewMax[i] = byteArray2Int(data, 1 + 60 + (i * 4));
		}
	}
	
	
	@Override
	public byte[] getData()
	{
		final byte data[] = new byte[getDataLength()];
		
		byte2ByteArray(data, 0, mode);
		
		for (int i = 0; i < 5; i++)
		{
			int2ByteArray(data, 1 + 0 + (i * 4), kP[i]);
			int2ByteArray(data, 1 + 20 + (i * 4), kI[i]);
			int2ByteArray(data, 1 + 40 + (i * 4), kD[i]);
			int2ByteArray(data, 1 + 60 + (i * 4), slewMax[i]);
		}
		
		return data;
	}
	
	
	@Override
	public int getCommand()
	{
		return CommandConstants.CMD_MOTOR_SET_PARAMS;
	}
	
	
	@Override
	public int getDataLength()
	{
		return 81;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 * @param motor
	 * @param kp
	 * @param ki
	 * @param kd
	 * @param slew
	 */
	public void setPidParams(int motor, float kp, float ki, float kd, int slew)
	{
		if ((motor > 4) || (motor < 0))
		{
			return;
		}
		
		kP[motor] = (int) (kp * 10000.0f);
		kI[motor] = (int) (ki * 10000.0f);
		kD[motor] = (int) (kd * 10000.0f);
		slewMax[motor] = slew;
	}
	
	
	/**
	 * 
	 * @param mode
	 */
	public void setMode(MotorMode mode)
	{
		this.mode = mode.ordinal();
	}
	
	
	/**
	 * 
	 * @param mode
	 */
	public void setMode(int mode)
	{
		if (mode >= MotorMode.values().length)
		{
			return;
		}
		
		this.mode = mode;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public MotorMode getMode()
	{
		return MotorMode.values()[mode];
	}
	
	
	/**
	 * 
	 * @param motor
	 * @return
	 */
	public float getKp(int motor)
	{
		return kP[motor] / 10000.0f;
	}
	
	
	/**
	 * 
	 * @param motor
	 * @return
	 */
	public float getKi(int motor)
	{
		return kI[motor] / 10000.0f;
	}
	
	
	/**
	 * 
	 * @param motor
	 * @return
	 */
	public float getKd(int motor)
	{
		return kD[motor] / 10000.0f;
	}
	
	
	/**
	 * 
	 * @param motor
	 * @return
	 */
	public int getSlewMax(int motor)
	{
		return slewMax[motor];
	}
	
}
