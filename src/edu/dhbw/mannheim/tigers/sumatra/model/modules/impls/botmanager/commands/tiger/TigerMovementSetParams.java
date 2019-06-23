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
 * Set movement params.
 * Used for TWO_PID control mode.
 * 
 * @author AndreR
 * 
 */
@Deprecated
public class TigerMovementSetParams extends ACommand
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final int	kP[]			= new int[3];
	private final int	kI[]			= new int[3];
	private final int	kD[]			= new int[3];
	private final int	slewMax[]	= new int[3];
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 */
	public TigerMovementSetParams()
	{
		for (int i = 0; i < 3; i++)
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
		for (int i = 0; i < 3; i++)
		{
			kP[i] = byteArray2Int(data, 0 + (i * 4));
			kI[i] = byteArray2Int(data, 12 + (i * 4));
			kD[i] = byteArray2Int(data, 24 + (i * 4));
			slewMax[i] = byteArray2Int(data, 36 + (i * 4));
		}
	}
	
	
	@Override
	public byte[] getData()
	{
		final byte data[] = new byte[getDataLength()];
		
		for (int i = 0; i < 3; i++)
		{
			int2ByteArray(data, 0 + (i * 4), kP[i]);
			int2ByteArray(data, 12 + (i * 4), kI[i]);
			int2ByteArray(data, 24 + (i * 4), kD[i]);
			int2ByteArray(data, 36 + (i * 4), slewMax[i]);
		}
		
		return data;
	}
	
	
	@Override
	public int getCommand()
	{
		return CommandConstants.CMD_MOVEMENT_SET_PARAMS;
	}
	
	
	@Override
	public int getDataLength()
	{
		return 48;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 * @param axis
	 * @param kp
	 * @param ki
	 * @param kd
	 * @param slew
	 */
	public void setPidParams(int axis, float kp, float ki, float kd, int slew)
	{
		if ((axis > 2) || (axis < 0))
		{
			return;
		}
		
		kP[axis] = (int) (kp * 10000.0f);
		kI[axis] = (int) (ki * 10000.0f);
		kD[axis] = (int) (kd * 10000.0f);
		slewMax[axis] = slew;
	}
	
	
	/**
	 * 
	 * @param axis
	 * @return
	 */
	public float getKp(int axis)
	{
		return kP[axis] / 10000.0f;
	}
	
	
	/**
	 * 
	 * @param axis
	 * @return
	 */
	public float getKi(int axis)
	{
		return kI[axis] / 10000.0f;
	}
	
	
	/**
	 * 
	 * @param axis
	 * @return
	 */
	public float getKd(int axis)
	{
		return kD[axis] / 10000.0f;
	}
	
	
	/**
	 * 
	 * @param axis
	 * @return
	 */
	public int getSlewMax(int axis)
	{
		return slewMax[axis];
	}
	
}
