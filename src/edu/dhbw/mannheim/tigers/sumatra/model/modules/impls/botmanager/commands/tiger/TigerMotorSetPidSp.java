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
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ECommand;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData.ESerialDataType;


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
	@SerialData(type = ESerialDataType.UINT8)
	private int	id;
	@SerialData(type = ESerialDataType.INT32)
	private int	setpoint;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 */
	public TigerMotorSetPidSp()
	{
		super(ECommand.CMD_MOTOR_SET_PID_SP);
	}
	
	
	/**
	 * @param id
	 * @param setpoint
	 */
	public TigerMotorSetPidSp(int id, int setpoint)
	{
		super(ECommand.CMD_MOTOR_SET_PID_SP);
		
		setId(id);
		setSetpoint(setpoint);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
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
