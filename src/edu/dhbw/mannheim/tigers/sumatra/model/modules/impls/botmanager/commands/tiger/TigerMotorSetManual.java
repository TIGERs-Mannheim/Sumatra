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
 * Set the motor speed manually.
 * Should be used only for testing.
 * 
 * @author AndreR
 * 
 */
public class TigerMotorSetManual extends ACommand
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	@SerialData(type = ESerialDataType.UINT8)
	private int	id;
	@SerialData(type = ESerialDataType.INT16)
	private int	power;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 */
	public TigerMotorSetManual()
	{
		super(ECommand.CMD_MOTOR_SET_MANUAL);
	}
	
	
	/**
	 * 
	 * @param id
	 * @param power
	 */
	public TigerMotorSetManual(int id, int power)
	{
		super(ECommand.CMD_MOTOR_SET_MANUAL);
		
		setId(id);
		setPower(power);
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
	 * @return the power
	 */
	public int getPower()
	{
		return power;
	}
	
	
	/**
	 * @param power the power to set
	 */
	public void setPower(int power)
	{
		this.power = power;
	}
}
