/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 18.04.2011
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ECommand;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData.ESerialDataType;


/**
 * Enable/Disable various logs.
 *
 * @author AndreR
 */
public class TigerSystemSetLogs extends ACommand
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	@SerialData(type = ESerialDataType.UINT8)
	private final int	motor[]	= new int[5];
	@SerialData(type = ESerialDataType.UINT8)
	private int			kicker;
	@SerialData(type = ESerialDataType.UINT8)
	private int			power;
	@SerialData(type = ESerialDataType.UINT8)
	private int			movement;
	@SerialData(type = ESerialDataType.UINT8)
	private int			extMovement;
	@SerialData(type = ESerialDataType.UINT8)
	private int			accel;
	@SerialData(type = ESerialDataType.UINT8)
	private int			ir;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 *
	 */
	public TigerSystemSetLogs()
	{
		super(ECommand.CMD_SYSTEM_SET_LOGS);
		
		for (int i = 0; i < 5; i++)
		{
			motor[i] = 0;
		}
		
		kicker = 0;
		power = 0;
		movement = 0;
		extMovement = 0;
		accel = 0;
		ir = 0;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
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
	public void setKicker(final boolean kicker)
	{
		this.kicker = kicker ? 1 : 0;
	}
	
	
	/**
	 * @return the power
	 */
	public boolean getPower()
	{
		return power > 0 ? true : false;
	}
	
	
	/**
	 * @param power the power to set
	 */
	public void setPower(final boolean power)
	{
		this.power = power ? 1 : 0;
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
	public void setMovement(final boolean movement)
	{
		this.movement = movement ? 1 : 0;
	}
	
	
	/**
	 * @return the movement
	 */
	public boolean getExtMovement()
	{
		return extMovement > 0 ? true : false;
	}
	
	
	/**
	 * @param movement the movement to set
	 */
	public void setExtMovement(final boolean movement)
	{
		extMovement = movement ? 1 : 0;
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
	public void setAccel(final boolean accel)
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
	public void setIr(final boolean ir)
	{
		this.ir = ir ? 1 : 0;
	}
	
	
	/**
	 * @param id
	 * @return
	 */
	public boolean getMotor(final int id)
	{
		if ((id > 4) || (id < 0))
		{
			return false;
		}
		
		return motor[id] > 0 ? true : false;
	}
	
	
	/**
	 * @param id
	 * @param enable
	 */
	public void setMotor(final int id, final boolean enable)
	{
		if ((id > 4) || (id < 0))
		{
			return;
		}
		
		motor[id] = enable ? 1 : 0;
	}
	
	
	/**
	 * @return
	 */
	public int[] getMotors()
	{
		return motor;
	}
}
