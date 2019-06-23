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
	private int	id;
	private int	power;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 */
	public TigerMotorSetManual()
	{
	}
	
	
	/**
	 * 
	 * @param id
	 * @param power
	 */
	public TigerMotorSetManual(int id, int power)
	{
		setId(id);
		setPower(power);
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
		power = byteArray2Short(data, 1);
	}
	
	
	@Override
	public byte[] getData()
	{
		final byte data[] = new byte[getDataLength()];
		
		byte2ByteArray(data, 0, id);
		short2ByteArray(data, 1, power);
		
		return data;
	}
	
	
	@Override
	public int getCommand()
	{
		return CommandConstants.CMD_MOTOR_SET_MANUAL;
	}
	
	
	@Override
	public int getDataLength()
	{
		return 3;
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
