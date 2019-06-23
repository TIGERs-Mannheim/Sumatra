/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 08.08.2010
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ct;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.CommandConstants;

public class CTPIDHistory extends ACommand
{
	private int motorId;
	private int target;
	private int current;
	private int error;
	private int integral;
	private int derivative;

	// for calculation
	private float maxSpeed;
	
	// calculated
	private float targetF;
	private float currentF;
	private float errorF;
	private float integralF;
	private float derivativeF;

	public CTPIDHistory()
	{
		this(0, 0, 0, 0, 0, 0);
	}
	
	
	public CTPIDHistory(int motorId, int target, int current, int error, int integral, int derivative)
	{
		this.motorId = motorId;
		this.target = target;
		this.current = current;
		this.error = error;
		this.integral = integral;
		this.derivative = derivative;
	}
	

	public void setData(byte[] data)
	{
		motorId = byteArray2UByte(data, 0);
		target = byteArray2Int(data, 1);
		current = byteArray2Int(data, 5);
		error = byteArray2Int(data, 9);
		integral = byteArray2Int(data, 13);
		derivative = byteArray2Int(data, 17);
	}
	
	public byte[] getData()
	{
		byte data[] = new byte[getDataLength()];
		
		byte2ByteArray(data, 0, motorId);
		int2ByteArray(data, 1, target);
		int2ByteArray(data, 5, current);
		int2ByteArray(data, 9, error);
		int2ByteArray(data, 13, integral);
		int2ByteArray(data, 17, derivative);
		
		return data;
	}
	
	@Override
	public int getCommand()
	{
		return CommandConstants.CMD_CT_PID_HISTORY;
	}
	
	@Override
	public int getDataLength()
	{
		return 21;
	}

	
	public int[] getGroup()
	{
		int group[] = new int[5];
		
		group[0] = target;
		group[1] = current;
		group[2] = error;
		group[3] = integral;
		group[4] = derivative;
		
		return group;
	}
	
	public String[] getGroupHeader()
	{
		String names[] = new String[5];
		
		names[0] = "Target";
		names[1] = "Current";
		names[2] = "Error";
		names[3] = "Integral";
		names[4] = "Derivative";
		
		return names;
	}

	public int getMotorId()
	{
		return motorId;
	}

	public float getTarget()
	{
		return targetF;
	}

	public float getCurrent()
	{
		return currentF;
	}

	public float getError()
	{
		return errorF;
	}

	public float getIntegral()
	{
		return integralF;
	}

	public float getDerivative()
	{
		return derivativeF;
	}
	
	public void postProcess(float max)
	{
		maxSpeed = max;
		
		targetF = maxSpeed * target / 10000;
		currentF = maxSpeed * current / 10000;
		errorF = maxSpeed * error / 10000;
		integralF = maxSpeed * integral / 10000;
		derivativeF = maxSpeed * derivative / 10000;
	}
}
