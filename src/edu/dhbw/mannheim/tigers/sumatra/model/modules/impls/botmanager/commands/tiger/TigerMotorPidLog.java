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
 * Log packet from a single motor containing PID information.
 * 
 * @author AndreR
 * 
 */
public class TigerMotorPidLog extends ACommand
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	@SerialData(type = ESerialDataType.UINT8)
	private int	id;
	@SerialData(type = ESerialDataType.INT32)
	private int	latest;
	@SerialData(type = ESerialDataType.INT32)
	private int	setpoint;
	@SerialData(type = ESerialDataType.INT32)
	private int	pError;
	@SerialData(type = ESerialDataType.INT32)
	private int	iError;
	@SerialData(type = ESerialDataType.INT32)
	private int	dError;
	@SerialData(type = ESerialDataType.INT32)
	private int	output;
	@SerialData(type = ESerialDataType.UINT8)
	private int	overload;
	@SerialData(type = ESerialDataType.UINT16)
	private int	eCurrent;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 */
	public TigerMotorPidLog()
	{
		super(ECommand.CMD_MOTOR_PID_LOG);
	}
	
	
	/**
	 * 
	 * @param id
	 * @param current
	 * @param setpoint
	 * @param pError
	 * @param iError
	 * @param dError
	 * @param output
	 * @param overload
	 * @param eCurrent
	 */
	public TigerMotorPidLog(int id, int current, int setpoint, int pError, int iError, int dError, int output,
			boolean overload, int eCurrent)
	{
		super(ECommand.CMD_MOTOR_PID_LOG);
		
		set(id, current, setpoint, pError, iError, dError, output, overload, eCurrent);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 * @param id
	 * @param current
	 * @param setpoint
	 * @param pError
	 * @param iError
	 * @param dError
	 * @param output
	 * @param overload
	 * @param eCurrent
	 */
	public void set(int id, int current, int setpoint, int pError, int iError, int dError, int output, boolean overload,
			int eCurrent)
	{
		setId(id);
		setLatest(current);
		setSetpoint(setpoint);
		setpError(pError);
		setiError(iError);
		setdError(dError);
		setOutput(output);
		setOverload(overload);
		setECurrent(eCurrent);
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
	 * @return the current
	 */
	public int getLatest()
	{
		return latest;
	}
	
	
	/**
	 * @param latest the current to set
	 */
	public void setLatest(int latest)
	{
		this.latest = latest;
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
	
	
	/**
	 * @return the pError
	 */
	public int getpError()
	{
		return pError;
	}
	
	
	/**
	 * @param pError the pError to set
	 */
	public void setpError(int pError)
	{
		this.pError = pError;
	}
	
	
	/**
	 * @return the iError
	 */
	public int getiError()
	{
		return iError;
	}
	
	
	/**
	 * @param iError the iError to set
	 */
	public void setiError(int iError)
	{
		this.iError = iError;
	}
	
	
	/**
	 * @return the dError
	 */
	public int getdError()
	{
		return dError;
	}
	
	
	/**
	 * @param dError the dError to set
	 */
	public void setdError(int dError)
	{
		this.dError = dError;
	}
	
	
	/**
	 * @return the output
	 */
	public int getOutput()
	{
		return output;
	}
	
	
	/**
	 * @param output the output to set
	 */
	public void setOutput(int output)
	{
		this.output = output;
	}
	
	
	/**
	 * @return the overload
	 */
	public boolean getOverload()
	{
		if (overload == 1)
		{
			return true;
		}
		
		return false;
	}
	
	
	/**
	 * @param overload the overload to set
	 */
	public void setOverload(boolean overload)
	{
		if (overload)
		{
			this.overload = 1;
		} else
		{
			this.overload = 0;
		}
	}
	
	
	/**
	 * @return the eCurrent
	 */
	public float getECurrent()
	{
		return eCurrent / 1000.0f;
	}
	
	
	/**
	 * @param eCurrent the eCurrent to set
	 */
	public void setECurrent(float eCurrent)
	{
		this.eCurrent = (int) (eCurrent * 1000.0f);
	}
}
