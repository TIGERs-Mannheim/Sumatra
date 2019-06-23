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
	public enum MotorMode
	{
		MANUAL,
		PID,
		AUTOMATIC,
		@Deprecated
		TWO_PID,
	}

	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private int mode = 0;
	private int Kp[] = new int[5];
	private int Ki[] = new int[5];
	private int Kd[] = new int[5];
	private int slewMax[] = new int[5];

	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public TigerMotorSetParams()
	{
		mode = MotorMode.MANUAL.ordinal();

		for(int i = 0; i < 5; i++)
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
		mode = byteArray2UByte(data, 0);
		
		for(int i = 0; i < 5; i++)
		{
			Kp[i] 		= byteArray2Int(data, 1+ 0+i*4);
			Ki[i] 		= byteArray2Int(data, 1+20+i*4);
			Kd[i] 		= byteArray2Int(data, 1+40+i*4);
			slewMax[i] 	= byteArray2Int(data, 1+60+i*4);
		}
	}

	@Override
	public byte[] getData()
	{
		byte data[] = new byte[getDataLength()];
		
		byte2ByteArray(data, 0, mode);
		
		for(int i = 0; i < 5; i++)
		{
			int2ByteArray(data, 1+ 0+i*4, Kp[i]);
			int2ByteArray(data, 1+20+i*4, Ki[i]);
			int2ByteArray(data, 1+40+i*4, Kd[i]);
			int2ByteArray(data, 1+60+i*4, slewMax[i]);
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
	public void setPidParams(int motor, float kp, float ki, float kd, int slew)
	{
		if(motor > 4 || motor < 0)
			return;
		
		Kp[motor] = (int) (kp*10000.0f);
		Ki[motor] = (int) (ki*10000.0f);
		Kd[motor] = (int) (kd*10000.0f);
		slewMax[motor] = slew;
	}
	
	public void setMode(MotorMode mode)
	{
		this.mode = mode.ordinal();
	}

	public void setMode(int mode)
	{
		if(mode >= MotorMode.values().length)
			return;
		
		this.mode = mode;
	}
	
	public MotorMode getMode()
	{
		return MotorMode.values()[mode];
	}
	
	public float getKp(int motor)
	{
		return Kp[motor]/10000.0f;
	}

	public float getKi(int motor)
	{
		return Ki[motor]/10000.0f;
	}

	public float getKd(int motor)
	{
		return Kd[motor]/10000.0f;
	}
	
	public int getSlewMax(int motor)
	{
		return slewMax[motor];
	}

}
