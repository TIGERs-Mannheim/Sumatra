/* 
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 13.10.2010
 * Author(s): AndreR
 *
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.CommandConstants;

/**
 * Set PID setpoint manually.
 * Should be used only for testing.
 * 
 * @author AndreR
 * 
 */
public class TigerMotorSetPidSp extends ACommand
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private int id;
	private int setpoint;
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public TigerMotorSetPidSp()
	{
	}
	
	public TigerMotorSetPidSp(int id, int setpoint)
	{
		setId(id);
		setSetpoint(setpoint);
	}

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void setData(byte[] data)
	{
		id = byteArray2UByte(data, 0);
		setpoint = byteArray2Int(data, 1);
	}

	@Override
	public byte[] getData()
	{
		byte data[] = new byte[getDataLength()];
		
		byte2ByteArray(data, 0, id);
		int2ByteArray(data, 1, setpoint);
		
		return data;
	}
	
	@Override
	public int getCommand()
	{
		return CommandConstants.CMD_MOTOR_SET_PID_SP;
	}
	
	@Override
	public int getDataLength()
	{
		return 5;
	}


	/**
	 * @return the id
	 */
	public int getId()
	{
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id)
	{
		this.id = id;
	}

	/**
	 * @return the setpoint
	 */
	public int getSetpoint()
	{
		return setpoint;
	}

	/**
	 * @param setpoint the setpoint to set
	 */
	public void setSetpoint(int setpoint)
	{
		this.setpoint = setpoint;
	}
}
