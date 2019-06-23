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
	private int Kp[] = new int[3];
	private int Ki[] = new int[3];
	private int Kd[] = new int[3];
	private int slewMax[] = new int[3];

	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public TigerMovementSetParams()
	{
		for(int i = 0; i < 3; i++)
		{
			Kp[i] = 0;
			Ki[i] = 0;
			Kd[i] = 0;
			slewMax[i] = 0;
		}
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void setData(byte[] data)
	{
		for(int i = 0; i < 3; i++)
		{
			Kp[i] 		= byteArray2Int(data,  0+i*4);
			Ki[i] 		= byteArray2Int(data, 12+i*4);
			Kd[i] 		= byteArray2Int(data, 24+i*4);
			slewMax[i] 	= byteArray2Int(data, 36+i*4);
		}
	}

	@Override
	public byte[] getData()
	{
		byte data[] = new byte[getDataLength()];
		
		for(int i = 0; i < 3; i++)
		{
			int2ByteArray(data,  0+i*4, Kp[i]);
			int2ByteArray(data, 12+i*4, Ki[i]);
			int2ByteArray(data, 24+i*4, Kd[i]);
			int2ByteArray(data, 36+i*4, slewMax[i]);
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
	public void setPidParams(int axis, float kp, float ki, float kd, int slew)
	{
		if(axis > 2 || axis < 0)
			return;
		
		Kp[axis] = (int) (kp*10000.0f);
		Ki[axis] = (int) (ki*10000.0f);
		Kd[axis] = (int) (kd*10000.0f);
		slewMax[axis] = slew;
	}
	
	public float getKp(int axis)
	{
		return Kp[axis]/10000.0f;
	}

	public float getKi(int axis)
	{
		return Ki[axis]/10000.0f;
	}

	public float getKd(int axis)
	{
		return Kd[axis]/10000.0f;
	}
	
	public int getSlewMax(int axis)
	{
		return slewMax[axis];
	}

}
