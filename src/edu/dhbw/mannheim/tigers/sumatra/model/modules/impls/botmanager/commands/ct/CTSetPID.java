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

public class CTSetPID extends ACommand
{
	private int kp[] = new int[2];
	private int ki[] = new int[2];
	private int kd[] = new int[2];
	private int delay;
	
	public CTSetPID()
	{
	}

	public void setData(byte[] data)
	{
		kp[0] = byteArray2Int(data, 0);
		kp[1] = byteArray2Int(data, 4);
		ki[0] = byteArray2Int(data, 8);
		ki[1] = byteArray2Int(data, 12);
		kd[0] = byteArray2Int(data, 16);
		kd[1] = byteArray2Int(data, 20);
		delay = byteArray2UShort(data, 24);
	}
	
	public byte[] getData()
	{
		byte data[] = new byte[getDataLength()];
		
		int2ByteArray(data, 0, kp[0]);
		int2ByteArray(data, 4, kp[1]);
		int2ByteArray(data, 8, ki[0]);
		int2ByteArray(data, 12, ki[1]);
		int2ByteArray(data, 16, kd[0]);
		int2ByteArray(data, 20, kd[1]);
		
		short2ByteArray(data, 24, delay);
		
		return data;
	}
	
	@Override
	public int getCommand()
	{
		return CommandConstants.CMD_CT_SET_PID;
	}
	
	@Override
	public int getDataLength()
	{
		return 26;
	}


	public void setKp(float left, float right)
	{
		this.kp[0] = (int)(left*1000);
		this.kp[1] = (int)(right*1000);
	}

	public void setKi(float left, float right)
	{
		this.ki[0] = (int)(left*1000);
		this.ki[1] = (int)(right*1000);
	}

	public void setKd(float left, float right)
	{
		this.kd[0] = (int)(left*1000);
		this.kd[1] = (int)(right*1000);
	}

	public void setDelay(int delay)
	{
		this.delay = delay;
	}
	
	public float[] getKd()
	{
		float kd[] = new float[2];
		
		kd[0] = this.kd[0]/1000f;
		kd[1] = this.kd[1]/1000f;
		
		return kd;
	}
	
	public float[] getKi()
	{
		float ki[] = new float[2];
		
		ki[0] = this.kd[0]/1000f;
		ki[1] = this.ki[1]/1000f;
		
		return ki;
	}
	
	public float[] getKp()
	{
		float kp[] = new float[2];
		
		kp[0] = this.kp[0]/1000f;
		kp[1] = this.kp[1]/1000f;

		return kp;
	}
	
	public int getDelay()
	{
		return this.delay;
	}
}
