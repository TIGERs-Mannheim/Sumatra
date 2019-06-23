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
 * Enable/Disable various logs.
 * 
 * @author AndreR
 * 
 */
public class TigerSystemSetLogs extends ACommand
{

	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private int motor[] = new int[5];
	private int kicker;
	private int movement;
	private int accel;
	private int ir;	

	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public TigerSystemSetLogs()
	{
		for(int i = 0; i < 5; i++)
		{
			motor[i] = 0;
		}
		
		kicker = 0;
		movement = 0;
		accel = 0;
		ir = 0;
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void setData(byte[] data)
	{
		for(int i = 0; i < 5; i++)
		{
			motor[i] = byteArray2UByte(data, i);
		}
		
		kicker = byteArray2UByte(data, 5);
		movement = byteArray2UByte(data, 6);
		accel = byteArray2UByte(data, 7);
		ir = byteArray2UByte(data, 8);
	}

	@Override
	public byte[] getData()
	{
		byte data[] = new byte[getDataLength()];
		
		for(int i = 0; i < 5; i++)
		{
			byte2ByteArray(data, i, motor[i]);
		}
		
		byte2ByteArray(data, 5, kicker);
		byte2ByteArray(data, 6, movement);
		byte2ByteArray(data, 7, accel);
		byte2ByteArray(data, 8, ir);
		
		return data;
	}

	@Override
	public int getCommand()
	{
		return CommandConstants.CMD_SYSTEM_SET_LOGS;
	}

	@Override
	public int getDataLength()
	{
		return 9;
	}


	/**
	 * @return the kicker
	 */
	public boolean getKicker()
	{
		return kicker > 0 ? true : false;
	}


	/**
	 * @param kicker the kicker to set
	 */
	public void setKicker(boolean kicker)
	{
		this.kicker = kicker ? 1 : 0;
	}


	/**
	 * @return the movement
	 */
	public boolean getMovement()
	{
		return movement > 0 ? true : false;
	}


	/**
	 * @param movement the movement to set
	 */
	public void setMovement(boolean movement)
	{
		this.movement = movement ? 1 : 0;
	}


	/**
	 * @return the accel
	 */
	public boolean getAccel()
	{
		return accel > 0 ? true : false;
	}


	/**
	 * @param accel the accel to set
	 */
	public void setAccel(boolean accel)
	{
		this.accel = accel ? 1 : 0;
	}


	/**
	 * @return the ir
	 */
	public boolean getIr()
	{
		return ir > 0 ? true : false;
	}


	/**
	 * @param ir the ir to set
	 */
	public void setIr(boolean ir)
	{
		this.ir = ir ? 1 : 0;
	}
	
	public boolean getMotor(int id)
	{
		if(id > 4 || id < 0)
		{
			return false;
		}
		
		return motor[id] > 0 ? true : false;
	}
	
	public void setMotor(int id, boolean enable)
	{
		if(id > 4 || id < 0)
		{
			return;
		}
		
		motor[id] = enable ? 1 : 0;
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
