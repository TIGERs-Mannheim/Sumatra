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
	private int	id;
	private int	latest;
	private int	setpoint;
	private int	pError;
	private int	iError;
	private int	dError;
	private int	output;
	private int	overload;
	private int	eCurrent;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 */
	public TigerMotorPidLog()
	{
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
		set(id, current, setpoint, pError, iError, dError, output, overload, eCurrent);
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
		latest = byteArray2Int(data, 1);
		setpoint = byteArray2Int(data, 5);
		pError = byteArray2Int(data, 9);
		iError = byteArray2Int(data, 13);
		dError = byteArray2Int(data, 17);
		output = byteArray2Int(data, 21);
		overload = byteArray2UByte(data, 25);
		eCurrent = byteArray2UShort(data, 26);
	}
	
	
	@Override
	public byte[] getData()
	{
		final byte data[] = new byte[getDataLength()];
		
		byte2ByteArray(data, 0, id);
		int2ByteArray(data, 1, latest);
		int2ByteArray(data, 5, setpoint);
		int2ByteArray(data, 9, pError);
		int2ByteArray(data, 13, iError);
		int2ByteArray(data, 17, dError);
		int2ByteArray(data, 21, output);
		byte2ByteArray(data, 25, overload);
		short2ByteArray(data, 26, eCurrent);
		
		return data;
	}
	
	
	@Override
	public int getCommand()
	{
		return CommandConstants.CMD_MOTOR_PID_LOG;
	}
	
	
	@Override
	public int getDataLength()
	{
		return 28;
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
