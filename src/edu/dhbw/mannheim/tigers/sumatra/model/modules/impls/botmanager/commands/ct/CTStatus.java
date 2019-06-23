/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 08.08.2010
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ct;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.CommandConstants;

public class CTStatus extends ACommand
{
	private int speed[] = new int[2];	// [mm/s]
	
	private int kp[] = new int[2];
	private int ki[] = new int[2];
	private int kd[] = new int[2];
	
	private int max[] = new int[2];	// [mm/s], maximum forward wheel speed
	private int min[] = new int[2];	// [mm/s], maximum reverse wheel speed
	
	private int delay;	// [ms]

	// calculated data
	private Vector2 velocityVector = new Vector2(); // [m/s] 2 components vector
	private float angularVelocity = 0;	// [m/s]
	
	public CTStatus()
	{
		this(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
	}
	
	/**
	 * @param speedL	current speed left wheel in [m/s]
	 * @param speedR	current speed right wheel in [m/s]
	 * @param kpL
	 * @param kpR
	 * @param kiL
	 * @param kiR
	 * @param kdL
	 * @param kdR
	 * @param maxL		max speed left wheel in [m/s]
	 * @param maxR		max speed right wheel in [m/s]
	 * @param minL
	 * @param minR
	 * @param delay
	 */
	public CTStatus(float speedL, float speedR, int kpL, int kpR, int kiL, int kiR, int kdL, int kdR, float maxL, float maxR, float minL, float minR, int delay)
	{
		// Values
		speed[0] = Math.round(speedL * 1000);
		speed[1] = Math.round(speedR * 1000);
		
		kp[0] = kpL;
		kp[1] = kpR;
		ki[0] = kiL;
		ki[1] = kiR;
		kd[0] = kdL;
		kd[1] = kdR;
		
		max[0] = Math.round(maxL * 1000);
		max[1] = Math.round(maxR * 1000);
		
		min[0] = Math.round(minL * 1000);
		min[1] = Math.round(minR * 1000);
		
		this.delay = delay;
		
		calcDirAndAv();
	}
	
	public void setData(byte[] data)
	{
		speed[0] = byteArray2Short(data, 0);
		speed[1] = byteArray2Short(data, 2);
		
		kp[0] = byteArray2Int(data, 4);
		kp[1] = byteArray2Int(data, 8);
		ki[0] = byteArray2Int(data, 12);
		ki[1] = byteArray2Int(data, 16);
		kd[0] = byteArray2Int(data, 20);
		kd[1] = byteArray2Int(data, 24);
		
		max[0] = byteArray2Short(data, 28);
		max[1] = byteArray2Short(data, 30);
		
		min[0] = byteArray2Short(data, 32);
		min[1] = byteArray2Short(data, 34);
		
		delay = byteArray2UShort(data, 36);
		
		calcDirAndAv();
	}
	
	public byte[] getData()
	{
		byte data[] = new byte[getDataLength()];
		
		short2ByteArray(data, 0, speed[0]);
		short2ByteArray(data, 2, speed[1]);
		
		int2ByteArray(data, 4, kp[0]);
		int2ByteArray(data, 8, kp[1]);
		int2ByteArray(data, 12, ki[0]);
		int2ByteArray(data, 16, ki[1]);
		int2ByteArray(data, 20, kd[0]);
		int2ByteArray(data, 24, kd[1]);
		
		short2ByteArray(data, 28, max[0]);
		short2ByteArray(data, 30, max[1]);
		
		short2ByteArray(data, 32, min[0]);
		short2ByteArray(data, 34, min[1]);
		
		short2ByteArray(data, 36, delay);
		
		return data;
	}
	
	@Override
	public int getCommand()
	{
		return CommandConstants.CMD_CT_STATUS;
	}
	
	@Override
	public int getDataLength()
	{
		return 38;
	}

	@Deprecated
	public float getSpeedLeft()
	{
		return ((float)speed[0])/1000;
	}

	@Deprecated
	public float getSpeedRight()
	{
		return ((float)speed[1])/1000;
	}
	
	/**
	 * 
	 * @return Wheelspeed in [m/s]
	 */
	public float[] getSpeed()
	{
		float[] ret = new float[2];
		ret[0] = ((float)speed[0])/1000;
		ret[1] = ((float)speed[1])/1000;
		return ret;
	}

	public float[] getKp()
	{
		float[] ret = new float[2];
		ret[0] = ((float)kp[0])/1000;
		ret[1] = ((float)kp[1])/1000;
		return ret;
	}

	public float[] getKi()
	{
		float[] ret = new float[2];
		ret[0] = ((float)ki[0])/1000;
		ret[1] = ((float)ki[1])/1000;
		return ret;
	}

	public float[] getKd()
	{
		float[] ret = new float[2];
		ret[0] = ((float)kd[0])/1000;
		ret[1] = ((float)kd[1])/1000;
		return ret;
	}
	
	public float[] getMin()
	{
		float[] ret = new float[2];
		ret[0] = ((float)min[0])/1000;
		ret[1] = ((float)min[1])/1000;
		return ret;
	}
	
	public float[] getMax()
	{
		float[] ret = new float[2];
		ret[0] = ((float)max[0])/1000;
		ret[1] = ((float)max[1])/1000;
		return ret;
	}

	public int getDelay()
	{
		return delay;
	}

	public Vector2 getDir()
	{
		return velocityVector;
	}

	public float getAngularVelocity()
	{
		return angularVelocity;
	}
	
	private void calcDirAndAv()
	{
		// float wheelDiameter = 0.056f;
		float botDiameter = 0.15f;
		float turnInfluence = (speed[0] - speed[1]) / 2f;
		
		angularVelocity = (turnInfluence) * 2 / botDiameter;
		velocityVector = new Vector2(0.0f, speed[0] - turnInfluence);		
	}
}
